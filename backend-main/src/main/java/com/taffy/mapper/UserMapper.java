package com.taffy.mapper;

import com.taffy.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO users (username, password_hash, email, role, created_at, updated_at) " +
            "VALUES (#{username}, #{passwordHash}, #{email}, #{role}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}