package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByNoticeStatus(NoticeStatus status);
    List<Notice> findByEmployeeId(Long employeeId);
}
