package com.drinkhere.infrapass.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NiceApiRequestData(
        @JsonProperty("requestno") String requestNo,
        @JsonProperty("returnurl") String returnUrl,
        @JsonProperty("sitecode") String siteCode,
        @JsonProperty("popupyn") String popupYn
//        @JsonProperty("receivedata") String receiveData
) {
}
