package com.drinkhere.infrapass.dto.request;

public record GetEncTokenRequest(
        DataHeader dataHeader,
        DataBody dataBody
) {
    public record DataHeader(String CNTY_CD) {}

    public record DataBody(
            String req_dtim,
            String req_no,
            String enc_mode
    ) {}
}
