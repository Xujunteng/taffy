package com.taffy.service;

import com.taffy.config.JwtUtil;
import com.taffy.entity.User;
import com.taffy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(String username, String password, String email) {
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("user");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generateToken(user.getId(), user.getUsername()));
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return data;
    }

    /** 获取或生成用户的永久 API Key */
    public String getOrGenerateApiKey(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getApiKey() != null && !user.getApiKey().isBlank()) return user.getApiKey();
        String key = "taffy_" + java.util.UUID.randomUUID().toString().replace("-", "");
        userMapper.updateApiKey(userId, key);
        return key;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    public User getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            // 安全：清除密码哈希，不暴露给前端
            user.setPasswordHash(null);
        }
        return user;
    }
}
