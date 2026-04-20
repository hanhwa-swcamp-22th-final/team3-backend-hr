package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.AppealQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppealQueryServiceTest {

    @Mock
    private AppealQueryMapper mapper;

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private AppealQueryService service;

    /* ── getAppeal ─────────────────────────────────────────────────── */

    @Test
    @DisplayName("이의신청 상세 조회 — HRM은 누구든 조회 가능")
    void getAppeal_hrm_success() {
        // given
        AppealDetailResponse detail = new AppealDetailResponse();
        detail.setAppealId(1L);
        detail.setAppealEmployeeId(100L);
        given(mapper.findAppealById(1L)).willReturn(Optional.of(detail));
        EmployeeUserDetails hrm = new EmployeeUserDetails(200L, "H001", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));

        // when
        AppealDetailResponse result = service.getAppeal(1L, hrm);

        // then
        assertThat(result.getAppealId()).isEqualTo(1L);
        verify(mapper).findAppealById(1L);
    }

    @Test
    @DisplayName("이의신청 상세 조회 — Worker는 본인 것 조회 가능")
    void getAppeal_worker_own_success() {
        // given
        AppealDetailResponse detail = new AppealDetailResponse();
        detail.setAppealId(1L);
        detail.setAppealEmployeeId(100L);
        given(mapper.findAppealById(1L)).willReturn(Optional.of(detail));
        EmployeeUserDetails worker = new EmployeeUserDetails(100L, "W001", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));

        // when
        AppealDetailResponse result = service.getAppeal(1L, worker);

        // then
        assertThat(result.getAppealId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이의신청 상세 조회 — Worker가 타인 것 조회 시 예외 발생")
    void getAppeal_worker_others_forbidden() {
        // given
        AppealDetailResponse detail = new AppealDetailResponse();
        detail.setAppealId(1L);
        detail.setAppealEmployeeId(100L);
        given(mapper.findAppealById(1L)).willReturn(Optional.of(detail));
        EmployeeUserDetails worker = new EmployeeUserDetails(999L, "W002", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));

        // when & then
        assertThatThrownBy(() -> service.getAppeal(1L, worker))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이의신청 상세 조회 — 존재하지 않으면 예외 발생")
    void getAppeal_notFound() {
        // given
        given(mapper.findAppealById(999L)).willReturn(Optional.empty());
        EmployeeUserDetails hrm = new EmployeeUserDetails(1L, "H001", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));

        // when & then
        assertThatThrownBy(() -> service.getAppeal(999L, hrm))
                .isInstanceOf(BusinessException.class);
    }

    /* ── getAppeals ────────────────────────────────────────────────── */

    @Test
    @DisplayName("이의신청 목록 조회 — 상태 필터 없이 전체 반환")
    void getAppeals_noFilter() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setAppealId(1L);
        given(mapper.findAppeals(null, null, 10, 0)).willReturn(List.of(item));
        given(mapper.countAppeals(null, null)).willReturn(1L);

        // when
        var result = service.getAppeals(null, null, 0, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이의신청 목록 조회 — 상태 필터 적용")
    void getAppeals_withStatusFilter() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setStatus("RECEIVING");
        given(mapper.findAppeals("RECEIVING", null, 10, 10)).willReturn(List.of(item));
        given(mapper.countAppeals("RECEIVING", null)).willReturn(1L);

        // when
        var result = service.getAppeals("RECEIVING", null, 1, 10);

        // then
        assertThat(result.getContent()).allMatch(r -> "RECEIVING".equals(r.getStatus()));
        verify(mapper).findAppeals("RECEIVING", null, 10, 10);
    }

    /* ── getMyAppeals ──────────────────────────────────────────────── */

    @Test
    @DisplayName("내 이의신청 조회 — 본인 목록 반환")
    void getMyAppeals_success() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setAppealEmployeeId(100L);
        given(mapper.findMyAppeals(100L, null)).willReturn(List.of(item));

        // when
        List<AppealSummaryResponse> result = service.getMyAppeals(100L, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppealEmployeeId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("내 이의신청 조회 — 데이터 없으면 빈 목록")
    void getMyAppeals_empty() {
        // given
        given(mapper.findMyAppeals(999L, null)).willReturn(List.of());

        // when
        List<AppealSummaryResponse> result = service.getMyAppeals(999L, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("TL 이의신청 조회 — 진행중과 완료 상태를 함께 반환하도록 조회한다")
    void getTlAppeals_includesCompletedStatuses() {
        // given
        given(adminClient.getTeamMemberIds(200L)).willReturn(List.of(100L, 101L));
        given(mapper.findReviewerAppeals(List.of(100L, 101L), null, List.of("RECEIVING", "REVIEWING", "COMPLETED")))
                .willReturn(List.of(new AppealSummaryResponse()));

        // when
        List<AppealSummaryResponse> result = service.getTlAppeals(200L, null);

        // then
        assertThat(result).hasSize(1);
        verify(mapper).findReviewerAppeals(List.of(100L, 101L), null, List.of("RECEIVING", "REVIEWING", "COMPLETED"));
    }

    @Test
    @DisplayName("DL 이의신청 조회 — 검토중과 완료 상태를 함께 반환하도록 조회한다")
    void getDlAppeals_includesCompletedStatuses() {
        // given
        given(adminClient.getWorkerProfile(300L)).willReturn(EmployeeProfileResponse.builder()
                .employeeId(300L)
                .departmentId(10L)
                .build());
        given(adminClient.getActiveWorkerIdsByRootDepartmentId(10L)).willReturn(List.of(100L, 101L));
        given(mapper.findReviewerAppeals(List.of(100L, 101L), null, List.of("REVIEWING", "COMPLETED")))
                .willReturn(List.of(new AppealSummaryResponse()));

        // when
        List<AppealSummaryResponse> result = service.getDlAppeals(300L, null);

        // then
        assertThat(result).hasSize(1);
        verify(mapper).findReviewerAppeals(List.of(100L, 101L), null, List.of("REVIEWING", "COMPLETED"));
    }


}
