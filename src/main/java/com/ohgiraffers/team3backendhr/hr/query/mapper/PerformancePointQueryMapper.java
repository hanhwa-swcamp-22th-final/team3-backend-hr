package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.PointHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PerformancePointQueryMapper {

    PointSummaryResponse findSummaryByEmployeeId(@Param("employeeId") Long employeeId);

    List<PointHistoryResponse> findHistoryByEmployeeId(@Param("employeeId") Long employeeId);
}
