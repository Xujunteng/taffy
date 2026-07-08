package com.taffy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 语音AI服务配置属性
 * 映射 application.yml 中的 voice-ai 配置段
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "voice-ai")
public class VoiceAiProperties {
    /** 音频文件存储根目录 */
    private String storageRoot;

    /** 阿里云语音服务配置 */
    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Aliyun {
        /** 是否启用阿里云真实TTS（false时使用Mock占位） */
        private boolean enabled;
        /** 阿里云 AccessKey ID */
        private String accessKeyId;
        /** 阿里云 AccessKey Secret */
        private String accessKeySecret;
        /** 阿里云语音服务 AppKey */
        private String appKey;
        /** 阿里云语音服务 Endpoint */
        private String endpoint;
    }
}
