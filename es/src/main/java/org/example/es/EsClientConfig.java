package org.example.es;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.net.URI;
import java.util.Arrays;

/**
 * 自定义 ElasticsearchClient 配置.
 *
 * 1) 从 spring.elasticsearch.* 读取连接参数；
 * 2) 自定义 JsonpMapper：
 *    - findAndRegisterModules() 注册 JavaTimeModule；
 *    - ESCAPE_NON_ASCII=true，非 ASCII 字符以 \\uXXXX 写入 JSON，
 *      避免 ES 8.x 因 charset 协商把 UTF-8 字节当 ISO-8859-1 解码导致中文乱码；
 *    - 保留 WRITE_DATES_AS_TIMESTAMPS=true（默认），让 LocalDateTime 在 ES 端存为
 *      epoch_millis（与 FieldIndex.updateTime 用 epoch_millis 格式一致）.
 */
@Configuration
@EnableConfigurationProperties(EsProperties.class)
public class EsClientConfig extends ElasticsearchConfiguration {

    private final EsProperties props;

    public EsClientConfig(EsProperties props) {
        this.props = props;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        String[] hostAndPorts = Arrays.stream(props.getUris().split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(EsClientConfig::toHostPort).toArray(String[]::new);

        ClientConfiguration.TerminalClientConfigurationBuilder builder =
                ClientConfiguration.builder().connectedTo(hostAndPorts);

        if (notBlank(props.getUsername()) && notBlank(props.getPassword())) {
            builder = builder.withBasicAuth(props.getUsername(), props.getPassword());
        }

        return builder
                .withConnectTimeout(props.getConnectionTimeout())
                .withSocketTimeout(props.getSocketTimeout())
                .build();
    }

    @Override
    public JsonpMapper jsonpMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        return new JacksonJsonpMapper(mapper);
    }

    private static String toHostPort(String u) {
        URI uri = URI.create(u);
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 9200) : uri.getPort();
        return uri.getHost() + ":" + port;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}