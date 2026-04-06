package com.ohgiraffers.team3backendhr.appeal.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ScoreModificationLogResponse {

    private Long scoreModificationLogId;
    private Long scoreEvaluateeId;
    private String evaluateeName;
    private Long scoreModifierId;
    private String modifierName;
    private Double scoreOriginalScore;
    private Double scoreModifiedScore;
    private String scoreReason;
    private LocalDateTime scoreModifiedAt;
}
