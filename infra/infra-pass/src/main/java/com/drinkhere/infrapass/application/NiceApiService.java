package com.drinkhere.infrapass.application;

import com.drinkhere.infrapass.config.NiceApiProperties;
import com.drinkhere.infrapass.dto.CryptoData;
import com.drinkhere.infrapass.dto.request.GetCryptoTokenRequest;
import com.drinkhere.infrapass.dto.request.NiceApiRequestData;
import com.drinkhere.infrapass.dto.response.CreateNiceApiRequestDataDto;
import com.drinkhere.infrapass.dto.response.GetCryptoTokenResponse;
import com.drinkhere.infraredis.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class NiceApiService {
    private final NiceApiWebClientService niceApiWebClientService;
    private final RedisUtil redisUtil;

    public CreateNiceApiRequestDataDto startPassAuthentication(Long memberId) {
        try {
            GetCryptoTokenResponse cryptoToken;
            ObjectMapper objectMapper = new ObjectMapper();
            String key = null;
            String iv = null;
            String hmacKey = null;

            String storedJson = (String) redisUtil.get("cryptoToken");

            if (storedJson != null) { // 암호화 토큰 만료 X
                cryptoToken = objectMapper.readValue(storedJson, GetCryptoTokenResponse.class);

                // 기존에 저장된 대칭키(key,iv) 및 무결성키(hmac_key) 조회
                String storedCryptoData = (String) redisUtil.get("cryptoData");
                if (storedCryptoData != null) {
                    CryptoData cryptoData = objectMapper.readValue(storedCryptoData, CryptoData.class);
                    key = cryptoData.key();
                    iv = cryptoData.iv();
                    hmacKey = cryptoData.hmacKey();
                }

            } else { // 암호화 토큰 만료 O
                String reqDtim = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String reqNo = UUID.randomUUID().toString().substring(0, 30);
                cryptoToken = niceApiWebClientService.requestCryptoToken(reqDtim, reqNo); // WebClient로 NICE API 암호화 토큰 발급 호출

                if (cryptoToken == null || cryptoToken.dataBody() == null) {
                    throw new RuntimeException("Response data is null");
                }
                // 발급받은 응답 값 Redis에 저장
                String jsonValue = objectMapper.writeValueAsString(cryptoToken); // Java 객체 → JSON 문자열 (직렬화)
                redisUtil.saveAsValue("cryptoToken", jsonValue, 3300L, SECONDS);

                // 3. 암호화 토큰 발급 시 사용된 요청 및 응답 데이터로 key, iv, hmac_key 생성 -> 유저마다 redis에 저장하자
                String tokenVal = cryptoToken.dataBody().tokenVal(); // 암호화 토큰
                String value = reqDtim.trim() + reqNo.trim() + tokenVal.trim();
                String resultVal = generateResultVal(value);

                // 새로 발급받은 암호화 토큰으로 대칭키(key,iv) 및 무결성키(hmac_key) 생성
                key = resultVal.substring(0, 16);
                iv = resultVal.substring(resultVal.length() - 16);
                hmacKey = resultVal.substring(0, 32);

                CryptoData cryptoData = CryptoData.of(key, iv, hmacKey);
                String cryptoDataJson = objectMapper.writeValueAsString(cryptoData);
                redisUtil.saveWithoutExpiration("cryptoData", cryptoDataJson);
            }

            // 4. 요청 데이터 암호화(encData) 및 Hmac 무결성 체크값(integrityValue) 생성
            String reqData = createReqDataJson(cryptoToken.dataBody().siteCode(), memberId);
            String encData = encryptReqData(key, iv, reqData);
            byte[] hmacSha256 = hmac256(hmacKey.getBytes(), encData.getBytes());
            String integrityValue = Base64.getEncoder().encodeToString(hmacSha256);

            // 5. 표준창 호출 시 필요한 tokenVersionId, encData, integrityValue 반환
            return new CreateNiceApiRequestDataDto(
                    cryptoToken.dataBody().tokenVersionId(),
                    encData,
                    integrityValue
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process Pass Authentication", e);
        }
    }

    private String generateResultVal(String value) {
        // key, iv, hmac_kay 생성하기 위한 resultVal 생성
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes());
            byte[] arrHashValue = md.digest();
            return Base64.getEncoder().encodeToString(arrHashValue);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String createReqDataJson(String siteCode, Long memberId) throws Exception {
        // 요청 데이터 생성
        ObjectMapper objectMapper = new ObjectMapper();
        String requestNo = UUID.randomUUID().toString().substring(0, 30);
        // memberId를 URL에 추가
        String callbackUrl = "https://drinkhere.store/api/v1/public/nice/call-back?mid=" + memberId;
        NiceApiRequestData requestData = new NiceApiRequestData(
                requestNo,
                callbackUrl,
                siteCode,
                "Y"
        );

        // Redis에 requestNo 저장 (key: memberId, value: requestNo, 30분 만료)
        redisUtil.saveAsValue("memberId:" + memberId + ":requestNo", requestNo, 30L, MINUTES);

        return objectMapper.writeValueAsString(requestData);
    }

    public String encryptReqData(String key, String iv, String reqData) {
        // 요청 데이터 암호화
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
        // HMAC-SHA256 계산(hmacSha256):
        //    - 암호화된 데이터(encData)와 비밀 키(hmacKey)를 사용하여 HMAC-SHA256 값을 계산.
        //    - 이 값은 데이터의 무결성을 검증하는 데 사용됨.
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec sks = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(sks);
            return mac.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMACSHA256 encrypt", e);
        }
    }
}
