package com.taffy.mapper;

import com.taffy.entity.HelpArticle;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

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
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(HelpArticle article);
}
