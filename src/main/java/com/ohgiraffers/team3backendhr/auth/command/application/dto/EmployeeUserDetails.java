package com.ohgiraffers.team3backendhr.auth.command.application.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class EmployeeUserDetails extends User {

    private final Long employeeId;

    public EmployeeUserDetails(Long employeeId, String employeeCode, String password,
                                Collection<? extends GrantedAuthority> authorities) {
        super(employeeCode, password, authorities);
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}
