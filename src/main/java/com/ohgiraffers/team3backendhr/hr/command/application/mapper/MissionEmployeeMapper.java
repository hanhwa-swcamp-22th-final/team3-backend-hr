package com.ohgiraffers.team3backendhr.hr.command.application.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MissionEmployeeMapper {

    @Select("""
            SELECT employee_id
            FROM employee
            WHERE employee_role = 'WORKER'
              AND employee_status = 'ACTIVE'
              AND employee_tier = #{tier}
            """)
    List<Long> findActiveWorkerIdsByTier(@Param("tier") String tier);

    @Select("""
            SELECT employee_id
            FROM employee
            WHERE employee_id = #{employeeId}
              AND employee_role = 'WORKER'
              AND employee_status = 'ACTIVE'
              AND employee_tier = #{tier}
            LIMIT 1
            """)
    Long findActiveWorkerIdByIdAndTier(@Param("employeeId") Long employeeId, @Param("tier") String tier);
}
