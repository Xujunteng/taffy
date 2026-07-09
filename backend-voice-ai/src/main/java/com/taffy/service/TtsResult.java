package com.taffy.service;

/**
 * TTS 合成结果，包含音频数据和供应商信息。
 */
public class TtsResult {
    private final byte[] audio;
    private final String provider;      // "aliyun" / "edge-tts"
    private final String providerNote;  // 可选备注

    public TtsResult(byte[] audio, String provider, String providerNote) {
        this.audio = audio;
        this.provider = provider;
        this.providerNote = providerNote;
    }

    public byte[] getAudio() { return audio; }
    public String getProvider() { return provider; }
    public String getProviderNote() { return providerNote; }
}
