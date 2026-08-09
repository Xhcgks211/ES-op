package org.example.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ES 操作服务示例.
 *
 * 这里仅演示集群连通性与基础健康检查，
 * 索引/文档 API 可在此基础上扩展.
 */
@Service
public class EsService {

    private static final Logger log = LoggerFactory.getLogger(EsService.class);

    private final ElasticsearchClient client;

    public EsService(ElasticsearchClient client) {
        this.client = client;
    }

    /** 集群是否可达. */
    public boolean ping() {
        try {
            return client.ping().value();
        } catch (Exception e) {
            log.warn("ES ping failed: {}", e.getMessage());
            return false;
        }
    }

    /** 集群健康度概要信息. */
    public Map<String, Object> clusterInfo() throws Exception {
        HealthResponse health = client.cluster().health(h -> h.timeout(t -> t.time("5s")));
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("clusterName", health.clusterName());
        info.put("status", mapStatus(health.status()));
        info.put("numberOfNodes", health.numberOfNodes());
        info.put("activePrimaryShards", health.activePrimaryShards());
        info.put("activeShards", health.activeShards());
        info.put("unassignedShards", health.unassignedShards());
        return info;
    }

    private static String mapStatus(HealthStatus status) {
        return switch (status) {
            case Green -> "GREEN";
            case Yellow -> "YELLOW";
            case Red -> "RED";
        };
    }
}