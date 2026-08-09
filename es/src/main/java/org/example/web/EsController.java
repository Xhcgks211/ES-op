package org.example.web;

import org.example.es.EsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ES 相关 REST 接口，便于快速验证集群连通性.
 *
 * <ul>
 *   <li>{@code GET /es/ping}  - 是否可达</li>
 *   <li>{@code GET /es/info}  - 集群健康概要</li>
 * </ul>
 */
@RestController
@RequestMapping("/es")
public class EsController {

    private final EsService esService;

    public EsController(EsService esService) {
        this.esService = esService;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean ok = esService.ping();
        body.put("reachable", ok);
        body.put("message", ok ? "ES cluster is reachable" : "ES cluster is NOT reachable");
        return body;
    }

    @GetMapping("/info")
    public Map<String, Object> info() throws Exception {
        return esService.clusterInfo();
    }
}