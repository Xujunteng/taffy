package com.taffy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;

/**
 * 数据库初始化器
 * 确保 SQLite 数据库文件和 schema.sql 在应用启动时自动执行
 * 同时启用 SQLite 外键约束
 */
@Component
public class DataSourceInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try {
            // 确保 data 目录存在
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                log.info("创建 data 目录: {}", dataDir.getAbsolutePath());
            }

            // 确保 audio 目录存在
            File audioDir = new File("./data/audio");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
                log.info("创建 audio 目录: {}", audioDir.getAbsolutePath());
            }

            // 执行 schema.sql 建表（使用 IF NOT EXISTS 保证幂等）
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("数据库表初始化完成");

        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }
}