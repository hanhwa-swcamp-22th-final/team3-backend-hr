package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.OrgCommandService;
import com.ohgiraffers.team3backendhr.hr.query.service.OrgQueryService;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgQueryService orgQueryService;
    private final OrgCommandService orgCommandService;

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

    /* HR-076: 신규 부서 생성 */
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> createDepartment(@RequestBody @Valid DepartmentCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(orgCommandService.createDepartment(request)));
    }

    /* HR-077: 부서 정보 수정 */
    @PutMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> updateDepartment(
            @PathVariable Long departmentId,
            @RequestBody @Valid DepartmentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orgCommandService.updateDepartment(departmentId, request)));
    }

    /* HR-078: 부서 삭제 */
    @DeleteMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long departmentId) {
        orgCommandService.deleteDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* HR-079: 신규 팀 생성 */
    @PostMapping("/departments/{departmentId}/teams")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> createTeam(
            @PathVariable Long departmentId,
            @RequestBody @Valid TeamCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(orgCommandService.createTeam(departmentId, request)));
    }

    /* HR-080: 팀 정보 수정 */
    @PutMapping("/teams/{teamId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> updateTeam(
            @PathVariable Long teamId,
            @RequestBody @Valid TeamCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orgCommandService.updateTeam(teamId, request)));
    }

    /* HR-081: 팀 삭제 */
    @DeleteMapping("/teams/{teamId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long teamId) {
        orgCommandService.deleteTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* HR-082: 부서 상세 조회 */
    @GetMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<DepartmentDetailResponse>> getDepartmentDetail(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(orgQueryService.getDepartmentDetail(departmentId)));
    }

    /* HR-083: 팀원 추가 */
    @PostMapping("/teams/{teamId}/members")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> addTeamMembers(
            @PathVariable Long teamId,
            @RequestBody @Valid TeamMemberAddRequest request) {
        orgCommandService.addTeamMembers(teamId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* HR-084: 팀원 제거 */
    @DeleteMapping("/teams/{teamId}/members/{employeeId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        orgCommandService.removeTeamMember(teamId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* HR-085: 부서장 지정 */
    @PatchMapping("/departments/{departmentId}/leader")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> assignDepartmentLeader(
            @PathVariable Long departmentId,
            @RequestBody @Valid DepartmentLeaderAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                orgCommandService.assignDepartmentLeader(departmentId, request)));
    }
}
