package org.example.es;

import org.example.domain.FieldIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

/**
 * 启动时确保业务索引存在.
 *
 * - 若已存在则跳过；
 * - 不存在则根据实体注解创建 mapping.
 *
 * 失败时仅记录警告，不会阻断应用启动.
 */
@Configuration
public class IndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    @Bean
    public ApplicationRunner ensureFieldIndex(ElasticsearchOperations operations) {
        return args -> ensureIndex(operations, FieldIndex.class);
    }

    private void ensureIndex(ElasticsearchOperations operations, Class<?> entityClass) {
        try {
            IndexOperations indexOps = operations.indexOps(entityClass);
            String name = indexOps.getIndexCoordinates().getIndexName();
            if (indexOps.exists()) {
                log.info("Index '{}' already exists, skip create.", name);
                return;
            }
            indexOps.createWithMapping();
            log.info("Created index '{}' with mapping derived from {}", name, entityClass.getSimpleName());
        } catch (Exception e) {
            log.warn("Failed to ensure index for {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
    }
}