package org.example.es;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ES 连接相关配置项，前缀 {@code spring.elasticsearch}.
 *
 * 仅给 {@link EsClientConfig} 读取用，
 * 字段类型与 Spring Boot 标准 ElasticsearchProperties 保持一致.
 */
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class EsProperties {

    private String uris;
    private String username;
    private String password;
    private Duration connectionTimeout = Duration.ofSeconds(10);
    private Duration socketTimeout = Duration.ofSeconds(30);

    public String getUris() { return uris; }
    public void setUris(String uris) { this.uris = uris; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Duration getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Duration connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public Duration getSocketTimeout() { return socketTimeout; }
    public void setSocketTimeout(Duration socketTimeout) { this.socketTimeout = socketTimeout; }
}