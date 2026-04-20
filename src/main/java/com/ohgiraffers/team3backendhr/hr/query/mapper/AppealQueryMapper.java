package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AppealQueryMapper {

    /* HRM — 이의신청 상세 조회 */
    Optional<AppealDetailResponse> findAppealById(@Param("appealId") Long appealId);

    /* HRM — 이의신청 목록 (상태 필터, 페이징) */
    List<AppealSummaryResponse> findAppeals(
            @Param("status") String status,
            @Param("evaluationPeriodId") Long evaluationPeriodId,
            @Param("size") int size,
            @Param("offset") int offset);

    long countAppeals(
            @Param("status") String status,
            @Param("evaluationPeriodId") Long evaluationPeriodId);

    /* Worker — 내 이의신청 목록 */
    List<AppealSummaryResponse> findMyAppeals(
            @Param("employeeId") Long employeeId,
            @Param("evaluationPeriodId") Long evaluationPeriodId);

    /* TL/DL — 담당 팀원 이의신청 목록 (memberIds 로 필터) */
    List<AppealSummaryResponse> findReviewerAppeals(
            @Param("memberIds") List<Long> memberIds,
            @Param("evaluationPeriodId") Long evaluationPeriodId,
            @Param("statuses") List<String> statuses);
}
