package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardMemberItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardQueryMapper {

    List<HrmKpiDetailItem> findHrmKpiScores(@Param("employeeIds") List<Long> employeeIds,
                                            @Param("year") int year,
                                            @Param("evalSequence") int evalSequence);

    /* HR-003: HRM KPI 추이 (연도별 분기 목록) */
    List<HrmKpiTrendItem> findHrmKpiTrends(@Param("year") int year);

    /* HR-005/006: TL 팀원 평가 점수. 직원 정보는 Admin Feign으로 조회한다. */
    List<TlDashboardMemberItem> findTlDashboardScores(@Param("employeeIds") List<Long> employeeIds);

    /* HR-009: DL 관할 직원 평가 점수. 직원 정보는 Admin Feign으로 조회한다. */
    List<DlDashboardMemberItem> findDlDashboardScores(@Param("employeeIds") List<Long> employeeIds);
}
