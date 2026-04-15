package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NotificationQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private static final Pattern EMPLOYEE_ID_LABEL_PATTERN = Pattern.compile("(?<!\\d)(\\d{13,})(번 직원)");

    private final NotificationQueryMapper notificationQueryMapper;
    private final AdminClient adminClient;

    public List<NotificationResponse> getVisibleNotifications(Long employeeId) {
        Map<Long, String> employeeDisplayCache = new HashMap<>();
        return notificationQueryMapper.findVisibleByEmployeeId(employeeId).stream()
                .map(notification -> notification.withDisplayText(
                        replaceEmployeeIds(notification.getNotificationTitle(), employeeDisplayCache),
                        replaceEmployeeIds(notification.getNotificationContent(), employeeDisplayCache)
                ))
                .toList();
    }

    public NotificationSummaryResponse getSummary(Long employeeId) {
        return notificationQueryMapper.findSummaryByEmployeeId(employeeId);
    }

    private String replaceEmployeeIds(String text, Map<Long, String> employeeDisplayCache) {
        if (text == null || text.isBlank()) {
            return text;
        }

        Matcher matcher = EMPLOYEE_ID_LABEL_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Long employeeId = Long.valueOf(matcher.group(1));
            String displayName = resolveEmployeeDisplayName(employeeId, employeeDisplayCache);
            matcher.appendReplacement(result, Matcher.quoteReplacement(displayName + " 직원"));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveEmployeeDisplayName(Long employeeId, Map<Long, String> employeeDisplayCache) {
        if (employeeDisplayCache.containsKey(employeeId)) {
            return employeeDisplayCache.get(employeeId);
        }

        String displayName = String.valueOf(employeeId);
        try {
            EmployeeProfileResponse profile = adminClient.getWorkerProfile(employeeId);
            if (profile != null) {
                String code = profile.getEmployeeCode();
                String name = profile.getEmployeeName();
                if (code != null && !code.isBlank() && name != null && !name.isBlank()) {
                    displayName = code + " " + name;
                } else if (name != null && !name.isBlank()) {
                    displayName = name;
                } else if (code != null && !code.isBlank()) {
                    displayName = code;
                }
            }
        } catch (RuntimeException ignored) {
            // 알림 조회 자체는 유지하고, Admin 조회 실패 시 기존 ID 표시로 fallback한다.
        }
        employeeDisplayCache.put(employeeId, displayName);
        return displayName;
    }
}
