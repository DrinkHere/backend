package com.drinkhere.infrapass.application;

import com.drinkhere.infrapass.dto.CryptoData;
import com.drinkhere.infrapass.dto.DecryptedData;
import com.drinkhere.infraredis.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class NiceApiAuthenticationHandler {
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public void handleAuthenticationResult(Long memberId, String encData) {
        try {
            // Redis에서 저장된 key, iv, hmacKey를 가져옴
            String cryptoDataJson = (String) redisUtil.get("cryptoData");

            if (cryptoDataJson == null) {
                throw new IllegalStateException("CryptoData not found in Redis");
            }

            // JSON 문자열을 CryptoData 객체로 변환
            CryptoData cryptoData = objectMapper.readValue(cryptoDataJson, CryptoData.class);

            String key = cryptoData.key();
            String iv = cryptoData.iv();

            // encData 복호화
            String decryptedData = decryptData(encData, key, iv);
            DecryptedData result = objectMapper.readValue(decryptedData, DecryptedData.class);

            String requestNoFromRedis = (String) redisUtil.get("memberId:" + memberId + ":requestNo");

            if (result.requestNo() != requestNoFromRedis) {
                throw new IllegalStateException("RequestNo mismatch: " + result.requestNo() + " != " + requestNoFromRedis);
            }


            // TODO : 복호화된 인증 결과 데이터를 상황에 맞게 데이터베이스에 저장
        } catch (Exception e) {
            // 예외 처리 (예: 복호화 실패, Redis 조회 실패 등)
            System.err.println("Error handling authentication result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 복호화 메서드
    private String decryptData(String encData, String key, String iv) throws Exception {
        // AES 복호화 알고리즘 사용
        SecretKey secureKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));

        byte[] decodedData = Base64.getDecoder().decode(encData);
        byte[] decryptedData = cipher.doFinal(decodedData);

        return new String(decryptedData, "UTF-8"); // 복호화된 데이터 반환
    }

}
