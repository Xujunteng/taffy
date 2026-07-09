package com.taffy.controller;

import com.taffy.common.Result;
import com.taffy.entity.User;
import com.taffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * 校验规则：
     * - 用户名 3~20 字符，只允许字母数字下划线
     * - 密码至少 6 位
     * - 邮箱格式基本校验（可选）
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");

        // 参数非空校验
        if (username == null || username.isBlank()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.error(400, "密码不能为空");
        }

        // 用户名格式校验
        if (username.length() < 3 || username.length() > 20) {
            return Result.error(400, "用户名长度必须在3-20个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return Result.error(400, "用户名只能包含字母、数字和下划线");
        }

        // 密码强度校验
        if (password.length() < 6) {
            return Result.error(400, "密码长度至少6位");
        }

        // 邮箱格式校验（如果提供）
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return Result.error(400, "邮箱格式不正确");
            }
        }

        try {
            userService.register(username, password, email);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.isBlank()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.error(400, "密码不能为空");
        }
        try {
            Map<String, Object> data = userService.login(username, password);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body,
                                    jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (oldPwd == null || oldPwd.isBlank() || newPwd == null || newPwd.isBlank()) {
            return Result.error(400, "新旧密码均不能为空");
        }
        if (newPwd.length() < 6) return Result.error(400, "新密码长度至少6位");
        try {
            userService.changePassword(userId, oldPwd, newPwd);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 获取或生成用户的 API Key（永久有效） */
    @GetMapping("/apikey")
    public Result<?> getApiKey(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "未登录");
        return Result.success(Map.of("apiKey", userService.getOrGenerateApiKey(userId)));
    }

    /**
     * 获取当前登录用户信息
     * 返回字段：id, username, email, role
     * 不返回密码哈希
     */
    @GetMapping("/userinfo")
    public Result<?> getUserInfo(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getUserInfo(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }
}
