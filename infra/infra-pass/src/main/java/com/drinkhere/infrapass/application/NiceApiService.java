package com.drinkhere.infrapass.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class NiceApiService {
    private final WebClient webClient;


}
