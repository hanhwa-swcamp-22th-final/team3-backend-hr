package com.ohgiraffers.team3backendhr.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> test(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("HR 인증 성공 | 사원코드: " + userDetails.getUsername()
                + " | 권한: " + userDetails.getAuthorities());
    }
}
