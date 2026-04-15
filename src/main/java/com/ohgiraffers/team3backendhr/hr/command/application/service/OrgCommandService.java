package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentLeaderAssignRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamMemberAddRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrgCommandService {

    private final AdminClient adminClient;

    /* HR-076 */
    public Long createDepartment(DepartmentCreateRequest request) {
        return adminClient.createDepartment(request);
    }

    /* HR-077 */
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        return adminClient.updateDepartment(departmentId, request);
    }

    /* HR-078 */
    public void deleteDepartment(Long departmentId) {
        adminClient.deleteDepartment(departmentId);
    }

    /* HR-079 */
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        return adminClient.createTeam(departmentId, request);
    }

    /* HR-080 */
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        return adminClient.updateTeam(teamId, request);
    }

    /* HR-081 */
    public void deleteTeam(Long teamId) {
        adminClient.deleteTeam(teamId);
    }

    /* HR-083 */
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
        adminClient.addTeamMembers(teamId, request);
    }

    /* HR-084 */
    public void removeTeamMember(Long teamId, Long employeeId) {
        adminClient.removeTeamMember(teamId, employeeId);
    }

    /* HR-085 */
    public Long assignDepartmentLeader(Long departmentId, DepartmentLeaderAssignRequest request) {
        return adminClient.assignDepartmentLeader(departmentId, request);
    }
}
