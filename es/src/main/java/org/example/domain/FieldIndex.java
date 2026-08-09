package org.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * ES 索引 {@code field_index} 的文档模型.
 *
 * <p>字段约定：
 * <ul>
 *   <li>{@code esId} —— ES 文档主键（{@code _id}），由调用方按业务约定拼接，
 *       例如 {@code "TABLE_user_info_001"}，需保证 {@code (type, bizId)} 组合唯一.</li>
 *   <li>{@code id}（JSON 名）—— 业务 id，普通 keyword 字段，可重复；
 *       Java 字段名为 {@code bizId}（避免与 Spring Data 的 id 探测冲突）.</li>
 * </ul>
 *
 * <p>{@code updateTime} 用 {@code epoch_millis}（Long）存储，
 * 避免 Jackson 默认把 LocalDateTime 序列化成数组后 Spring Data ES 解析失败.</p>
 */
@Document(indexName = "field_index", createIndex = false)
public class FieldIndex {

    /** ES 主键（{@code _id}），由调用方按业务约定拼接. */
    @Id
    @Field(type = FieldType.Keyword)
    private String esId;

    /** 业务 id，JSON 里仍叫 {@code "id"}，Java 字段名是 {@code bizId}，可重复. */
    @JsonProperty("id")
    @Field(type = FieldType.Keyword, name = "id")
    private String bizId;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Integer)
    private Integer yn;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long updateTime;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tableName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String code;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Keyword)
    private String level;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String entityName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String method;

    @Field(type = FieldType.Keyword)
    private String securityLevel;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String enumName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String businessDefinition;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String applicablScenarios;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String businessScope;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String ownerErp;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String managerErp;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String valueName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String valueDescription;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String valueCode;

    public FieldIndex() {}

    public String getEsId() { return esId; }
    public void setEsId(String esId) { this.esId = esId; }

    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getYn() { return yn; }
    public void setYn(Integer yn) { this.yn = yn; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }

    public String getEnumName() { return enumName; }
    public void setEnumName(String enumName) { this.enumName = enumName; }

    public String getBusinessDefinition() { return businessDefinition; }
    public void setBusinessDefinition(String businessDefinition) { this.businessDefinition = businessDefinition; }

    public String getApplicablScenarios() { return applicablScenarios; }
    public void setApplicablScenarios(String applicablScenarios) { this.applicablScenarios = applicablScenarios; }

    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }

    public String getOwnerErp() { return ownerErp; }
    public void setOwnerErp(String ownerErp) { this.ownerErp = ownerErp; }

    public String getManagerErp() { return managerErp; }
    public void setManagerErp(String managerErp) { this.managerErp = managerErp; }

    public String getValueName() { return valueName; }
    public void setValueName(String valueName) { this.valueName = valueName; }

    public String getValueDescription() { return valueDescription; }
    public void setValueDescription(String valueDescription) { this.valueDescription = valueDescription; }

    public String getValueCode() { return valueCode; }
    public void setValueCode(String valueCode) { this.valueCode = valueCode; }
}