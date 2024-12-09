package com.drinkhere.infrapass.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetEncTokenResponse(
        @JsonProperty("dataHeader") DataHeader dataHeader,
        @JsonProperty("dataBody") DataBody dataBody
) {
    public record DataHeader(
            @JsonProperty("gwRsltCd") String gwRsltCd,
            @JsonProperty("gwRsltMsg") String gwRsltMsg
    ) {}

    public record DataBody(
            @JsonProperty("rspCd") String rspCd,
            @JsonProperty("siteCode") String siteCode,
            @JsonProperty("resultCd") String resultCd,
            @JsonProperty("tokenVersionId") String tokenVersionId,
            @JsonProperty("tokenVal") String tokenVal,
            @JsonProperty("period") int period
    ) {}
}
