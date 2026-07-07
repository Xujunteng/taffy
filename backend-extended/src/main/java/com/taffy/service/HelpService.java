package com.taffy.service;

import com.taffy.entity.HelpArticle;
import com.taffy.mapper.HelpArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpService {

    @Autowired
    private HelpArticleMapper helpArticleMapper;

    public List<HelpArticle> getArticles(String category) {
        if (category == null || category.isEmpty()) {
            return helpArticleMapper.findAll();
        }
        return helpArticleMapper.findByCategory(category);
    }

    public HelpArticle getArticleById(Long id) {
        return helpArticleMapper.findById(id);
    }

    public List<HelpArticle> searchArticles(String keyword) {
        return helpArticleMapper.search(keyword);
    }
}
