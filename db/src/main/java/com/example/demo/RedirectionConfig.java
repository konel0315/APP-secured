package com.example.demo;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedirectionConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        // HTTPS만 사용할 수 있도록 설정
        return new TomcatServletWebServerFactory();  // HTTP 커넥터를 추가하지 않음
    }
}
