package com.navtex.controller;

import com.navtex.model.NavtexMessage;
import com.navtex.service.NavtexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NavtexController {

    private final NavtexService navtexService;

    public NavtexController(NavtexService navtexService) {
        this.navtexService = navtexService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<NavtexMessage>> getAllMessages() {
        return ResponseEntity.ok(navtexService.getAllMessages());
    }

    @GetMapping("/messages/country/{country}")
    public ResponseEntity<List<NavtexMessage>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(navtexService.getMessagesByCountry(country));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "navtex-api"));
    }
}