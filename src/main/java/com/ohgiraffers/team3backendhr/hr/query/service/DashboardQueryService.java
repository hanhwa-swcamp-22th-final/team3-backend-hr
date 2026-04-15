package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardTeamItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTeamStatsItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.DashboardQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final DashboardQueryMapper mapper;

    /* HR-001 */
    public HrmKpiSummaryResponse getHrmKpiSummary(int year, int evalSequence) {
        return mapper.findHrmKpiSummary(year, evalSequence);
    }

    /* HR-002 */
    public List<HrmKpiDetailItem> getHrmKpiDetails(int year, int evalSequence, int page, int size) {
        return mapper.findHrmKpiDetails(year, evalSequence, size, page * size);
    }

    public List<HrmKpiDetailItem> getHrmKpiDetailsAll(int year, int evalSequence) {
        return mapper.findHrmKpiDetailsAll(year, evalSequence);
    }

    /* HR-003 */
    public List<HrmKpiTrendItem> getHrmKpiTrends(int year) {
        return mapper.findHrmKpiTrends(year);
    }

    public List<HrmKpiTeamStatsItem> getHrmKpiTeamStats(int year, int evalSequence) {
        return mapper.findHrmKpiTeamStats(year, evalSequence);
    }

    /* HR-005 */
    public TlDashboardSummaryResponse getTlDashboardSummary(Long tlId) {
        return mapper.findTlDashboardSummary(tlId);
    }

    /* HR-006 */
    public List<TlDashboardMemberItem> getTlDashboardMembers(Long tlId, int page, int size) {
        return mapper.findTlDashboardMembers(tlId, size, page * size);
    }

    /* HR-007 */
    public DlDashboardSummaryResponse getDlDashboardSummary(Long dlId) {
        return mapper.findDlDashboardSummary(dlId);
    }

    /* HR-008 */
    public List<DlDashboardTeamItem> getDlDashboardTeams(Long dlId) {
        return mapper.findDlDashboardTeams(dlId);
    }

    /* HR-009 */
    public List<DlDashboardMemberItem> getDlDashboardMembers(Long dlId, int page, int size) {
        return mapper.findDlDashboardMembers(dlId, size, page * size);
    }
}
