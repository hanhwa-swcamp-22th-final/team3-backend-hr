package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WorkerFeedbackResponse {

    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private List<WorkerFeedbackItem> feedbackItems;
}
