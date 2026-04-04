package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNoticeTargetRepository extends JpaRepository<NoticeTarget, Long> {
    List<NoticeTarget> findByNoticeId(Long noticeId);
}
