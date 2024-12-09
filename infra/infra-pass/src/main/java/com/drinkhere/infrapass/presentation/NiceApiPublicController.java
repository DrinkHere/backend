package com.drinkhere.infrapass.presentation;

import com.drinkhere.common.response.ApiResponse;
import com.drinkhere.infrapass.application.NiceApiService;
import com.drinkhere.infrapass.dto.response.CreateNiceApiRequestDataDto;
import com.drinkhere.infrapass.response.NiceApiSuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.drinkhere.infrapass.response.NiceApiSuccessStatus.START_PASS_AUTHENTICATION_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/nice")
public class NiceApiPublicController {
    private final NiceApiService niceApiService;

    @GetMapping
    public ResponseEntity<ApiResponse<CreateNiceApiRequestDataDto>> startPassAuthentication() {
        return ApiResponse.success(START_PASS_AUTHENTICATION_SUCCESS, niceApiService.startPassAuthentication());
    }
}
