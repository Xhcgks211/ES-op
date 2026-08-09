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
 * 关键改动：
 * 1) 重写 clientConfiguration 接管连接参数；
 * 2) 重写 jsonpMapper 开启 ESCAPE_NON_ASCII，
 *    让所有非 ASCII 字符以 \\uXXXX 转义写入 JSON，
 *    规避 ES 8.x 收到 application/vnd.elasticsearch+json 但无 charset 时
 *    按 ISO-8859-1 解码导致中文乱码的问题.
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