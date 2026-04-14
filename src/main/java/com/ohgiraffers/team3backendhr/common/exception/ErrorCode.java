package com.ohgiraffers.team3backendhr.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /* 공통 */
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "BAD_REQUEST_001", "잘못된 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORBIDDEN_001", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR_001", "서버 내부 오류가 발생했습니다."),

    /* 평가 기간 (EvaluationPeriod) */
    EVAL_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_001", "평가 기간을 찾을 수 없습니다."),
    EVAL_PERIOD_ALREADY_IN_PROGRESS(HttpStatus.BAD_REQUEST, "CONFLICT_001", "이미 진행 중인 평가 기간이 있습니다."),
    EVAL_PERIOD_DUPLICATE(HttpStatus.BAD_REQUEST, "CONFLICT_002", "동일한 연도·차수의 평가 기간이 이미 존재합니다."),
    EVAL_PERIOD_DATE_OVERLAP(HttpStatus.BAD_REQUEST, "CONFLICT_003", "기존 평가 기간과 날짜가 중복됩니다."),
    EVAL_PERIOD_CANNOT_CLOSE(HttpStatus.BAD_REQUEST, "CONFLICT_004", "진행 중인 평가 기간만 마감할 수 있습니다."),
    EVAL_PERIOD_CANNOT_CONFIRM(HttpStatus.BAD_REQUEST, "CONFLICT_005", "마감된 평가 기간만 확정할 수 있습니다."),
    EVAL_PERIOD_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "CONFLICT_006", "확정된 평가 기간은 수정할 수 없습니다."),
    EVAL_PERIOD_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "CONFLICT_007", "현재 진행 중인 평가 기간이 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "BAD_REQUEST_002", "종료일은 시작일보다 이후여야 합니다."),

    /* 정성 평가 (QualitativeEvaluation) */
    EVALUATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_002", "평가를 찾을 수 없습니다."),
    EVALUATION_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "CONFLICT_008", "이미 제출된 평가입니다."),
    EVALUATION_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "CONFLICT_009", "이미 확정된 평가입니다."),
    EVALUATION_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "CONFLICT_010", "제출된 평가에만 분석 결과를 반영할 수 있습니다."),
    EVALUATION_LEVEL1_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "CONFLICT_011", "1차 평가가 제출되지 않아 2차 평가를 진행할 수 없습니다."),
    EVALUATION_LEVEL2_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "CONFLICT_012", "2차 평가가 제출되지 않아 최종 확정을 진행할 수 없습니다."),
    EVALUATION_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "CONFLICT_013", "확정된 평가에 대해서만 이의신청할 수 있습니다."),
    INVALID_COMMENT_LENGTH(HttpStatus.BAD_REQUEST, "BAD_REQUEST_003", "평가 코멘트는 최소 20자 이상이어야 합니다."),

    /* 평가 기준 (TierCriteria) */
    TIER_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_003", "승급 목표 티어 설정을 찾을 수 없습니다."),
    INVALID_WEIGHT_SUM(HttpStatus.BAD_REQUEST, "BAD_REQUEST_004", "가중치 합계는 100%이어야 합니다."),
    INVALID_WEIGHT_FORMAT(HttpStatus.BAD_REQUEST, "BAD_REQUEST_005", "가중치 분포 JSON 형식이 올바르지 않습니다."),

    /* 이의신청 (Appeal) */
    APPEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_004", "이의신청을 찾을 수 없습니다."),
    APPEAL_EXPIRED(HttpStatus.BAD_REQUEST, "CONFLICT_014", "결과 통보 후 7일이 지나 이의신청할 수 없습니다."),
    APPEAL_NOT_RECEIVABLE(HttpStatus.BAD_REQUEST, "CONFLICT_015", "접수 중인 이의신청만 수정할 수 있습니다."),
    APPEAL_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "CONFLICT_016", "완료된 이의신청은 취소할 수 없습니다."),
    APPEAL_ALREADY_REVIEWING(HttpStatus.BAD_REQUEST, "CONFLICT_017", "이미 검토 중인 이의신청입니다."),
    APPEAL_NOT_REVIEWING(HttpStatus.BAD_REQUEST, "CONFLICT_018", "검토 중인 이의신청만 처리할 수 있습니다."),

    /* 승급 (Promotion) */
    PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_005", "승급 이력을 찾을 수 없습니다."),
    PROMOTION_NOT_UNDER_REVIEW(HttpStatus.BAD_REQUEST, "CONFLICT_019", "심사 중인 승급 후보만 처리할 수 있습니다."),
    PROMOTION_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "CONFLICT_020", "승급 확정된 이력만 티어에 반영할 수 있습니다."),

    /* 공지 (Notice) */
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_006", "공지를 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_007", "첨부파일을 찾을 수 없습니다."),
    NOTICE_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CONFLICT_021", "임시 저장 상태인 공지만 재저장할 수 있습니다."),
    INVALID_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "BAD_REQUEST_006", "예약 게시 시각은 중요 공지 종료일보다 이전이어야 합니다."),
    SCHEDULE_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "BAD_REQUEST_007", "예약 게시 시각은 필수입니다."),

    /* 알림 (Notification) */
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_007", "알림 수신 정보를 찾을 수 없습니다."),

    /* 미션 (Mission) */
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "CONFLICT_022", "이미 완료된 미션입니다."),

    /* 이의신청 제목·내용 길이 */
    INVALID_TITLE_LENGTH(HttpStatus.BAD_REQUEST, "BAD_REQUEST_008", "제목은 5자 이상 100자 이하여야 합니다."),
    INVALID_CONTENT_LENGTH(HttpStatus.BAD_REQUEST, "BAD_REQUEST_009", "내용은 20자 이상 2000자 이하여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
