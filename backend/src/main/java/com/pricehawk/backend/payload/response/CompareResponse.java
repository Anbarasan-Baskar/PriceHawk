package com.pricehawk.backend.payload.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CompareResponse {

    private String bestPlatform;
    private double bestPrice;
    private String bestTitle;
    private double confidence;
    private List<Map<String,Object>> results;

}
