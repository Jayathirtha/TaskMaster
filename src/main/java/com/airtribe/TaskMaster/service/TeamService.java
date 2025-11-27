package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.DTO.TeamDTO;
import com.airtribe.TaskMaster.model.Project;
import com.airtribe.TaskMaster.model.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public interface TeamService {
    // Team Management
    @Transactional
    Team createTeam(TeamDTO teamDTO, String creatorUsername);

    Team addMember(Long teamId, String memberUsername, String principalUsername);
    Optional<Team> getTeamById(Long teamId);
    List<Team> getUserTeams(String username);

    // Project Management
    Project createProject(Long teamId, Project project, String principalUsername);
    List<Project> getProjectsByTeam(Long teamId);
    Optional<Project> getProjectById(Long projectId);
}