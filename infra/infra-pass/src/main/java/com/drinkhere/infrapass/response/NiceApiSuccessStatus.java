package com.drinkhere.infrapass.response;

import com.drinkhere.common.response.BaseSuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NiceApiSuccessStatus implements BaseSuccessStatus {
    START_PASS_AUTHENTICATION_SUCCESS(HttpStatus.OK, "패스 본인인증 로직 시작");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
