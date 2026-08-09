package org.example.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * field_index 文档 Repository.
 *
 * 精确匹配走 keyword 字段（id / type / level / securityLevel），
 * 全文匹配走 text 字段（name / code / description 等带 IK 分词的字段）.
 */
@Repository
public interface FieldIndexRepository extends ElasticsearchRepository<FieldIndex, String> {

    /** 根据 type 精确查询. */
    List<FieldIndex> findByType(String type);

    /** name 包含关键词（match 查询，会经过 ik_smart 分词）. */
    List<FieldIndex> findByNameContaining(String keyword);

    /** code 精确匹配. */
    List<FieldIndex> findByCode(String code);
}