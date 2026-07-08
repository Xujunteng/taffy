package com.taffy.controller;

import com.taffy.entity.HelpArticle;
import com.taffy.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/help")
public class HelpController {

    @Autowired
    private HelpService helpService;

    @GetMapping("/articles")
    public Map<String, Object> list(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        List<HelpArticle> articles = helpService.getArticles(category);
        result.put("code", 200);
        result.put("data", articles);
        return result;
    }

    @GetMapping("/articles/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        HelpArticle article = helpService.getArticleById(id);
        if (article == null) {
            result.put("code", 404);
            result.put("message", "文章不存在");
            result.put("data", null);
        } else {
            result.put("code", 200);
            result.put("data", article);
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "搜索关键词不能为空");
            return result;
        }
        List<HelpArticle> articles = helpService.searchArticles(keyword.trim());
        result.put("code", 200);
        result.put("data", articles);
        return result;
    }
}
