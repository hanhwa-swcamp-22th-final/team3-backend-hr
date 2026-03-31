package com.ohgiraffers.team3backendhr.auth.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "employee_password")
    private String employeePassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role")
    private EmployeeRole employeeRole;
}
