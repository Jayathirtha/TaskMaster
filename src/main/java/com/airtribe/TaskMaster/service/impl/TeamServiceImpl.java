package com.airtribe.TaskMaster.service.impl;

import com.airtribe.TaskMaster.DTO.TeamDTO;
import com.airtribe.TaskMaster.model.Project;
import com.airtribe.TaskMaster.model.Team;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.repository.ProjectRepository;
import com.airtribe.TaskMaster.repository.TeamRepository;
import com.airtribe.TaskMaster.repository.UserRepository;
import com.airtribe.TaskMaster.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for Team and Project Management. (SRP: Collaboration Setup)
 */
@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private void checkMembership(Team team, String username) {
        boolean isMember = team.getMembers().stream()
                .anyMatch(u -> u.getUsername().equals(username));
        if (!isMember) {
            throw new SecurityException("User " + username + " is not a member of team " + team.getName());
        }
    }

    // --- Team Management ---

    @Transactional
    @Override
    public Team createTeam(TeamDTO teamDTO, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator not found: " + creatorUsername));

        if (teamRepository.findByName(teamDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Team name already exists.");
        }

        // Initialize members with the creator
        Set<User> members = new HashSet<>();
        members.add(creator);

        // Add optional members specified in DTO
        if (teamDTO.getMemberUsernames() != null) {
            for (String memberUsername : teamDTO.getMemberUsernames()) {
                userRepository.findByUsername(memberUsername).ifPresent(members::add);
            }
        }

        Team newTeam = Team.builder()
                .name(teamDTO.getName())
                .description(teamDTO.getDescription())
                .members(members)
                .build();

        Team savedTeam = teamRepository.save(newTeam);

        // Ensure reverse relationship is also updated
        members.forEach(m -> m.getTeams().add(savedTeam));
        userRepository.saveAll(members);

        return savedTeam;
    }

    @Override
    @Transactional
    public Team addMember(Long teamId, String memberUsername, String principalUsername) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        checkMembership(team, principalUsername); // Only existing members can add others

        User newMember = userRepository.findByUsername(memberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + memberUsername));

        team.getMembers().add(newMember);
        newMember.getTeams().add(team); // Maintain bi-directional relationship

        userRepository.save(newMember);
        return teamRepository.save(team);
    }

    @Override
    public Optional<Team> getTeamById(Long teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    public List<Team> getUserTeams(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Lazily loaded collection, converts Set to List for API simplicity
        return user.getTeams().stream().collect(Collectors.toList());
    }


    @Override
    @Transactional
    public Project createProject(Long teamId, Project project, String principalUsername) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        checkMembership(team, principalUsername); // Only members can create projects

        project.setTeam(team);
        team.getProjects().add(project);

        return projectRepository.save(project);
    }

    @Override
    public List<Project> getProjectsByTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        // Returns the Set as a List
        return team.getProjects().stream().collect(Collectors.toList());
    }

    @Override
    public Optional<Project> getProjectById(Long projectId) {
        return projectRepository.findById(projectId);
    }
}