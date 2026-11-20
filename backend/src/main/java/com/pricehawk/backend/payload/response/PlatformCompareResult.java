package com.pricehawk.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformCompareResult {
    private List<CompareItem> best = new ArrayList<>();
    private CompareItem lowest;
}
