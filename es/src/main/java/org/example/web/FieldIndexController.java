package org.example.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * field_index REST 接口.
 *
 * <p>主键约定：{@code esId}（ES 文档 _id）由调用方按业务约定拼接，
 * 例如 {@code "TABLE_user_info_001"}，需保证 {@code (type, bizId)} 组合唯一.
 * 业务 {@code id}（Java 字段 {@code bizId}）是普通字段，可重复.</p>
 *
 * <ul>
 *   <li>POST   /field-index                       - 创建（esId 缺省时自动 UUID）</li>
 *   <li>PUT    /field-index/{esId}                - 全量替换（save，走 _index API）</li>
 *   <li>POST   /field-index/_upsert               - 按 esId upsert（_update + doc_as_upsert=true），必传 esId</li>
 *   <li>GET    /field-index/{esId}                - 详情</li>
 *   <li>DELETE /field-index/{esId}                - 删除</li>
 *   <li>GET    /field-index?page=0&size=20        - 分页</li>
 *   <li>GET    /field-index/search/type?type=xxx</li>
 *   <li>GET    /field-index/search/name?keyword=xxx</li>
 *   <li>GET    /field-index/search/code?code=xxx</li>
 *   <li>GET    /field-index/search/hybrid?q=xxx   - 混合搜索（text 模糊 + keyword 精确）</li>
 * </ul>
 */
@RestController
@RequestMapping("/field-index")
public class FieldIndexController {

    private static final String INDEX = "field_index";

    /** text 类型字段，参与模糊匹配（用 IK 分词器的 search_analyzer = ik_smart）. */
    private static final List<String> TEXT_FIELDS = List.of(
            "name^2", "tableName", "code", "description",
            "entityName", "method", "enumName",
            "businessDefinition", "applicablScenarios", "businessScope",
            "ownerErp", "managerErp",
            "valueName", "valueDescription", "valueCode"
    );

    /** keyword 类型字段，参与精确匹配. */
    private static final List<String> KEYWORD_FIELDS = List.of("level", "securityLevel");

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
     * upsert：按 body 中 {@code esId} upsert（调用方需保证 {@code esId} 唯一，
     * 通常按 {@code type + bizId} 拼接）.
     *
     * <p>底层走 ES 原生 {@code POST /{index}/_update/{esId}} + {@code doc_as_upsert=true}.</p>
     */
    @PostMapping("/_upsert")
    public ResponseEntity<Map<String, Object>> upsert(@RequestBody FieldIndex doc) throws IOException {
        if (doc.getEsId() == null || doc.getEsId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "esId is required for upsert (use 'type_bizId' or similar composite key)"));
        }
        if (doc.getUpdateTime() == null) {
            doc.setUpdateTime(System.currentTimeMillis());
        }

        final String esId = doc.getEsId();
        boolean existedBefore = client.exists(e -> e.index(INDEX).id(esId)).value();

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
        result.put("esResult", resp.result().jsonValue());
        result.put("existedBefore", existedBefore);
        result.put("version", resp.version());
        result.put("updateTime", doc.getUpdateTime());
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

    /**
     * 混合搜索：{@code q} 同时在 text 字段（多字段模糊）和 keyword 字段（精确）中匹配.
     *
     * <p>查询结构：
     * <pre>
     *   bool {
     *     should: [
     *       multi_match { fields: [所有 text 字段], type: best_fields },
     *       term        { level }         ,     // 精确
     *       terms       { securityLevel }        // 精确
     *     ],
     *     minimum_should_match: 1
     *   }
     * </pre>
     *
     * <p>text 字段会自动用 mapping 里的 search_analyzer（即 {@code ik_smart}）分词；
     * keyword 字段（{@code level}/{@code securityLevel}）做 term 精确匹配.</p>
     */
    @GetMapping("/search/hybrid")
    public List<FieldIndex> hybridSearch(@RequestParam("q") String keyword,
                                         @RequestParam(defaultValue = "50") int size) throws IOException {
        final String kw = keyword;
        if (kw == null || kw.isBlank()) {
            return List.of();
        }

        List<Query> shoulds = new java.util.ArrayList<>();
        // 1) text 字段模糊匹配（用 ik_smart 分词）
        shoulds.add(Query.of(qb -> qb.multiMatch(m -> m
                .query(kw)
                .fields(TEXT_FIELDS)
                .type(TextQueryType.BestFields))));
        // 2) keyword 字段精确匹配（level + securityLevel）
        for (String kf : KEYWORD_FIELDS) {
            shoulds.add(Query.of(qb -> qb.term(t -> t
                    .field(kf)
                    .value(v -> v.stringValue(kw)))));
        }

        SearchResponse<FieldIndex> resp = client.search(s -> s
                        .index(INDEX)
                        .size(size)
                        .query(qb -> qb.bool(b -> b
                                .should(shoulds)
                                .minimumShouldMatch("1")))
                , FieldIndex.class);

        return resp.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();
    }
}