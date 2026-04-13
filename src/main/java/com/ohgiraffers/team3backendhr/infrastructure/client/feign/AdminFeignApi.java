package com.ohgiraffers.team3backendhr.infrastructure.client.feign;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentUpdateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamMemberAddRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierUpdateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "adminFeignApi",
    url = "${feign.admin.url}",
    configuration = AdminFeignConfiguration.class
)
public interface AdminFeignApi {

    /* 직원 목록 */
    @GetMapping("/api/v1/organization/employees")
    AdminApiResponse<List<WorkerResponse>> getWorkers();

    /* 직원 프로필: Admin 구현 예정 계약 */
    @GetMapping("/api/v1/admin/employees/{employeeId}/profile")
    AdminApiResponse<EmployeeProfileResponse> getWorkerProfile(@PathVariable Long employeeId);

    /* 직원 보유 스킬: Admin 구현 예정 계약 */
    @GetMapping("/api/v1/admin/employees/{employeeId}/skills")
    AdminApiResponse<List<EmployeeSkillResponse>> getWorkerSkills(@PathVariable Long employeeId);

    /* 티어 차트 데이터: Admin 구현 예정 계약 */
    @GetMapping("/api/v1/admin/employees/{employeeId}/tier-chart")
    AdminApiResponse<List<TierChartPointResponse>> getTierChart(@PathVariable Long employeeId);

    /* 팀원 ID 목록 (TL용): Admin 구현 예정 계약 */
    @GetMapping("/api/v1/admin/employees/{leaderId}/team-members")
    AdminApiResponse<List<Long>> getTeamMemberIds(@PathVariable Long leaderId);

    /* 티어 변경: Admin 구현 예정 계약 */
    @PatchMapping("/api/v1/admin/employees/{employeeId}/tier")
    void updateEmployeeTier(@PathVariable Long employeeId, @RequestBody TierUpdateRequest request);

    /* 도메인 키워드 룰 */
    @GetMapping("/api/v1/domain-keyword")
    AdminApiResponse<List<DomainKeywordRuleResponse>> getDomainKeywordRules();

    /* 알고리즘 버전 상세 */
    @GetMapping("/api/v1/algorithm-version/{algorithmVersionId}")
    AdminApiResponse<AlgorithmVersionSnapshotResponse> getAlgorithmVersionDetail(
        @PathVariable Long algorithmVersionId
    );

    @GetMapping("/api/v1/algorithm-version")
    AdminApiResponse<List<AlgorithmVersionSnapshotResponse>> getAlgorithmVersionList(
        @RequestParam(required = false) Boolean isActive
    );

    /* 조직도 트리 */
    @GetMapping("/api/v1/admin/org/units")
    AdminApiResponse<OrgUnitTreeResponse> getOrgTree();

    /* 직원 목록 (필터) */
    @GetMapping("/api/v1/admin/org/employees")
    AdminApiResponse<List<OrgEmployeeResponse>> getEmployees(
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) Long teamId,
        @RequestParam(required = false) String keyword,
        @RequestParam int page,
        @RequestParam int size
    );

    /* 팀 구성원 조회 */
    @GetMapping("/api/v1/admin/org/teams/{teamId}/members")
    AdminApiResponse<OrgTeamMembersResponse> getTeamMembers(@PathVariable Long teamId);

    /* 부서 생성 */
    @PostMapping("/api/v1/admin/org/departments")
    AdminApiResponse<Long> createDepartment(@RequestBody DepartmentCreateRequest request);

    @PutMapping("/api/v1/admin/org/departments/{departmentId}")
    AdminApiResponse<Long> updateDepartment(@PathVariable Long departmentId, @RequestBody DepartmentCreateRequest request);

    @DeleteMapping("/api/v1/admin/org/departments/{departmentId}")
    void deleteDepartment(@PathVariable Long departmentId);

    /* 팀 생성 */
    @PostMapping("/api/v1/admin/org/departments/{departmentId}/teams")
    AdminApiResponse<Long> createTeam(@PathVariable Long departmentId, @RequestBody TeamCreateRequest request);

    /* 팀 수정 */
    @PutMapping("/api/v1/admin/org/teams/{teamId}")
    void updateTeam(@PathVariable Long teamId, @RequestBody TeamCreateRequest request);

    /* 팀 삭제 */
    @DeleteMapping("/api/v1/admin/org/teams/{teamId}")
    void deleteTeam(@PathVariable Long teamId);

    /* 부서 상세 */
    @GetMapping("/api/v1/admin/org/departments/{departmentId}")
    AdminApiResponse<DepartmentDetailResponse> getDepartmentDetail(@PathVariable Long departmentId);

    /* 팀원 추가 */
    @PostMapping("/api/v1/admin/org/teams/{teamId}/members")
    void addTeamMembers(@PathVariable Long teamId, @RequestBody TeamMemberAddRequest request);

    /* 팀원 제거 */
    @DeleteMapping("/api/v1/admin/org/teams/{teamId}/members/{employeeId}")
    void removeTeamMember(@PathVariable Long teamId, @PathVariable Long employeeId);
}
