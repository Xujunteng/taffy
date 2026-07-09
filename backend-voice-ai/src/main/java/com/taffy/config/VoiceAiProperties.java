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

    /** edge-tts 配置（微软Edge神经网络语音，免费高质量兜底） */
    private EdgeTts edgeTts = new EdgeTts();

    @Data
    public static class EdgeTts {
        /** edge-tts 命令路径，默认 "edge-tts" */
        private String command = "auto";
        /** 默认声音: auto(根据性别自动) / zh-CN-XiaoxiaoNeural / zh-CN-YunxiNeural 等 */
        private String voice = "auto";
    }

    @Data
    public static class Aliyun {
        /** 是否启用阿里云真实TTS（false时使用本地TTS） */
        private boolean enabled;
        /** 阿里云 AccessKey ID */
        private String accessKeyId;
        /** 阿里云 AccessKey Secret */
        private String accessKeySecret;
        /** 阿里云语音服务 AppKey */
        private String appKey;
        /** 阿里云语音服务 Endpoint */
        private String endpoint;
        /** 默认发音人（xiaoyun/xiaogang/ruoxi/sitong等） */
        private String voiceName = "xiaoyun";
        /** 音频采样率 */
        private int sampleRate = 16000;
        /** 音频格式（wav/pcm/mp3） */
        private String format = "wav";
    }
}
