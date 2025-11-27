package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}