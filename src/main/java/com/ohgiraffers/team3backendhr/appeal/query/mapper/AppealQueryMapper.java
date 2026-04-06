package com.ohgiraffers.team3backendhr.appeal.query.mapper;

import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.ScoreModificationLogResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppealQueryMapper {

    /* HRM — 이의신청 목록 (상태 필터, 페이징) */
    List<AppealSummaryResponse> findAppeals(
            @Param("status") String status,
            @Param("size") int size,
            @Param("offset") int offset);

    long countAppeals(@Param("status") String status);

    /* Worker — 내 이의신청 목록 */
    List<AppealSummaryResponse> findMyAppeals(@Param("employeeId") Long employeeId);

    /* HRM — 점수 수정 이력 */
    List<ScoreModificationLogResponse> findScoreModificationLogs();
}
