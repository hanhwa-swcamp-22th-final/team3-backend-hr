package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerFeedbackItem {

    /** 1=TL, 2=DL, 3=HRM */
    private Long evaluationLevel;
    private String evalComment;
    private String inputMethod;
    private String status;
    private Integer evalYear;
    private Integer evalSequence;
}
