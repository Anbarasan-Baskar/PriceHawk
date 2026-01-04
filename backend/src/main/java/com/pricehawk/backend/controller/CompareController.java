
package com.pricehawk.backend.controller;

import com.pricehawk.backend.service.CompareService;
import com.pricehawk.backend.payload.response.CompareResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CompareController {

    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @GetMapping("/compare")
    public CompareResponse compare(
            @RequestParam String platform,
            @RequestParam String platformId
    ) {
        return compareService.compare(platform, platformId);
    }
}
