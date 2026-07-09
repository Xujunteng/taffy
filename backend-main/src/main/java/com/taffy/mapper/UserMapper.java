package com.taffy.mapper;

import com.taffy.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO users (username, password_hash, email, role, created_at, updated_at) " +
            "VALUES (#{username}, #{passwordHash}, #{email}, #{role}, #{createdAt}, #{updatedAt})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(User user);

    @Update("UPDATE users SET password_hash = #{newHash} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("newHash") String newHash);

    @Update("UPDATE users SET api_key = #{apiKey} WHERE id = #{id}")
    int updateApiKey(@Param("id") Long id, @Param("apiKey") String apiKey);

    @Select("SELECT * FROM users WHERE api_key = #{apiKey}")
    User findByApiKey(String apiKey);
}