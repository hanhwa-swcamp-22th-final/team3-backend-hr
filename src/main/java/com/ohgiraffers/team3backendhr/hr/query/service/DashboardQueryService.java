package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardTeamItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmTeamStatsItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.DashboardQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final DashboardQueryMapper mapper;
    private final AdminClient adminClient;

    /* HR-001 */
    public HrmKpiSummaryResponse getHrmKpiSummary(int year, int evalSequence) {
        List<HrmKpiDetailItem> details = getHrmKpiDetailItems(year, evalSequence);
        return buildHrmKpiSummary(details);
    }

    /* HR-002 */
    public List<HrmKpiDetailItem> getHrmKpiDetails(int year, int evalSequence, int page, int size) {
        return getHrmKpiDetailItems(year, evalSequence).stream()
                .sorted(this::compareHrmKpiDetail)
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public List<HrmKpiDetailItem> getHrmKpiDetailsAll(int year, int evalSequence) {
        return getHrmKpiDetailItems(year, evalSequence).stream()
                .sorted(this::compareHrmKpiDetail)
                .toList();
    }

    /* HR-003 */
    public List<HrmKpiTrendItem> getHrmKpiTrends(int year) {
        return mapper.findHrmKpiTrends(year);
    }

    public List<HrmTeamStatsItem> getHrmTeamStats(int year, int evalSequence) {
        return getHrmKpiDetailItems(year, evalSequence).stream()
                .collect(Collectors.groupingBy(item -> item.getDepartmentName() == null ? "미지정" : item.getDepartmentName()))
                .entrySet()
                .stream()
                .map(entry -> toHrmTeamStatsItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HrmTeamStatsItem::getDepartmentName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /* HR-005 */
    public TlDashboardSummaryResponse getTlDashboardSummary(Long tlId) {
        List<TlDashboardMemberItem> members = getTlDashboardMemberItems(tlId);
        return buildTlDashboardSummary(members);
    }

    /* HR-006 */
    public List<TlDashboardMemberItem> getTlDashboardMembers(Long tlId, int page, int size) {
        return getTlDashboardMemberItems(tlId).stream()
                .sorted(this::compareTlDashboardMember)
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /* HR-007 */
    public DlDashboardSummaryResponse getDlDashboardSummary(Long dlId) {
        return buildDlDashboardSummary(getDlDashboardMemberItems(dlId));
    }

    /* HR-008 */
    public List<DlDashboardTeamItem> getDlDashboardTeams(Long dlId) {
        return buildDlDashboardTeams(getDlDashboardMemberItems(dlId));
    }

    /* HR-009 */
    public List<DlDashboardMemberItem> getDlDashboardMembers(Long dlId, int page, int size) {
        return getDlDashboardMemberItems(dlId).stream()
                .sorted(this::compareDlDashboardMember)
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    private List<TlDashboardMemberItem> getTlDashboardMemberItems(Long tlId) {
        List<EmployeeProfileResponse> profiles = adminClient.getTeamMemberProfiles(tlId);
        if (profiles.isEmpty()) {
            return List.of();
        }

        List<Long> employeeIds = profiles.stream()
                .map(EmployeeProfileResponse::getEmployeeId)
                .toList();
        Map<Long, TlDashboardMemberItem> scoreMap = mapper.findTlDashboardScores(employeeIds).stream()
                .collect(Collectors.toMap(TlDashboardMemberItem::getEmployeeId, Function.identity()));

        return profiles.stream()
                .map(profile -> toTlDashboardMemberItem(profile, scoreMap.get(profile.getEmployeeId())))
                .toList();
    }

    private List<HrmKpiDetailItem> getHrmKpiDetailItems(int year, int evalSequence) {
        List<OrgEmployeeResponse> employees = adminClient.getEmployees(null, null, null, 0, 10_000).stream()
                .filter(employee -> employee.getEmployeeId() != null)
                .filter(employee -> employee.getRole() == null || "WORKER".equals(employee.getRole()))
                .toList();
        if (employees.isEmpty()) {
            return List.of();
        }

        List<Long> employeeIds = employees.stream()
                .map(OrgEmployeeResponse::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, HrmKpiDetailItem> scoreMap = mapper.findHrmKpiScores(employeeIds, year, evalSequence).stream()
                .collect(Collectors.toMap(HrmKpiDetailItem::getEmployeeId, Function.identity(), (left, right) -> left));

        return employees.stream()
                .map(employee -> toHrmKpiDetailItem(employee, scoreMap.get(employee.getEmployeeId())))
                .toList();
    }

    private HrmKpiDetailItem toHrmKpiDetailItem(OrgEmployeeResponse employee, HrmKpiDetailItem score) {
        HrmKpiDetailItem item = score == null ? new HrmKpiDetailItem() : score;
        item.setEmployeeId(employee.getEmployeeId());
        item.setEmployeeName(employee.getName());
        item.setEmployeeTier(employee.getCurrentTier());
        item.setDepartmentName(resolveHrmDepartmentName(employee));
        if (item.getEvalStatus() == null) {
            item.setEvalStatus("NO_INPUT");
        }
        return item;
    }

    private String resolveHrmDepartmentName(OrgEmployeeResponse employee) {
        if (employee.getDepartmentName() != null && !employee.getDepartmentName().isBlank()) {
            return employee.getDepartmentName();
        }
        if (employee.getTeamName() != null && !employee.getTeamName().isBlank()) {
            return employee.getTeamName();
        }
        return "미지정";
    }

    private HrmKpiSummaryResponse buildHrmKpiSummary(List<HrmKpiDetailItem> details) {
        HrmKpiSummaryResponse summary = new HrmKpiSummaryResponse();
        summary.setTotalEmployees(details.size());
        if (details.isEmpty()) {
            summary.setAvgScore(0.0);
            summary.setEvaluationRate(0.0);
            return summary;
        }

        double avgScore = details.stream()
                .map(HrmKpiDetailItem::getQualitativeScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        long evaluatedCount = details.stream()
                .filter(item -> item.getEvalStatus() != null && !"NO_INPUT".equals(item.getEvalStatus()))
                .count();

        summary.setAvgScore(round1(avgScore));
        summary.setEvaluationRate(round1(evaluatedCount * 100.0 / details.size()));
        summary.setUnevaluatedCount((int) (details.size() - evaluatedCount));
        summary.setSTierCount(countHrmTier(details, "S"));
        summary.setATierCount(countHrmTier(details, "A"));
        summary.setBTierCount(countHrmTier(details, "B"));
        summary.setCTierCount(countHrmTier(details, "C"));
        return summary;
    }

    private HrmTeamStatsItem toHrmTeamStatsItem(String departmentName, List<HrmKpiDetailItem> details) {
        HrmTeamStatsItem item = new HrmTeamStatsItem();
        item.setDepartmentName(departmentName);
        item.setMemberCount(details.size());
        item.setAvgScore(round1(details.stream()
                .map(HrmKpiDetailItem::getQualitativeScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0)));
        item.setSTierCount(countHrmTier(details, "S"));
        item.setATierCount(countHrmTier(details, "A"));
        item.setBTierCount(countHrmTier(details, "B"));
        item.setCTierCount(countHrmTier(details, "C"));
        return item;
    }

    private int countHrmTier(List<HrmKpiDetailItem> details, String tier) {
        return (int) details.stream()
                .filter(item -> tier.equals(item.getEmployeeTier()))
                .count();
    }

    private int compareHrmKpiDetail(HrmKpiDetailItem left, HrmKpiDetailItem right) {
        Double leftScore = left.getQualitativeScore();
        Double rightScore = right.getQualitativeScore();
        if (leftScore == null && rightScore != null) {
            return 1;
        }
        if (leftScore != null && rightScore == null) {
            return -1;
        }
        int scoreCompare = leftScore == null ? 0 : Double.compare(rightScore, leftScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Comparator.nullsLast(String::compareTo)
                .compare(left.getEmployeeName(), right.getEmployeeName());
    }

    private TlDashboardMemberItem toTlDashboardMemberItem(
            EmployeeProfileResponse profile,
            TlDashboardMemberItem score
    ) {
        TlDashboardMemberItem item = new TlDashboardMemberItem();
        item.setEmployeeId(profile.getEmployeeId());
        item.setEmployeeName(profile.getEmployeeName());
        item.setEmployeeCode(profile.getEmployeeCode());
        item.setTier(profile.getCurrentTier() == null ? null : profile.getCurrentTier().name());

        if (score != null) {
            item.setQuantitativeScore(score.getQuantitativeScore());
            item.setQualitativeScore(score.getQualitativeScore());
            item.setGrade(score.getGrade());
            item.setEvalStatus(score.getEvalStatus());
        }
        return item;
    }

    private List<DlDashboardMemberItem> getDlDashboardMemberItems(Long dlId) {
        List<EmployeeProfileResponse> profiles = adminClient.getTeamMemberProfiles(dlId);
        if (profiles.isEmpty()) {
            return List.of();
        }

        List<Long> employeeIds = profiles.stream()
                .map(EmployeeProfileResponse::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, DlDashboardMemberItem> scoreMap = mapper.findDlDashboardScores(employeeIds).stream()
                .collect(Collectors.toMap(DlDashboardMemberItem::getEmployeeId, Function.identity(), (left, right) -> left));

        return profiles.stream()
                .collect(Collectors.toMap(EmployeeProfileResponse::getEmployeeId, Function.identity(), (left, right) -> left))
                .values()
                .stream()
                .map(profile -> toDlDashboardMemberItem(profile, scoreMap.get(profile.getEmployeeId())))
                .toList();
    }

    private DlDashboardMemberItem toDlDashboardMemberItem(
            EmployeeProfileResponse profile,
            DlDashboardMemberItem score
    ) {
        DlDashboardMemberItem item = new DlDashboardMemberItem();
        item.setEmployeeId(profile.getEmployeeId());
        item.setEmployeeCode(profile.getEmployeeCode());
        item.setEmployeeName(profile.getEmployeeName());
        item.setEmployeeTier(profile.getCurrentTier() == null ? null : profile.getCurrentTier().name());
        item.setTeamName(resolveTeamName(profile));

        if (score != null) {
            item.setQualitativeScore(score.getQualitativeScore());
            item.setGrade(score.getGrade());
            item.setEvalStatus(score.getEvalStatus());
        }
        if (item.getEvalStatus() == null) {
            item.setEvalStatus("NO_INPUT");
        }
        return item;
    }

    private DlDashboardSummaryResponse buildDlDashboardSummary(List<DlDashboardMemberItem> members) {
        DlDashboardSummaryResponse summary = new DlDashboardSummaryResponse();
        if (members.isEmpty()) {
            summary.setDeptAvgScore(0.0);
            summary.setEvaluationRate(0.0);
            return summary;
        }

        double average = members.stream()
                .map(DlDashboardMemberItem::getQualitativeScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        long unevaluatedCount = members.stream()
                .filter(member -> member.getEvalStatus() == null || "NO_INPUT".equals(member.getEvalStatus()))
                .count();

        summary.setDeptAvgScore(round1(average));
        summary.setEvaluationRate(round1((members.size() - unevaluatedCount) * 100.0 / members.size()));
        summary.setUnevaluatedCount((int) unevaluatedCount);
        summary.setSTierCount(countDlTier(members, "S"));
        summary.setATierCount(countDlTier(members, "A"));
        summary.setBTierCount(countDlTier(members, "B"));
        summary.setCTierCount(countDlTier(members, "C"));
        return summary;
    }

    private List<DlDashboardTeamItem> buildDlDashboardTeams(List<DlDashboardMemberItem> members) {
        return members.stream()
                .collect(Collectors.groupingBy(member -> member.getTeamName() == null ? "미지정" : member.getTeamName()))
                .entrySet()
                .stream()
                .map(entry -> toDlDashboardTeamItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DlDashboardTeamItem::getTeamName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private DlDashboardTeamItem toDlDashboardTeamItem(String teamName, List<DlDashboardMemberItem> members) {
        DlDashboardTeamItem item = new DlDashboardTeamItem();
        item.setTeamName(teamName);
        item.setTlName(teamName);
        item.setMemberCount(members.size());
        item.setTeamAvgScore(round1(members.stream()
                .map(DlDashboardMemberItem::getQualitativeScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0)));
        boolean allEvaluated = members.stream()
                .allMatch(member -> member.getEvalStatus() != null && !"NO_INPUT".equals(member.getEvalStatus()));
        item.setEvaluationStatus(allEvaluated ? "평가완료" : "심사중");
        return item;
    }

    private int countDlTier(List<DlDashboardMemberItem> members, String tier) {
        return (int) members.stream()
                .filter(member -> tier.equals(member.getEmployeeTier()))
                .count();
    }

    private String resolveTeamName(EmployeeProfileResponse profile) {
        if (profile.getTeamName() != null && !profile.getTeamName().isBlank()) {
            return profile.getTeamName();
        }
        return profile.getDepartmentName();
    }

    private TlDashboardSummaryResponse buildTlDashboardSummary(List<TlDashboardMemberItem> members) {
        TlDashboardSummaryResponse summary = new TlDashboardSummaryResponse();
        if (members.isEmpty()) {
            summary.setTeamAvgScore(0.0);
            summary.setEvaluationRate(0.0);
            return summary;
        }

        double average = members.stream()
                .map(TlDashboardMemberItem::getQualitativeScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        long evaluatedCount = members.stream()
                .filter(member -> member.getEvalStatus() != null && !"NO_INPUT".equals(member.getEvalStatus()))
                .count();
        long completedCount = members.stream()
                .filter(member -> "CONFIRMED".equals(member.getEvalStatus()))
                .count();

        summary.setTeamAvgScore(round1(average));
        summary.setEvaluationRate(round1(evaluatedCount * 100.0 / members.size()));
        summary.setCompletedCount((int) completedCount);
        summary.setSTierCount(countTier(members, "S"));
        summary.setATierCount(countTier(members, "A"));
        summary.setBTierCount(countTier(members, "B"));
        summary.setCTierCount(countTier(members, "C"));
        return summary;
    }

    private int countTier(List<TlDashboardMemberItem> members, String tier) {
        return (int) members.stream()
                .filter(member -> tier.equals(member.getGrade() == null ? member.getTier() : member.getGrade()))
                .count();
    }

    private int compareTlDashboardMember(TlDashboardMemberItem left, TlDashboardMemberItem right) {
        Double leftScore = left.getQualitativeScore();
        Double rightScore = right.getQualitativeScore();

        if (leftScore == null && rightScore != null) {
            return 1;
        }
        if (leftScore != null && rightScore == null) {
            return -1;
        }

        int scoreCompare = leftScore == null ? 0 : Double.compare(rightScore, leftScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Comparator.nullsLast(String::compareTo)
                .compare(left.getEmployeeName(), right.getEmployeeName());
    }

    private int compareDlDashboardMember(DlDashboardMemberItem left, DlDashboardMemberItem right) {
        Double leftScore = left.getQualitativeScore();
        Double rightScore = right.getQualitativeScore();

        if (leftScore == null && rightScore != null) {
            return 1;
        }
        if (leftScore != null && rightScore == null) {
            return -1;
        }

        int scoreCompare = leftScore == null ? 0 : Double.compare(rightScore, leftScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Comparator.nullsLast(String::compareTo)
                .compare(left.getEmployeeName(), right.getEmployeeName());
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
