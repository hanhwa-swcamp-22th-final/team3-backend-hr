package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
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
import com.ohgiraffers.team3backendhr.infrastructure.client.feign.AdminFeignApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminFeignClientTest {

    @Mock
    private AdminFeignApi adminFeignApi;

    @InjectMocks
    private AdminFeignClient adminFeignClient;

    private <T> AdminApiResponse<T> response(T data) {
        AdminApiResponse<T> response = new AdminApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    @Test
    @DisplayName("직원 목록 응답 data를 언래핑한다")
    void getWorkers_unwrapsData() {
        List<WorkerResponse> workers = List.of(mock(WorkerResponse.class));
        given(adminFeignApi.getWorkers()).willReturn(response(workers));

        List<WorkerResponse> result = adminFeignClient.getWorkers();

        assertThat(result).isSameAs(workers);
    }

    @Test
    @DisplayName("직원 목록 응답이 null이면 빈 목록을 반환한다")
    void getWorkers_nullResponse_returnsEmptyList() {
        given(adminFeignApi.getWorkers()).willReturn(null);

        List<WorkerResponse> result = adminFeignClient.getWorkers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("직원 프로필 응답 data를 언래핑한다")
    void getWorkerProfile_unwrapsData() {
        EmployeeProfileResponse profile = mock(EmployeeProfileResponse.class);
        given(adminFeignApi.getWorkerProfile(10L)).willReturn(response(profile));

        EmployeeProfileResponse result = adminFeignClient.getWorkerProfile(10L);

        assertThat(result).isSameAs(profile);
    }

    @Test
    @DisplayName("직원 스킬 응답 data를 언래핑한다")
    void getWorkerSkills_unwrapsData() {
        List<EmployeeSkillResponse> skills = List.of(mock(EmployeeSkillResponse.class));
        given(adminFeignApi.getWorkerSkills(10L)).willReturn(response(skills));

        List<EmployeeSkillResponse> result = adminFeignClient.getWorkerSkills(10L);

        assertThat(result).isSameAs(skills);
    }

    @Test
    @DisplayName("티어 차트 응답 data를 언래핑한다")
    void getTierChart_unwrapsData() {
        List<TierChartPointResponse> chart = List.of(mock(TierChartPointResponse.class));
        given(adminFeignApi.getTierChart(10L)).willReturn(response(chart));

        List<TierChartPointResponse> result = adminFeignClient.getTierChart(10L);

        assertThat(result).isSameAs(chart);
    }

    @Test
    @DisplayName("팀원 ID 목록 응답 data를 언래핑한다")
    void getTeamMemberIds_unwrapsData() {
        List<Long> memberIds = List.of(1L, 2L, 3L);
        given(adminFeignApi.getTeamMemberIds(77L)).willReturn(response(memberIds));

        List<Long> result = adminFeignClient.getTeamMemberIds(77L);

        assertThat(result).isSameAs(memberIds);
    }

    @Test
    @DisplayName("티어 변경 요청을 TierUpdateRequest로 위임한다")
    void updateEmployeeTier_forwardsTierUpdateRequest() {
        ArgumentCaptor<TierUpdateRequest> captor = ArgumentCaptor.forClass(TierUpdateRequest.class);

        adminFeignClient.updateEmployeeTier(10L, Grade.A);

        verify(adminFeignApi).updateEmployeeTier(eq(10L), captor.capture());
        assertThat(captor.getValue().getTier()).isEqualTo(Grade.A);
    }

    @Test
    @DisplayName("활성 도메인 키워드 룰 응답 data를 언래핑한다")
    void getActiveDomainKeywordRules_unwrapsData() {
        List<DomainKeywordRuleResponse> rules = List.of(mock(DomainKeywordRuleResponse.class));
        given(adminFeignApi.getDomainKeywordRules()).willReturn(response(rules));

        List<DomainKeywordRuleResponse> result = adminFeignClient.getActiveDomainKeywordRules();

        assertThat(result).isSameAs(rules);
    }

    @Test
    @DisplayName("알고리즘 버전 상세 응답 data를 언래핑한다")
    void getAlgorithmVersionSnapshot_unwrapsData() {
        AlgorithmVersionSnapshotResponse snapshot = mock(AlgorithmVersionSnapshotResponse.class);
        given(adminFeignApi.getAlgorithmVersionDetail(100L)).willReturn(response(snapshot));

        AlgorithmVersionSnapshotResponse result = adminFeignClient.getAlgorithmVersionSnapshot(100L);

        assertThat(result).isSameAs(snapshot);
    }

    @Test
    @DisplayName("조직도 트리 응답 data를 언래핑한다")
    void getOrgTree_unwrapsData() {
        OrgUnitTreeResponse tree = mock(OrgUnitTreeResponse.class);
        given(adminFeignApi.getOrgTree()).willReturn(response(tree));

        OrgUnitTreeResponse result = adminFeignClient.getOrgTree();

        assertThat(result).isSameAs(tree);
    }

    @Test
    @DisplayName("직원 필터 조회 요청을 위임하고 응답 data를 언래핑한다")
    void getEmployees_forwardsParamsAndUnwrapsData() {
        List<OrgEmployeeResponse> employees = List.of(mock(OrgEmployeeResponse.class));
        given(adminFeignApi.getEmployees(1L, 2L, "kim", 0, 20)).willReturn(response(employees));

        List<OrgEmployeeResponse> result = adminFeignClient.getEmployees(1L, 2L, "kim", 0, 20);

        assertThat(result).isSameAs(employees);
    }

    @Test
    @DisplayName("팀 구성원 조회 응답 data를 언래핑한다")
    void getTeamMembers_unwrapsData() {
        OrgTeamMembersResponse members = mock(OrgTeamMembersResponse.class);
        given(adminFeignApi.getTeamMembers(2L)).willReturn(response(members));

        OrgTeamMembersResponse result = adminFeignClient.getTeamMembers(2L);

        assertThat(result).isSameAs(members);
    }

    @Test
    @DisplayName("부서 생성 요청을 위임하고 생성 ID를 반환한다")
    void createDepartment_forwardsRequestAndReturnsId() {
        DepartmentCreateRequest request = mock(DepartmentCreateRequest.class);
        given(adminFeignApi.createDepartment(request)).willReturn(response(100L));

        Long result = adminFeignClient.createDepartment(request);

        assertThat(result).isEqualTo(100L);
    }

    @Test
    @DisplayName("부서 수정 요청을 위임하고 응답 ID를 반환한다")
    void updateDepartment_forwardsRequestAndReturnsId() {
        DepartmentCreateRequest request = mock(DepartmentCreateRequest.class);
        given(adminFeignApi.updateDepartment(100L, request)).willReturn(response(101L));

        Long result = adminFeignClient.updateDepartment(100L, request);

        assertThat(result).isEqualTo(101L);
    }

    @Test
    @DisplayName("부서 삭제 요청을 위임한다")
    void deleteDepartment_forwardsRequest() {
        adminFeignClient.deleteDepartment(100L);

        verify(adminFeignApi).deleteDepartment(100L);
    }

    @Test
    @DisplayName("팀 생성 요청을 위임하고 생성 ID를 반환한다")
    void createTeam_forwardsRequestAndReturnsId() {
        TeamCreateRequest request = mock(TeamCreateRequest.class);
        given(adminFeignApi.createTeam(100L, request)).willReturn(response(200L));

        Long result = adminFeignClient.createTeam(100L, request);

        assertThat(result).isEqualTo(200L);
    }

    @Test
    @DisplayName("팀 수정 요청을 위임하고 teamId를 반환한다")
    void updateTeam_forwardsRequestAndReturnsTeamId() {
        TeamCreateRequest request = mock(TeamCreateRequest.class);

        Long result = adminFeignClient.updateTeam(200L, request);

        verify(adminFeignApi).updateTeam(200L, request);
        assertThat(result).isEqualTo(200L);
    }

    @Test
    @DisplayName("팀 삭제 요청을 위임한다")
    void deleteTeam_forwardsRequest() {
        adminFeignClient.deleteTeam(200L);

        verify(adminFeignApi).deleteTeam(200L);
    }

    @Test
    @DisplayName("부서 상세 응답 data를 언래핑한다")
    void getDepartmentDetail_unwrapsData() {
        DepartmentDetailResponse detail = mock(DepartmentDetailResponse.class);
        given(adminFeignApi.getDepartmentDetail(100L)).willReturn(response(detail));

        DepartmentDetailResponse result = adminFeignClient.getDepartmentDetail(100L);

        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("팀원 추가 요청을 위임한다")
    void addTeamMembers_forwardsRequest() {
        TeamMemberAddRequest request = mock(TeamMemberAddRequest.class);

        adminFeignClient.addTeamMembers(200L, request);

        verify(adminFeignApi).addTeamMembers(200L, request);
    }

    @Test
    @DisplayName("팀원 제거 요청을 위임한다")
    void removeTeamMember_forwardsRequest() {
        adminFeignClient.removeTeamMember(200L, 10L);

        verify(adminFeignApi).removeTeamMember(200L, 10L);
    }
}
