package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAttachmentFileGroupRepository extends JpaRepository<AttachmentFileGroup, Long> {
    Optional<AttachmentFileGroup> findByReferenceIdAndReferenceType(Long referenceId, ReferenceType referenceType);
    List<AttachmentFileGroup> findByReferenceType(ReferenceType referenceType);
}
