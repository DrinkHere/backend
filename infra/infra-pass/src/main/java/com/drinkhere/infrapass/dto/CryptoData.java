package com.drinkhere.infrapass.dto;

public record CryptoData(
        String key,
        String iv,
        String hmacKey
) {
    public static CryptoData of(String key, String iv, String hmacKey) {
        return new CryptoData(key, iv, hmacKey);
    }
}
