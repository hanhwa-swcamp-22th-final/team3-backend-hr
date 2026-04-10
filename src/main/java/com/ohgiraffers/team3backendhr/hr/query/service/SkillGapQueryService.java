package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.skillgap.SkillGapResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.skillgap.SkillGapResponse.SkillItem;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillGapQueryService {

    private final AdminClient adminClient;
    // TODO: KmsClient 연동 후 추천 교육 조회 추가

    public SkillGapResponse getSkillGap(Long employeeId, String targetTier) {
        // 현재 보유 스킬 조회 (Admin API)
        List<EmployeeSkillResponse> rawSkills = adminClient.getWorkerSkills(employeeId);
        List<SkillItem> currentSkills = rawSkills.stream()
                .map(s -> SkillItem.builder()
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .skillScore(s.getSkillScore())
                        .build())
                .toList();

        // TODO: Admin API에 티어별 필요 스킬 조회 엔드포인트 추가 후 연동
        // List<SkillItem> requiredSkills = adminClient.getRequiredSkillsByTier(targetTier);

        // TODO: missingSkills = requiredSkills - currentSkills (skillId 기준 차집합)

        // TODO: KMS API 연동 후 missingSkills 기반 추천 교육 조회

        return SkillGapResponse.builder()
                .currentSkills(currentSkills)
                .requiredSkills(List.of())       // TODO: Admin API 연동 후 채우기
                .missingSkills(List.of())         // TODO: 계산 로직 추가
                .recommendedCourses(List.of())    // TODO: KMS 연동 후 채우기
                .build();
    }
}
