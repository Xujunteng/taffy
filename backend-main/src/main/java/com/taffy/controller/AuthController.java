package com.taffy.controller;

import com.taffy.common.Result;
import com.taffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        if (username == null || password == null) {
            return Result.error(400, "用户名和密码不能为空");
        }
        try {
            userService.register(username, password, email);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.error(400, "用户名和密码不能为空");
        }
        try {
            Map<String, Object> data = userService.login(username, password);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }

    @GetMapping("/userinfo")
    public Result<?> getUserInfo(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
}