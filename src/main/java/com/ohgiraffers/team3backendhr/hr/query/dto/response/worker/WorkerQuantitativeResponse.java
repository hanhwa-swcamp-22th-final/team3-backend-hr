package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerQuantitativeResponse {

    private Long quantitativeEvaluationId;
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private Double uphScore;
    private Double yieldScore;
    private Double leadTimeScore;
    private Double actualError;
    @JsonProperty("sQuant")
    private Double sQuant;
    @JsonProperty("tScore")
    private Double tScore;
    private Boolean materialShielding;
    private String status;
}
