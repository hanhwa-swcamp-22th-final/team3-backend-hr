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
            @Param("size") int size,
            @Param("offset") int offset);

    long countAppeals(@Param("status") String status);

    /* Worker — 내 이의신청 목록 */
    List<AppealSummaryResponse> findMyAppeals(@Param("employeeId") Long employeeId);

    /* TL/DL — 본인이 담당하는 차수의 이의신청 목록 */
    List<AppealSummaryResponse> findReviewerAppeals(
            @Param("reviewerId") Long reviewerId,
            @Param("evaluationLevel") Long evaluationLevel,
            @Param("statuses") List<String> statuses);

    /* TL/DL — 본인이 담당하는 차수의 이의신청 접근 권한 확인 */
    boolean existsReviewerAppealAccess(
            @Param("appealId") Long appealId,
            @Param("reviewerId") Long reviewerId,
            @Param("evaluationLevel") Long evaluationLevel);
}
