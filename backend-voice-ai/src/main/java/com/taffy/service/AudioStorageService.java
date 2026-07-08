package com.taffy.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 音频文件存储服务接口
 */
public interface AudioStorageService {
    /**
     * 保存上传的音频文件
     * @param file 上传的音频文件
     * @param userId 用户ID
     * @return 文件的存储路径
     */
    String saveAudio(MultipartFile file, Long userId) throws Exception;
}
