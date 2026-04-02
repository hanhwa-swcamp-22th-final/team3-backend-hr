CREATE TABLE IF NOT EXISTS evaluation_period (
    eval_period_id       BIGINT       NOT NULL PRIMARY KEY,
    algorithm_version_id BIGINT       NOT NULL,
    eval_year            INT          NOT NULL,
    eval_sequence        INT          NOT NULL,
    eval_type            VARCHAR(20)  NOT NULL,
    start_date           DATE         NOT NULL,
    end_date             DATE         NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    created_at           TIMESTAMP    NULL,
    created_by           BIGINT       NULL,
    updated_at           TIMESTAMP    NULL,
    updated_by           BIGINT       NULL
);

CREATE TABLE IF NOT EXISTS qualitative_evaluation (
    qualitative_evaluation_id BIGINT        NOT NULL PRIMARY KEY,
    evaluatee_id              BIGINT        NOT NULL,
    evaluator_id              BIGINT        NULL,
    evaluation_period_id      BIGINT        NOT NULL,
    evaluation_level          BIGINT        NULL,
    eval_items                VARCHAR(2000) NULL,
    eval_comment              VARCHAR(2000) NULL,
    grade                     VARCHAR(5)    NULL,
    score                     DECIMAL(10,2) NULL,
    input_method              VARCHAR(20)   NULL,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'NO_INPUT',
    created_at                TIMESTAMP     NULL,
    created_by                BIGINT        NULL,
    updated_at                TIMESTAMP     NULL,
    updated_by                BIGINT        NULL
);

CREATE TABLE IF NOT EXISTS employee (
    employee_id               BIGINT       NOT NULL PRIMARY KEY,
    department_id             BIGINT       NOT NULL,
    employee_code             VARCHAR(255) NULL,
    employee_name             VARCHAR(255) NULL,
    employee_email            VARCHAR(255) NULL,
    employee_phone            VARCHAR(255) NULL,
    employee_address          VARCHAR(255) NULL,
    employee_tier             VARCHAR(5)   NULL,
    employee_emergency_contact VARCHAR(30) NULL,
    employee_password         VARCHAR(255) NULL,
    employee_role             VARCHAR(20)  NOT NULL,
    employee_status           VARCHAR(20)  NOT NULL,
    mfa_enabled               BOOLEAN      NULL,
    login_fail_count          INT          NULL,
    is_locked                 BOOLEAN      NULL,
    last_login_at             TIMESTAMP    NULL,
    created_at                TIMESTAMP    NULL,
    created_by                BIGINT       NULL,
    updated_at                TIMESTAMP    NULL,
    updated_by                BIGINT       NULL
);
