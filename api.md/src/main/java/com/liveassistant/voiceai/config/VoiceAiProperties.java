package com.liveassistant.voiceai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "voice-ai")
public class VoiceAiProperties {
    private String storageRoot;
    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Aliyun {
        private boolean enabled;
        private String accessKeyId;
        private String accessKeySecret;
        private String appKey;
        private String endpoint;
    }
}
