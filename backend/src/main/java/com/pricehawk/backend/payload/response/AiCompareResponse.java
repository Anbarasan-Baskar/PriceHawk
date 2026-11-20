package com.pricehawk.backend.payload.response;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AiCompareResponse {
    private Map<String, List<CompareItem>> platforms = new LinkedHashMap<>();
    private String error;
    private Object errors;
}
