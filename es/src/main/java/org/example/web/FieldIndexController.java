package org.example.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import org.example.domain.FieldIndex;
import org.example.domain.FieldIndexRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * field_index REST 閹恒儱褰?
 *
 * <p>娑撳鏁痪锕€鐣鹃敍姝緻code esId}閿涘湕S 閺傚洦銆?_id閿涘鏁辩拫鍐暏閺傝瀵滄稉姘缁撅箑鐣鹃幏鍏煎复閿? * 娓氬顩?{@code "TABLE_user_info_001"}閿涘矂娓舵穱婵婄槈 {@code (type, id)} 缂佸嫬鎮庨崬顖欑.
 * 娑撴艾濮?{@code id} 閺勵垱娅橀柅姘摟濞堢绱濋崣顖炲櫢婢?</p>
 *
 * <ul>
 *   <li>POST   /field-index                - 閸掓稑缂撻敍鍧媠Id 缂傝櫣娓烽弮鎯板殰閸?UUID閿?/li>
 *   <li>PUT    /field-index/{esId}         - 閸忋劑鍣洪弴鎸庡床閿涘澃ave閿涘矁铔?_index API閿?/li>
 *   <li>POST   /field-index/_upsert        - 閹?esId upsert閿涘潈update + doc_as_upsert=true閿涘绱濊箛鍛炊 esId</li>
 *   <li>GET    /field-index/{esId}         - 鐠囷附鍎?/li>
 *   <li>DELETE /field-index/{esId}         - 閸掔娀娅?/li>
 *   <li>GET    /field-index?page=0&size=20 - 閸掑棝銆?/li>
 *   <li>GET    /field-index/search/type?type=xxx</li>
 *   <li>GET    /field-index/search/name?keyword=xxx</li>
 *   <li>GET    /field-index/search/code?code=xxx</li>
 * </ul>
 */
@RestController
@RequestMapping("/field-index")
public class FieldIndexController {

    private static final String INDEX = "field_index";

    private final FieldIndexRepository repository;
    private final ElasticsearchClient client;

    public FieldIndexController(FieldIndexRepository repository,
                                ElasticsearchClient client) {
        this.repository = repository;
        this.client = client;
    }

    @PostMapping
    public FieldIndex create(@RequestBody FieldIndex doc) {
        if (doc.getEsId() == null || doc.getEsId().isBlank()) {
            doc.setEsId(UUID.randomUUID().toString());
        }
        if (doc.getUpdateTime() == null) {
            doc.setUpdateTime(System.currentTimeMillis());
        }
        return repository.save(doc);
    }

    @PutMapping("/{esId}")
    public ResponseEntity<FieldIndex> update(@PathVariable String esId, @RequestBody FieldIndex doc) {
        doc.setEsId(esId);
        doc.setUpdateTime(System.currentTimeMillis());
        return ResponseEntity.ok(repository.save(doc));
    }

    /**
     * upsert閿涙碍瀵?body 娑?{@code esId} upsert閿涘牐鐨熼悽銊︽煙闂団偓娣囨繆鐦?{@code esId} 閸烆垯绔撮敍?     * 闁艾鐖堕幐?{@code type + id} 閹峰吋甯撮敍?
     *
     * <p>鎼存洖鐪扮挧?ES 閸樼喓鏁?{@code POST /{index}/_update/{esId}} + {@code doc_as_upsert=true}閿?     * 娑?{@link #create} / {@link #update} 閻?{@code _index} 閸忋劑鍣洪弴鎸庡床娑撳秴鎮撻敍?     * 鏉╂瑩鍣烽張宥呭缁旑垯绱伴崠鍝勫瀻鏉╂柨娲?{@code result: "created"} 閹?{@code result: "updated"}.</p>
     *
     * <p>濞夈劍鍓伴敍姝緻code _update} + {@code doc} 濡€崇础娑撳绱漿@code doc} 閺勵垬鈧苯鍙忛柌蹇旀禌閹诡潿鈧秷顕㈡稊澶涚礉
     * 娑撳秳绱堕惃鍕摟濞堝吀绱扮悮顐ｇ缁岀尨绱欐稉宥呮倱娴?MongoDB 閻?$set閿?
     * 閼汇儵娓?partial update 鐠囬攱鏁奸悽?scripted_upsert + painless script.</p>
     */
    @PostMapping("/_upsert")
    public ResponseEntity<Map<String, Object>> upsert(@RequestBody FieldIndex doc) throws Exception {
        if (doc.getEsId() == null || doc.getEsId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "esId is required for upsert (use 'type_id' or similar composite key)"));
        }

        final String esId = doc.getEsId();

        UpdateResponse<FieldIndex> resp = client.update(u -> u
                        .index(INDEX)
                        .id(esId)
                        .doc(doc)
                        .docAsUpsert(true),
                FieldIndex.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("esId", resp.id());
        result.put("id", doc.getBizId());
        result.put("type", doc.getType());
        result.put("esResult", resp.result().jsonValue());  // "created" / "updated" / "noop"
        result.put("version", resp.version());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{esId}")
    public ResponseEntity<FieldIndex> get(@PathVariable String esId) {
        Optional<FieldIndex> d = repository.findById(esId);
        return d.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{esId}")
    public ResponseEntity<Void> delete(@PathVariable String esId) {
        repository.deleteById(esId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public org.springframework.data.domain.Page<FieldIndex> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        return repository.findAll(pageable);
    }

    @GetMapping("/search/type")
    public List<FieldIndex> searchByType(@RequestParam String type) {
        return repository.findByType(type);
    }

    @GetMapping("/search/name")
    public List<FieldIndex> searchByName(@RequestParam String keyword) {
        return repository.findByNameContaining(keyword);
    }

    @GetMapping("/search/code")
    public List<FieldIndex> searchByCode(@RequestParam String code) {
        return repository.findByCode(code);
    }

    @PostMapping("/_debug_upsert")
    public ResponseEntity<Map<String, Object>> debugUpsert(@RequestBody FieldIndex doc) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper tmpMapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
        tmpMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        String s1 = tmpMapper.writeValueAsString(doc);
        com.fasterxml.jackson.databind.ObjectMapper tmpMapper2 = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
        String s2 = tmpMapper2.writeValueAsString(doc);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("withEscape", s1);
        r.put("withoutEscape", s2);
        return ResponseEntity.ok(r);
    }
}