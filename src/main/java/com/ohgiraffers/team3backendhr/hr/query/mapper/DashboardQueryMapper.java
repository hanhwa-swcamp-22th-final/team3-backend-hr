package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardTeamItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardQueryMapper {

    /* HR-001: HRM 전사 KPI 요약 */
    HrmKpiSummaryResponse findHrmKpiSummary(@Param("year") int year, @Param("evalSequence") int evalSequence);

    /* HR-002: HRM 사원별 KPI 상세 목록 */
    List<HrmKpiDetailItem> findHrmKpiDetails(@Param("year") int year,
                                             @Param("evalSequence") int evalSequence,
                                             @Param("size") int size,
                                             @Param("offset") int offset);

    List<HrmKpiDetailItem> findHrmKpiDetailsAll(@Param("year") int year,
                                                @Param("evalSequence") int evalSequence);

    long countHrmKpiDetails(@Param("year") int year,
                            @Param("evalSequence") int evalSequence);

    /* HR-003: HRM KPI 추이 (연도별 분기 목록) */
    List<HrmKpiTrendItem> findHrmKpiTrends(@Param("year") int year);

    /* HR-005: TL 팀 지표 요약 */
    TlDashboardSummaryResponse findTlDashboardSummary(@Param("tlId") Long tlId);

    /* HR-006: TL 팀원별 지표 */
    List<TlDashboardMemberItem> findTlDashboardMembers(
            @Param("tlId") Long tlId,
            @Param("size") int size,
            @Param("offset") int offset);

    long countTlDashboardMembers(@Param("tlId") Long tlId);

    /* HR-007: DL 부서 지표 요약 */
    DlDashboardSummaryResponse findDlDashboardSummary(@Param("dlId") Long dlId);

    /* HR-008: DL 팀별 현황 */
    List<DlDashboardTeamItem> findDlDashboardTeams(@Param("dlId") Long dlId);

    /* HR-009: DL 팀원 역량/성과 목록 */
    List<DlDashboardMemberItem> findDlDashboardMembers(
            @Param("dlId") Long dlId,
            @Param("size") int size,
            @Param("offset") int offset);

    long countDlDashboardMembers(@Param("dlId") Long dlId);
}
