package com.drinkhere.infrapass.dto.response;

/**
 * Nice API 표준창 호출 위한 데이터 프론트로 반환
 */
public record CreateNiceApiRequestDataDto(
        String tokenVersionId,
        String encData,
        String integrityValue
) {
    public static CreateNiceApiRequestDataDto of(String tokenVersionId, String encData, String integrityValue) {
        return new CreateNiceApiRequestDataDto(tokenVersionId, encData, integrityValue);
    }
}
