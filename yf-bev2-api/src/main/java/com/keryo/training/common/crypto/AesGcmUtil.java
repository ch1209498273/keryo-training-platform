package com.keryo.training.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加解密——手机号存储
 * 格式: Base64(IV[12B] || ciphertext[~16B] || tag[16B])
 */
public final class AesGcmUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_LEN = 128;

    private AesGcmUtil() {}

    public static String encrypt(String plainText, byte[] keyBytes) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[GCM_IV_LEN];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LEN, iv));
        byte[] cipherText = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] combined = new byte[GCM_IV_LEN + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, GCM_IV_LEN);
        System.arraycopy(cipherText, 0, combined, GCM_IV_LEN, cipherText.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedB64, byte[] keyBytes) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedB64);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[GCM_IV_LEN];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
        byte[] cipherText = new byte[combined.length - GCM_IV_LEN];
        System.arraycopy(combined, GCM_IV_LEN, cipherText, 0, cipherText.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LEN, iv));
        return new String(cipher.doFinal(cipherText), java.nio.charset.StandardCharsets.UTF_8);
    }

    /** 生成 AES-256 密钥（Base64 编码），运行一次后存入 .env */
    public static String generateKeyB64() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256);
        return Base64.getEncoder().encodeToString(gen.generateKey().getEncoded());
    }
}

