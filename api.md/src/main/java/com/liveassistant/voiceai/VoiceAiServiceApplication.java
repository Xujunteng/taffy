package com.liveassistant.voiceai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.liveassistant.voiceai.mapper")
public class VoiceAiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceAiServiceApplication.class, args);
    }
}
