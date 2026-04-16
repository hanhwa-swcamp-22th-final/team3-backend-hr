package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.OrgQueryService;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/org")
@RequiredArgsConstructor
public class OrgQueryController {

    private final OrgQueryService orgQueryService;

    /* HR-073: 조직도 트리 조회 */
    @GetMapping("/units")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<OrgUnitTreeResponse>> getOrgTree() {
        return ResponseEntity.ok(ApiResponse.success(orgQueryService.getOrgTree()));
    }

    /* HR-074: 사원 목록 조회 */
    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<OrgEmployeeResponse>>> getEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                orgQueryService.getEmployees(departmentId, teamId, keyword, page, size)));
    }

    /* HR-075: 팀원 목록 및 팀장 정보 조회 */
    @GetMapping("/teams/{teamId}/members")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<OrgTeamMembersResponse>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(orgQueryService.getTeamMembers(teamId)));
    }

    /* HR-082: 부서 상세 조회 */
    @GetMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<DepartmentDetailResponse>> getDepartmentDetail(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(orgQueryService.getDepartmentDetail(departmentId)));
    }
}
