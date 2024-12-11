package com.drinkhere.infrapass.response;

import com.drinkhere.common.response.BaseSuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NiceApiSuccessStatus implements BaseSuccessStatus {
    START_PASS_AUTHENTICATION_SUCCESS(HttpStatus.OK, "나이스 API 표준창 호출 시 필요한 데이터 반환 성공"),
    HANDLE_AUTHENTICATION_RESULT_SUCCESS(HttpStatus.OK, "인증결과 처리 성공");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
