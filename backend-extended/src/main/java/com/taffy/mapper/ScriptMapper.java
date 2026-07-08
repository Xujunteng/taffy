package com.taffy.mapper;

import com.taffy.entity.Script;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

@Mapper
public interface ScriptMapper {

    @Select("SELECT * FROM scripts WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Script> findByUserId(Long userId);

    @Select("SELECT * FROM scripts WHERE user_id = #{userId} AND category = #{category} ORDER BY created_at DESC")
    List<Script> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    @Select("SELECT * FROM scripts WHERE id = #{id}")
    Script findById(Long id);

    @Insert("INSERT INTO scripts (user_id, title, content, category, created_at, updated_at) " +
            "VALUES (#{userId}, #{title}, #{content}, #{category}, #{createdAt}, #{updatedAt})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(Script script);

    @Update("UPDATE scripts SET title=#{title}, content=#{content}, category=#{category}, updated_at=#{updatedAt} WHERE id=#{id}")
    int update(Script script);

    @Delete("DELETE FROM scripts WHERE id=#{id}")
    int delete(Long id);
}
