package com.tuapp.grocery_backend.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.tuapp.grocery_backend.service.AssistantService;
import com.tuapp.grocery_backend.dto.ChatRequest;
import com.tuapp.grocery_backend.dto.ChatResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ChatController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = assistantService.chat(
            request.getMessage(),
            request.getUsuario()
        );
        return new ChatResponse(reply);
    }
}