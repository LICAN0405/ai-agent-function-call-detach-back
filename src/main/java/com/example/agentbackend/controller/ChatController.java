package com.example.agentbackend.controller;

import com.example.agentbackend.service.KimiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final KimiService kimiService;

    public ChatController(KimiService kimiService) {
        this.kimiService = kimiService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> requestBody) {
        return kimiService.chat(requestBody);
    }
}