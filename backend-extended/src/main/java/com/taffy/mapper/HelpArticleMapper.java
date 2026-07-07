package com.taffy.mapper;

import com.taffy.entity.HelpArticle;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HelpArticleMapper {

    @Select("SELECT * FROM help_articles ORDER BY sort_order, created_at DESC")
    List<HelpArticle> findAll();

    @Select("SELECT * FROM help_articles WHERE category = #{category} ORDER BY sort_order, created_at DESC")
    List<HelpArticle> findByCategory(String category);

    @Select("SELECT * FROM help_articles WHERE id = #{id}")
    HelpArticle findById(Long id);

    @Select("SELECT * FROM help_articles WHERE title LIKE '%' || #{keyword} || '%' OR content LIKE '%' || #{keyword} || '%' ORDER BY sort_order, created_at DESC")
    List<HelpArticle> search(String keyword);

    @Insert("INSERT INTO help_articles (title, content, category, sort_order, created_at) " +
            "VALUES (#{title}, #{content}, #{category}, #{sortOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpArticle article);
}
