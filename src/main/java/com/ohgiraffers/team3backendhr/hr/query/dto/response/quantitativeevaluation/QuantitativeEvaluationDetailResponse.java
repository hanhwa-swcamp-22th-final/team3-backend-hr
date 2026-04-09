package com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuantitativeEvaluationDetailResponse {

    private Long quantitativeEvaluationId;
    private Long employeeId;
    private String employeeName;
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private Long equipmentId;
    private Double uphScore;
    private Double yieldScore;
    private Double leadTimeScore;
    private Double actualError;
    private Double sQuant;
    private Double tScore;
    private Boolean materialShielding;
    private String status;
}
