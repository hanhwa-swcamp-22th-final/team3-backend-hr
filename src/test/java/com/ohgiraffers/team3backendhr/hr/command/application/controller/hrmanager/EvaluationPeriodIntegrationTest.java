package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EvaluationPeriodIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EvaluationPeriodRepository repository;

    @Autowired
    private QualitativeEvaluationRepository qualitativeEvaluationRepository;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AdminClient adminClient;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private WorkerResponse buildWorker(Long employeeId) {
        WorkerResponse worker = new WorkerResponse();
        worker.setEmployeeId(employeeId);
        return worker;
    }

    private EvaluationPeriod buildPeriod(EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .evalPeriodId(idGenerator.generate())
                .algorithmVersionId(1L)
                .evalYear(2026)
                .evalSequence(1)
                .evalType(EvalType.QUALITATIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @WithMockUser(roles = "HRM")
    @DisplayName("평가 기간 생성 시 DB에 저장된다")
    void create_success() throws Exception {
        // given
        given(adminClient.getWorkers()).willReturn(List.of(buildWorker(101L)));
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1, EvalType.QUALITATIVE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

        // when
        mockMvc.perform(post("/api/v1/hr/evaluation-periods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // then
        assertThat(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).isTrue();
        assertThat(qualitativeEvaluationRepository.findAll()).hasSize(3); // WORKER 1명 × level 3
    }

    @Test
    @WithMockUser(roles = "HRM")
    @DisplayName("이미 진행 중인 평가 기간이 있으면 400을 반환한다")
    void create_fail_alreadyInProgress() throws Exception {
        // given
        repository.save(buildPeriod(EvalPeriodStatus.IN_PROGRESS));
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 2, EvalType.QUALITATIVE,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30));

        // when & then
        mockMvc.perform(post("/api/v1/hr/evaluation-periods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HRM")
    @DisplayName("평가 기간을 마감하면 상태가 CLOSING으로 변경된다")
    void close_success() throws Exception {
        // given
        EvaluationPeriod period = repository.save(buildPeriod(EvalPeriodStatus.IN_PROGRESS));

        // when
        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/" + period.getEvalPeriodId() + "/close")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        EvaluationPeriod updated = repository.findById(period.getEvalPeriodId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EvalPeriodStatus.CLOSING);
    }

    @Test
    @WithMockUser(roles = "HRM")
    @DisplayName("평가 기간을 확정하면 상태가 CONFIRMED로 변경된다")
    void confirm_success() throws Exception {
        // given
        EvaluationPeriod period = repository.save(buildPeriod(EvalPeriodStatus.CLOSING));

        // when
        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/" + period.getEvalPeriodId() + "/confirm")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        EvaluationPeriod updated = repository.findById(period.getEvalPeriodId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EvalPeriodStatus.CONFIRMED);
    }

    @Test
    @WithMockUser(roles = "HRM")
    @DisplayName("평가 기간을 수정하면 날짜와 알고리즘 버전이 변경된다")
    void update_success() throws Exception {
        // given
        EvaluationPeriod period = repository.save(buildPeriod(EvalPeriodStatus.IN_PROGRESS));
        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30), 2L);

        // when
        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/" + period.getEvalPeriodId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        EvaluationPeriod updated = repository.findById(period.getEvalPeriodId()).orElseThrow();
        assertThat(updated.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(updated.getAlgorithmVersionId()).isEqualTo(2L);
    }
}
