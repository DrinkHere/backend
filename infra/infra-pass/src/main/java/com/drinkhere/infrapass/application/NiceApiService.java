package com.drinkhere.infrapass.application;

import com.drinkhere.infrapass.config.NiceApiProperties;
import com.drinkhere.infrapass.dto.request.GetEncTokenRequest;
import com.drinkhere.infrapass.dto.request.NiceApiRequestData;
import com.drinkhere.infrapass.dto.response.CreateNiceApiRequestDataDto;
import com.drinkhere.infrapass.dto.response.GetEncTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NiceApiService {
    private final NiceApiProperties niceApiProperties;
    private final WebClient niceApiWebClient;

    public CreateNiceApiRequestDataDto startPassAuthentication() {
        try {
            // 1. Header 및 Body 값 세팅
            String authorization = "bearer " + createAuthorizationHeader(niceApiProperties.getOrganizationToken(), niceApiProperties.getClientId());
            String productId = niceApiProperties.getProductId();
            String reqDtim = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String reqNo = UUID.randomUUID().toString().substring(0, 30);
            GetEncTokenRequest.DataHeader dataHeader = new GetEncTokenRequest.DataHeader("ko");
            GetEncTokenRequest.DataBody dataBody = new GetEncTokenRequest.DataBody(reqDtim, reqNo, "1");
            GetEncTokenRequest requestBody = new GetEncTokenRequest(dataHeader, dataBody);

            // 2. 암호화 토큰 요청
            GetEncTokenResponse response = niceApiWebClient.post()
                    .uri("/digital/niceid/api/v1.0/common/crypto/token")
                    .header("Authorization", authorization)
                    .header("ProductID", productId)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GetEncTokenResponse.class)  // 응답을 GetEncTokenResponse 객체로 변환
                    .block();  // 비동기적으로 요청하고 응답을 기다림 (블록킹)

            if (response == null || response.dataBody() == null) {
                throw new RuntimeException("Response data is null");
            }

            String tokenVal = response.dataBody().tokenVal(); // 암호화 토큰
            String value = reqDtim.trim() + reqNo.trim() + tokenVal.trim();
            String sha256Base64 = generateSha256Base64(value);

            // 3. 암호화 토큰 발급 시 사용된 요청 및 응답 데이터로 key, iv, hmac_key 생성
            String key = sha256Base64.substring(0, 16); // 앞에서부터 16byte
            String iv = sha256Base64.substring(sha256Base64.length() - 16); // 뒤에서부터 16byte
            String hmacKey = sha256Base64.substring(0, 32); // 앞에서부터 32byte

            // 4.요청 데이터 암호화(encData) 및 Hmac무결성체크값(integrityValue) 생성
            String reqData = createReqDataJson(reqNo, response.dataBody().siteCode());
            String encData = encryptRequestData(key, iv, reqData);
            byte[] hmacSha256 = hmac256(hmacKey.getBytes(), encData.getBytes());
            String integrityValue = Base64.getEncoder().encodeToString(hmacSha256);

            // 5. 표준창 호출 시 필요한 tokenVersionId, encData, integrityValue 반환
            return new CreateNiceApiRequestDataDto(
                    response.dataBody().tokenVersionId(),
                    encData,
                    integrityValue
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process Pass Authentication", e);
        }
    }

    public static String createAuthorizationHeader(String accessToken, String clientId) {
        try {
            long currentTimestamp = new Date().getTime() / 1000;
            String dataToEncode = accessToken + ":" + currentTimestamp + ":" + clientId;
            String encodedData = Base64.getEncoder().encodeToString(dataToEncode.getBytes(StandardCharsets.UTF_8));
            return "bearer " + encodedData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create authorization header", e);
        }
    }

    private String generateSha256Base64(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes());
            byte[] arrHashValue = md.digest();
            return Base64.getEncoder().encodeToString(arrHashValue);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public String encryptRequestData(String key, String iv, String reqData) {
        try {
            SecretKey secureKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(reqData.trim().getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt request data", e);
        }
    }

    public static byte[] hmac256(byte[] secretKey, byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec sks = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(sks);
            return mac.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMACSHA256 encrypt", e);
        }
    }

    private String createReqDataJson(String reqNo, String siteCode) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NiceApiRequestData requestData = new NiceApiRequestData(
                reqNo,
                "https://drinkhere.store/api/v1/public/nice/call-back",
                siteCode,
                "Y"
        );
        return objectMapper.writeValueAsString(requestData);
    }

}
