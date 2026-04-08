package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByFileGroupId(Long fileGroupId);
}
