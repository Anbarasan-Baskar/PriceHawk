
package com.pricehawk.backend.controller;

import com.pricehawk.backend.service.CompareService;
import com.pricehawk.backend.payload.response.CompareResponse;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/compare")
@CrossOrigin("*")
public class CompareController {

    private final CompareService service;

    public CompareController(CompareService service) {
        this.service = service;
    }

    @GetMapping("/instant")
    public CompareResponse compare(@RequestParam String title) {
        return service.compareByTitle(title);
    }
}
