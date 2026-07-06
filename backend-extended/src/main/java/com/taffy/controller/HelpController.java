package com.taffy.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/help")
public class HelpController {

    @GetMapping("/articles")
    public Map<String, Object> list(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }

    @GetMapping("/articles/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", null);
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }
}