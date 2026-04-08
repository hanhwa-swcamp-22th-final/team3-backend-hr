package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.MissionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionQueryService {

    private final MissionQueryMapper missionQueryMapper;

    public List<MissionResponse> getMissions(Long employeeId, String status, int page, int size) {
        return missionQueryMapper.findAllByEmployeeId(employeeId, status, page * size, size);
    }

    public List<MissionResponse> getUpgradeMissions(Long employeeId) {
        return missionQueryMapper.findUpgradeByEmployeeId(employeeId);
    }
}
