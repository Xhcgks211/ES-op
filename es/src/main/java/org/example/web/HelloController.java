package org.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 简单示例控制器，用于验证 Spring Boot 应用已成功启动.
 *
 * 访问 http://localhost:8080/hello 即可看到 JSON 响应.
 */
@RestController
public class HelloController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from ES-op Spring Boot!",
                "time", LocalDateTime.now().format(FORMATTER)
        );
    }
}