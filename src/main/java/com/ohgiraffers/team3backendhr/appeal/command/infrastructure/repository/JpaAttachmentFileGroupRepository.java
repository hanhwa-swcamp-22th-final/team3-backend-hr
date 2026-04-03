package com.ohgiraffers.team3backendhr.appeal.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate.AttachmentFileGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAttachmentFileGroupRepository extends JpaRepository<AttachmentFileGroup, Long> {
}
