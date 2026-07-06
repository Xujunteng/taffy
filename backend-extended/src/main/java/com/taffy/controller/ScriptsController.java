package com.taffy.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/scripts")
public class ScriptsController {

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", null);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}