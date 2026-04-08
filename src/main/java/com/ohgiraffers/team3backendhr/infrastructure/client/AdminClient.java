package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamMemberAddRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;

import java.util.List;

public interface AdminClient {

    List<WorkerResponse> getWorkers();

    /* HR-064: 사원 프로필 조회 */
    EmployeeProfileResponse getWorkerProfile(Long employeeId);

    /* HR-065: 사원 보유 스킬 조회 */
    List<EmployeeSkillResponse> getWorkerSkills(Long employeeId);

    /* HR-066: 티어 마일스톤 조회 */
    List<TierMilestoneResponse> getTierMilestones(Long employeeId);

    /* HR-067: 티어 차트(이력) 조회 */
    List<TierChartPointResponse> getTierChart(Long employeeId);

    /* TL KPI용: 팀장의 팀원 ID 목록 조회 */
    List<Long> getTeamMemberIds(Long leaderId);

    /* HR-063: 승급 확정 시 Admin 서비스에 사원 현재 티어 갱신 요청 */
    void updateEmployeeTier(Long employeeId, Grade newTier);

    /* HR-073: 조직도 트리 조회 */
    OrgUnitTreeResponse getOrgTree();

    /* HR-074: 사원 목록 조회 */
    List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size);

    /* HR-075: 팀원 목록 및 팀장 정보 조회 */
    OrgTeamMembersResponse getTeamMembers(Long teamId);

    /* HR-076: 신규 부서 생성 */
    Long createDepartment(DepartmentCreateRequest request);

    /* HR-077: 부서 정보 수정 */
    Long updateDepartment(Long departmentId, DepartmentCreateRequest request);

    /* HR-078: 부서 삭제 */
    void deleteDepartment(Long departmentId);

    /* HR-079: 신규 팀 생성 */
    Long createTeam(Long departmentId, TeamCreateRequest request);

    /* HR-080: 팀 정보 수정 */
    Long updateTeam(Long teamId, TeamCreateRequest request);

    /* HR-081: 팀 삭제 */
    void deleteTeam(Long teamId);

    /* HR-082: 부서 상세 조회 */
    DepartmentDetailResponse getDepartmentDetail(Long departmentId);

    /* HR-083: 팀원 추가 */
    void addTeamMembers(Long teamId, TeamMemberAddRequest request);

    /* HR-084: 팀원 제거 */
    void removeTeamMember(Long teamId, Long employeeId);
}
