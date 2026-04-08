package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgQueryService {

    private final AdminClient adminClient;

    /* HR-073 */
    public OrgUnitTreeResponse getOrgTree() {
        return adminClient.getOrgTree();
    }

    /* HR-074 */
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        return adminClient.getEmployees(departmentId, teamId, keyword, page, size);
    }

    /* HR-075 */
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        return adminClient.getTeamMembers(teamId);
    }

    /* HR-082 */
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        return adminClient.getDepartmentDetail(departmentId);
    }
}
