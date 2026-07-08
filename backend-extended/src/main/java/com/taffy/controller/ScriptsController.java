package com.taffy.controller;

import com.taffy.entity.Script;
import com.taffy.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/scripts")
public class ScriptsController {

    @Autowired
    private ScriptService scriptService;

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String category, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Script> scripts = scriptService.getScriptList(userId, category);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", scripts);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Script script = scriptService.getScriptById(id);
        Map<String, Object> result = new HashMap<>();
        if (script == null) {
            result.put("code", 404);
            result.put("message", "脚本不存在");
            result.put("data", null);
        } else {
            result.put("code", 200);
            result.put("data", script);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Script script, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        if (script.getTitle() == null || script.getTitle().trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "脚本标题不能为空");
            return result;
        }
        script.setUserId(userId);
        Script created = scriptService.createScript(script);
        result.put("code", 200);
        result.put("message", "脚本创建成功");
        result.put("data", created);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Script script) {
        Map<String, Object> result = new HashMap<>();
        if (scriptService.getScriptById(id) == null) {
            result.put("code", 404);
            result.put("message", "脚本不存在");
            return result;
        }
        script.setId(id);
        scriptService.updateScript(script);
        result.put("code", 200);
        result.put("message", "脚本更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        if (scriptService.getScriptById(id) == null) {
            result.put("code", 404);
            result.put("message", "脚本不存在");
            return result;
        }
        scriptService.deleteScript(id);
        result.put("code", 200);
        result.put("message", "脚本删除成功");
        return result;
    }
}
