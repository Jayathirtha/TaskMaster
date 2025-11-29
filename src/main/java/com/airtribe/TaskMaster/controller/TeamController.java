package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.DTO.TeamDTO;
import com.airtribe.TaskMaster.model.Project;
import com.airtribe.TaskMaster.model.Team;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.service.TeamService;
import com.sun.security.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Team and Project Management.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ) {// || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new SecurityException("User not authenticated.");
        }
        return ((User) authentication.getPrincipal()).getUsername();
    }

    // --- Team Endpoints ---

    /**
     * Creates a new team and add the current user as a member.
     * POST /api/teams
     */
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody TeamDTO teamDTO) {
        try {
            Team team = teamService.createTeam(teamDTO, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Team created successfully",
                    "teamId", team.getTeamId(),
                    "teamName", team.getName()
            ));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Adds a member to an existing team.
     * POST /api/teams/{teamId}/members?username=john.doe
     */
    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long teamId, @RequestParam String username) {
        try {
            Team team = teamService.addMember(teamId, username, getCurrentUsername());
            return ResponseEntity.ok(Map.of(
                    "message", username + " added to team " + team.getName()
            ));
        } catch (SecurityException | UsernameNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves all teams the current user is a member of.
     * GET /api/teams/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<Team>> getMyTeams() {
        try {
            List<Team> teams = teamService.getUserTeams(getCurrentUsername());
            return ResponseEntity.ok(teams);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // --- Project Endpoints ---

    /**
     * Creates a new project under a specific team.
     * POST /api/teams/{teamId}/projects
     */
    @PostMapping("/{teamId}/projects")
    public ResponseEntity<?> createProject(@PathVariable() Long teamId, @RequestBody Project project) {
        try {
            Project newProject = teamService.createProject(teamId, project, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Project created successfully",
                    "projectId", newProject.getId(),
                    "projectName", newProject.getName()
            ));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves all projects belonging to a specific team.
     * GET /api/teams/{teamId}/projects
     */
    @GetMapping("/{teamId}/projects")
    public ResponseEntity<?> getProjects(@PathVariable Long teamId) {
        try {
            List<Project> projects = teamService.getProjectsByTeam(teamId);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}