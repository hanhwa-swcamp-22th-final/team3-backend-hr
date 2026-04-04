package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationQueryMapper {

    List<NotificationResponse> findVisibleByEmployeeId(@Param("employeeId") Long employeeId);

    NotificationSummaryResponse findSummaryByEmployeeId(@Param("employeeId") Long employeeId);
}
