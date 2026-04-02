package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/disable-fk.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class QualitativeEvaluationRepositoryTest {

    @Autowired
    private QualitativeEvaluationRepository repository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    /**
     * 평가기간 생성 시 WORKER마다 level 1·2·3 레코드가 NO_INPUT 상태로 선생성된다.
     * evaluatorId, grade, inputMethod는 생성 시 null.
     */
    private QualitativeEvaluation buildEval(Long evaluateeId, Long periodId, Long level) {
        return QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(evaluateeId)
                .evaluationPeriodId(periodId)
                .evaluationLevel(level)
                .build(); // status 기본값 NO_INPUT
    }

    /* ── findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel ─────────
     * UI 응답용 조회가 아닌 Command 조회 — 차수 간 의존성 체크용.
     * 예: 2차 제출 전 Service에서 level=1 레코드가 SUBMITTED인지 확인할 때 사용.
     * ─────────────────────────────────────────────────────────────────── */

    @Test
    @DisplayName("피평가자ID·평가기간ID·차수로 레코드를 조회한다")
    void findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel_success() {
        // given — level 1·2·3 레코드 선생성
        repository.saveAll(List.of(
                buildEval(101L, 5L, 1L),
                buildEval(101L, 5L, 2L),
                buildEval(101L, 5L, 3L)));

        // when
        Optional<QualitativeEvaluation> result =
                repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(101L, 5L, 2L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEvaluationLevel()).isEqualTo(2L);
        assertThat(result.get().getStatus()).isEqualTo(QualEvalStatus.NO_INPUT);
    }

    @Test
    @DisplayName("존재하지 않는 차수 조회 시 empty를 반환한다")
    void findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel_whenNotFound_thenEmpty() {
        // given
        repository.saveAll(List.of(buildEval(101L, 5L, 1L)));

        // when — level 2는 저장하지 않음
        Optional<QualitativeEvaluation> result =
                repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(101L, 5L, 2L);

        // then
        assertThat(result).isEmpty();
    }
}
