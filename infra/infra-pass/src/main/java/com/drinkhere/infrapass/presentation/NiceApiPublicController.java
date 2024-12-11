package com.drinkhere.infrapass.presentation;

import com.drinkhere.common.response.ApiResponse;
import com.drinkhere.infrapass.application.NiceApiAuthenticationHandler;
import com.drinkhere.infrapass.application.NiceApiService;
import com.drinkhere.infrapass.dto.response.CreateNiceApiRequestDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.drinkhere.infrapass.response.NiceApiSuccessStatus.HANDLE_AUTHENTICATION_RESULT_SUCCESS;
import static com.drinkhere.infrapass.response.NiceApiSuccessStatus.START_PASS_AUTHENTICATION_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/nice")
public class NiceApiPublicController {
    private final NiceApiService niceApiService;
    private final NiceApiAuthenticationHandler niceApiAuthenticationHandler;
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<CreateNiceApiRequestDataDto>> startPassAuthentication(@PathVariable(value = "memberId") Long memberId) {
        return ApiResponse.success(START_PASS_AUTHENTICATION_SUCCESS, niceApiService.startPassAuthentication(memberId));
    }

    @GetMapping("/call-back")
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationResult(
            @RequestParam("memberId") Long memberId,
            @RequestParam("enc_data") String encData
    ) {
        niceApiAuthenticationHandler.handleAuthenticationResult(memberId, encData);
        return ApiResponse.success(HANDLE_AUTHENTICATION_RESULT_SUCCESS);
    }

}
