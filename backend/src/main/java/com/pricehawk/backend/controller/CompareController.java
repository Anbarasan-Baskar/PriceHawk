package com.pricehawk.backend.controller;

import com.pricehawk.backend.service.CompareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/compare")
@CrossOrigin("*")
public class CompareController {

    private final CompareService service;

    public CompareController(CompareService service) {
        this.service = service;
    }

    @GetMapping("/instant")
    public ResponseEntity<Map<String, Object>> compare(
            @RequestParam String title,
            @RequestParam(required = false) Long productId) {
        Map<String, Object> result = service.compareByTitle(title, productId);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
