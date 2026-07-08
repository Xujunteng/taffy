package com.taffy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM help_articles", Integer.class);
            if (count != null && count == 0) {
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "快速入门",
                "本平台使用流程——注册→创建声音→上传音频→训练→TTS合成→直播",
                "入门指南",
                1
            );
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "如何录制优质音频样本",
                "安静环境、好麦克风、自然语速、5-15分钟、WAV/MP3/M4A",
                "入门指南",
                2
            );
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "TTS参数调整说明",
                "语速0.5-2.0、音调0.5-2.0、音量0-100",
                "功能说明",
                3
            );
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "OBS直播配置教程",
                "OBS开启WebSocket→设置端口密码→平台输入信息→连接→发送文本",
                "功能说明",
                4
            );
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "如何进行声音评价",
                "选择声音→打分1-5星→写评论→提交",
                "入门指南",
                5
            );
            jdbcTemplate.update(
                "INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
                "常见问题FAQ",
                "训练5-15分钟/支持中文/文件限50MB/多直播独立会话",
                "常见问题",
                6
            );
            System.out.println("DataInitializer: 已插入6篇帮助文章(种子数据)");
        } else {
            System.out.println("DataInitializer: 帮助文章表已存在" + count + "条数据,跳过种子插入");
            }
        } catch (Exception e) {
            System.out.println("DataInitializer: 数据库未就绪,跳过种子数据插入 - " + e.getMessage());
        }
    }
}
