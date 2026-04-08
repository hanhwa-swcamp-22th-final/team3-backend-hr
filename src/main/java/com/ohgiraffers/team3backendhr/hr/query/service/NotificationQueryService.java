package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NotificationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationQueryMapper notificationQueryMapper;

    public List<NotificationResponse> getVisibleNotifications(Long employeeId) {
        return notificationQueryMapper.findVisibleByEmployeeId(employeeId);
    }

    public NotificationSummaryResponse getSummary(Long employeeId) {
        return notificationQueryMapper.findSummaryByEmployeeId(employeeId);
    }
}
