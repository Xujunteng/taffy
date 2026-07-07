package com.taffy.service;

import com.taffy.entity.Script;
import com.taffy.mapper.ScriptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScriptService {

    @Autowired
    private ScriptMapper scriptMapper;

    public List<Script> getScriptList(Long userId, String category) {
        if (category == null || category.isEmpty()) {
            return scriptMapper.findByUserId(userId);
        }
        return scriptMapper.findByUserIdAndCategory(userId, category);
    }

    public Script getScriptById(Long id) {
        return scriptMapper.findById(id);
    }

    public Script createScript(Script script) {
        LocalDateTime now = LocalDateTime.now();
        script.setCreatedAt(now);
        script.setUpdatedAt(now);
        scriptMapper.insert(script);
        return script;
    }

    public void updateScript(Script script) {
        script.setUpdatedAt(LocalDateTime.now());
        scriptMapper.update(script);
    }

    public void deleteScript(Long id) {
        scriptMapper.delete(id);
    }
}
