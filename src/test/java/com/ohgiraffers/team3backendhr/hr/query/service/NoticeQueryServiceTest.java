package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NoticeQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock
    private NoticeQueryMapper noticeQueryMapper;

    @InjectMocks
    private NoticeQueryService noticeQueryService;

    @Test
    @DisplayName("공지 목록 조회 — 페이지와 사이즈를 offset으로 변환해 mapper에 전달한다")
    void getNotices_delegatesToMapper() {
        // given
        NoticeListResponse item = new NoticeListResponse(
                1L, "공지", 99L, "POSTING", 0, 0L,
                LocalDateTime.now(), null, null);
        given(noticeQueryMapper.findNotices("키워드", "POSTING", null, 20, 10))
                .willReturn(List.of(item));

        // when
        List<NoticeListResponse> result = noticeQueryService.getNotices("키워드", null, "POSTING", 2, 10);

        // then
        assertThat(result).hasSize(1);
        verify(noticeQueryMapper).findNotices("키워드", "POSTING", null, 20, 10);
    }

    @Test
    @DisplayName("공지 상세 조회 — 존재하는 공지 반환")
    void getNoticeDetail_found_returnsResponse() {
        // given
        NoticeDetailResponse response = new NoticeDetailResponse(
                1L, "제목", "내용", 99L, "POSTING", 0, 5L,
                LocalDateTime.now(), null, null);
        given(noticeQueryMapper.findById(1L)).willReturn(response);

        // when
        NoticeDetailResponse result = noticeQueryService.getNoticeDetail(1L);

        // then
        assertThat(result.getNoticeId()).isEqualTo(1L);
        assertThat(result.getNoticeTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("공지 상세 조회 — 존재하지 않으면 예외 발생")
    void getNoticeDetail_notFound_throwsException() {
        // given
        given(noticeQueryMapper.findById(9999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> noticeQueryService.getNoticeDetail(9999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고정 공지 조회 — mapper 결과를 그대로 반환한다")
    void getPinnedNotice_returnsMapperResult() {
        // given
        NoticePinnedResponse response = new NoticePinnedResponse(
                1L, "중요 공지", "내용", LocalDateTime.now(), null);
        given(noticeQueryMapper.findPinned()).willReturn(response);

        // when
        NoticePinnedResponse result = noticeQueryService.getPinnedNotice();

        // then
        assertThat(result.getNoticeId()).isEqualTo(1L);
        assertThat(result.getNoticeTitle()).isEqualTo("중요 공지");
    }

    @Test
    @DisplayName("고정 공지 없으면 null 반환")
    void getPinnedNotice_noneExists_returnsNull() {
        // given
        given(noticeQueryMapper.findPinned()).willReturn(null);

        // when
        NoticePinnedResponse result = noticeQueryService.getPinnedNotice();

        // then
        assertThat(result).isNull();
    }
}
