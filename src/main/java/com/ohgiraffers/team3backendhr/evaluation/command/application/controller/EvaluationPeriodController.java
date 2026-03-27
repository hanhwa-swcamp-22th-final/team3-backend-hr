package com.ohgiraffers.team3backendhr.evaluation.command.application.controller;

import com.ohgiraffers.team3backendhr.evaluation.command.application.dto.request.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.evaluation.command.application.dto.request.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.evaluation.command.application.service.EvaluationPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation-periods")
@RequiredArgsConstructor
public class EvaluationPeriodController {

    private final EvaluationPeriodService service;

    @PostMapping
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<Void> create(@RequestBody EvaluationPeriodCreateRequest request) {
        service.create(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<Void> close(@PathVariable Long id) {
        service.close(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<Void> confirm(@PathVariable Long id) {
        service.confirm(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody EvaluationPeriodUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.ok().build();
    }
}
