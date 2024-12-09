package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    @GetMapping("/startWebSocket")
    public String startWebSocket() {
        return "WebSocket 서버가 실행 중입니다!";
    }
}

