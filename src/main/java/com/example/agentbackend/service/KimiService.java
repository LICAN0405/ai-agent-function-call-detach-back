package com.example.agentbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class KimiService {

    private final RestClient restClient;

    @Value("${kimi.api-key}")
    private String apiKey;

    public KimiService(RestClient.Builder builder) {
        // 用 builder 创建一个默认访问 https://api.moonshot.cn 的 HTTP 客户端
        this.restClient = builder
                .baseUrl("https://api.moonshot.cn")
                .build();
    }

    public Map<String, Object> chat(Map<String, Object> requestBody) {
        return restClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()//发送请求并拿到返回的数据
                .body(Map.class);//将kimi返回的json格式数据转化为Map<String, Object>格式数据
    }
}