package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.SportRequest;
import com.fifa.fifarest.dto.SportResponse;
import com.fifa.fifarest.service.SportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
public class SportController {

    private final SportService sportService;

    public SportController(SportService sportService) {
        this.sportService = sportService;
    }

    @GetMapping
    public ResponseEntity<List<SportResponse>> list() {
        return ResponseEntity.ok(sportService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SportResponse> create(@Valid @RequestBody SportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sportService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SportResponse> update(@PathVariable Long id, @Valid @RequestBody SportRequest request) {
        return ResponseEntity.ok(sportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
