package com.gradproject.controller;

import com.gradproject.entity.Message;
import com.gradproject.entity.User;
import com.gradproject.service.MessageService;
import com.gradproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getMessages(@PathVariable Long groupId,
                                         @RequestParam(required = false) Long after,
                                         Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            List<Message> messages = messageService.getMessages(groupId, user, after);
            return ResponseEntity.ok(messages.stream().map(this::mapMessage).toList());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/group/{groupId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long groupId,
                                         @RequestBody Map<String, String> body,
                                         Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            Message msg = messageService.send(groupId, user, body.get("content"));
            return ResponseEntity.ok(mapMessage(msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> mapMessage(Message m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("content", m.getContent());
        map.put("sentAt", m.getSentAt().toString());
        map.put("senderId", m.getSender().getId());
        map.put("senderName", m.getSender().getName());
        map.put("senderRole", m.getSender().getRole().name());
        return map;
    }
}
