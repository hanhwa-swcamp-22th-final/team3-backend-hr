package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeQueryMapper {

    List<NoticeListResponse> findNotices(@Param("keyword") String keyword,
                                         @Param("status") String status,
                                         @Param("isImportant") Boolean isImportant,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    long countNotices(@Param("keyword") String keyword,
                      @Param("status") String status,
                      @Param("isImportant") Boolean isImportant);

    NoticeDetailResponse findById(@Param("noticeId") Long noticeId);

    NoticePinnedResponse findPinned();
}
