package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.mission.MissionAssignmentRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.mission.MissionSeedRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission.MissionAssignmentResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission.MissionSeedResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.MissionAssignmentCommandService;
import com.ohgiraffers.team3backendhr.hr.command.application.service.MissionSeedCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/missions")
@RequiredArgsConstructor
public class MissionSeedController {

    private final MissionSeedCommandService missionSeedCommandService;
    private final MissionAssignmentCommandService missionAssignmentCommandService;

    @PostMapping("/seed")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<MissionSeedResponse>> seedMissions(
            @RequestBody @Valid MissionSeedRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(missionSeedCommandService.seed(request)));
    }

    @PostMapping("/assign-next-tier")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HRM')")
    public ResponseEntity<ApiResponse<MissionAssignmentResponse>> assignNextTierMissions(
            @RequestBody @Valid MissionAssignmentRequest request) {
        MissionAssignmentResponse response = missionAssignmentCommandService.assignNextTierMissions(
                request.getEmployeeId(),
                request.getCurrentTier()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
