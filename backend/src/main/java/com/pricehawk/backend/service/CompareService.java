package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.payload.response.CompareResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
@Service
public class CompareService {

    private final RestTemplate restTemplate = new RestTemplate();

    public CompareResponse compareByTitle(String title) {

        String url = "http://localhost:8000/instant-compare";

        Map<String,String> req = Map.of("title", title);

        return restTemplate.postForObject(url, req, CompareResponse.class);
    }
}
