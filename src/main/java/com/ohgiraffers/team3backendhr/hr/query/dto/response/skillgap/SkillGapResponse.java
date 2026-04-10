package com.ohgiraffers.team3backendhr.hr.query.dto.response.skillgap;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SkillGapResponse {

    private String currentTier;
    private String targetTier;
    private List<SkillItem> currentSkills;
    private List<SkillItem> requiredSkills;   // Admin API 연동 후 채워질 필드
    private List<SkillItem> missingSkills;    // requiredSkills - currentSkills
    private List<RecommendedCourse> recommendedCourses; // KMS 연동 후 채워질 필드

    @Getter
    @Builder
    public static class SkillItem {
        private Long skillId;
        private String skillName;
        private BigDecimal skillScore;
    }

    @Getter
    @Builder
    public static class RecommendedCourse {
        private Long courseId;
        private String courseTitle;
        private String targetSkillName;
    }
}
