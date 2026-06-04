package com.example.agentbackend.controller;
//前端调用大模型请求接口
import com.example.agentbackend.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "http://localhost:3000")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public List<Map<String, Object>> chat(@RequestBody Map<String, Object> requestBody) {
        return agentService.process(requestBody);
    }
}
