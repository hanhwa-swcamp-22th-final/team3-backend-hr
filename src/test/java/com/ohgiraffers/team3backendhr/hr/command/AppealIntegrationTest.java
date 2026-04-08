package com.ohgiraffers.team3backendhr.hr.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealReviewRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.ReferenceType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.EvaluationAppeal;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.ReviewResult;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AppealRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentFileGroupRepository;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppealIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppealRepository appealRepository;
    @Autowired private AttachmentFileGroupRepository fileGroupRepository;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long WORKER_ID = 100L;
    private static final Long HRM_ID    = 200L;
    private static final Long EVAL_ID   = 10L;
    private static final Long PERIOD_ID = 5L;

    private UsernamePasswordAuthenticationToken workerAuth() {
        EmployeeUserDetails u = new EmployeeUserDetails(WORKER_ID, "W001", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken hrmAuth() {
        EmployeeUserDetails u = new EmployeeUserDetails(HRM_ID, "HRM001", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status, score) VALUES (?,?,?,?,'CONFIRMED',80.0)",
                EVAL_ID, WORKER_ID, PERIOD_ID, 3);
    }

    @AfterEach
    void tearDown() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); }

    private EvaluationAppeal saveAppeal(AppealStatus status) {
        AttachmentFileGroup fileGroup = fileGroupRepository.save(
                AttachmentFileGroup.builder()
                        .fileGroupId(idGenerator.generate())
                        .referenceType(ReferenceType.APPEAL)
                        .build());
        return appealRepository.save(EvaluationAppeal.builder()
                .appealId(idGenerator.generate())
                .qualitativeEvaluationId(EVAL_ID)
                .appealEmployeeId(WORKER_ID)
                .appealType(AppealType.SCORE_ERRORS)
                .title("점수 오류 이의신청")
                .content("평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.")
                .status(status)
                .filedAt(LocalDateTime.now())
                .fileGroupId(fileGroup.getFileGroupId())
                .build());
    }

    @Test
    @DisplayName("이의신청을 등록하면 DB에 저장된다")
    void register_success() throws Exception {
        AppealRegisterRequest request = new AppealRegisterRequest(
                EVAL_ID, AppealType.SCORE_ERRORS,
                "점수 오류 이의신청", "평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.");

        mockMvc.perform(post("/api/v1/hr/appeals")
                        .with(csrf()).with(authentication(workerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(appealRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("이의신청을 수정하면 내용이 변경된다")
    void update_success() throws Exception {
        EvaluationAppeal appeal = saveAppeal(AppealStatus.RECEIVING);
        AppealUpdateRequest request = new AppealUpdateRequest(
                AppealType.MISSING_ITEMS, "수정된 이의신청 제목", "수정된 내용입니다. 충분히 길게 작성합니다.");

        mockMvc.perform(put("/api/v1/hr/appeals/" + appeal.getAppealId())
                        .with(csrf()).with(authentication(workerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        EvaluationAppeal updated = appealRepository.findById(appeal.getAppealId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("수정된 이의신청 제목");
        assertThat(updated.getAppealType()).isEqualTo(AppealType.MISSING_ITEMS);
    }

    @Test
    @DisplayName("이의신청을 취소하면 DB에서 삭제된다")
    void cancel_success() throws Exception {
        EvaluationAppeal appeal = saveAppeal(AppealStatus.RECEIVING);

        mockMvc.perform(delete("/api/v1/hr/appeals/" + appeal.getAppealId())
                        .with(csrf()).with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(appealRepository.findById(appeal.getAppealId())).isEmpty();
    }

    @Test
    @DisplayName("승인하면 이의신청이 COMPLETED·ACKNOWLEDGE로 변경된다")
    void approve_success() throws Exception {
        EvaluationAppeal appeal = saveAppeal(AppealStatus.REVIEWING);
        AppealReviewRequest request = new AppealReviewRequest(
                ReviewResult.ACKNOWLEDGE, 90.0, "점수 오류 확인됨. 재산정 완료.");

        mockMvc.perform(post("/api/v1/hr/appeals/" + appeal.getAppealId() + "/approve")
                        .with(csrf()).with(authentication(hrmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        EvaluationAppeal updated = appealRepository.findById(appeal.getAppealId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(updated.getReviewResult()).isEqualTo(ReviewResult.ACKNOWLEDGE);
    }

    @Test
    @DisplayName("반려하면 이의신청이 COMPLETED·DISMISS로 변경된다")
    void reject_success() throws Exception {
        EvaluationAppeal appeal = saveAppeal(AppealStatus.REVIEWING);

        mockMvc.perform(post("/api/v1/hr/appeals/" + appeal.getAppealId() + "/reject")
                        .with(csrf()).with(authentication(hrmAuth())))
                .andExpect(status().isOk());

        EvaluationAppeal updated = appealRepository.findById(appeal.getAppealId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(updated.getReviewResult()).isEqualTo(ReviewResult.DISMISS);
    }

    @Test
    @DisplayName("보류하면 이의신청이 REVIEWING 상태를 유지한다")
    void hold_success() throws Exception {
        EvaluationAppeal appeal = saveAppeal(AppealStatus.REVIEWING);

        mockMvc.perform(post("/api/v1/hr/appeals/" + appeal.getAppealId() + "/hold")
                        .with(csrf()).with(authentication(hrmAuth())))
                .andExpect(status().isOk());

        EvaluationAppeal updated = appealRepository.findById(appeal.getAppealId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AppealStatus.REVIEWING);
    }
}
