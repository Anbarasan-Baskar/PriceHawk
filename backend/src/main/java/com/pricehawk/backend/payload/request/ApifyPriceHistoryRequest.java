package com.pricehawk.backend.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class ApifyPriceHistoryRequest {
    private List<String> identifiers;
    private String country = "us";
    private Boolean includeVariants = false;
    private Boolean streamOutput = true;
}
