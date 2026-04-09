package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaAttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByFileGroupId(Long fileGroupId);

    @Modifying
    @Query("UPDATE Attachment a SET a.isDeleted = 1, a.deletedAt = CURRENT_TIMESTAMP WHERE a.fileGroupId = :fileGroupId")
    void deleteByFileGroupId(@Param("fileGroupId") Long fileGroupId);
}
