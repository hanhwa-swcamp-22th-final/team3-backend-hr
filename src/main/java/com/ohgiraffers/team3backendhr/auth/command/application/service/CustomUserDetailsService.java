package com.ohgiraffers.team3backendhr.auth.command.application.service;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.auth.command.domain.aggregate.Employee;
import com.ohgiraffers.team3backendhr.auth.command.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeCode) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new UsernameNotFoundException("사원을 찾을 수 없습니다: " + employeeCode));

        return new EmployeeUserDetails(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getEmployeePassword(),
                Collections.singleton(new SimpleGrantedAuthority(employee.getEmployeeRole().name()))
        );
    }
}
