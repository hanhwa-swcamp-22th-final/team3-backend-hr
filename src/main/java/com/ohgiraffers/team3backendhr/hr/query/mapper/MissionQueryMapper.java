package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissionQueryMapper {

    List<MissionResponse> findAllByEmployeeId(@Param("employeeId") Long employeeId,
                                               @Param("status") String status,
                                               @Param("offset") int offset,
                                               @Param("size") int size);

    List<MissionResponse> findUpgradeByEmployeeId(@Param("employeeId") Long employeeId);
}
