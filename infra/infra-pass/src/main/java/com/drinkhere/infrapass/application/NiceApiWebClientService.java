package com.drinkhere.infrapass.application;

import com.drinkhere.infrapass.config.NiceApiProperties;
import com.drinkhere.infrapass.dto.request.GetCryptoTokenRequest;
import com.drinkhere.infrapass.dto.response.GetCryptoTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 실제 WebClient를 통해 NICE API 호출
 */
@Service
@RequiredArgsConstructor
public class NiceApiWebClientService {
    private final WebClient niceApiWebClient;
    private final NiceApiProperties niceApiProperties;

    /**
     * GetCryptoToken 요청을 보내는 메서드
     */
    public GetCryptoTokenResponse requestCryptoToken(String reqDtim, String reqNo) {
        String authorization = createAuthorizationHeader(
                niceApiProperties.getOrganizationToken(),
                niceApiProperties.getClientId()
        );

        String productId = niceApiProperties.getProductId();


        // 요청 헤더와 바디 생성
        GetCryptoTokenRequest requestBody = createCryptoTokenRequest(reqDtim, reqNo);

        // WebClient를 이용한 요청 전송
        return niceApiWebClient.post()
                .uri("/digital/niceid/api/v1.0/common/crypto/token")
                .header("Authorization", authorization)
                .header("ProductID", productId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GetCryptoTokenResponse.class)
                .block();  // 블록킹 방식으로 응답 대기
    }

    private GetCryptoTokenRequest createCryptoTokenRequest(String reqDtim, String reqNo) {
        GetCryptoTokenRequest.DataHeader dataHeader = new GetCryptoTokenRequest.DataHeader("ko");
        GetCryptoTokenRequest.DataBody dataBody = new GetCryptoTokenRequest.DataBody(reqDtim, reqNo, "1");
        return new GetCryptoTokenRequest(dataHeader, dataBody);
    }

    private String createAuthorizationHeader(String accessToken, String clientId) {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        String dataToEncode = accessToken + ":" + currentTimestamp + ":" + clientId;
        return "bearer " + java.util.Base64.getEncoder()
                .encodeToString(dataToEncode.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
