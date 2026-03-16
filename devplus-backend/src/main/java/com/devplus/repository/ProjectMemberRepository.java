package com.devplus.repository;

import com.devplus.model.Project;
import com.devplus.model.ProjectMember;
import com.devplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    
    List<ProjectMember> findByProject(Project project);
    
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    
    void deleteByProjectAndUser(Project project, User user);
    
    @Query("SELECT pm.project FROM ProjectMember pm WHERE pm.user = :user")
    List<Project> findProjectsByUser(@Param("user") User user);
    
    @Query("SELECT pm.user FROM ProjectMember pm WHERE pm.project = :project AND pm.roleInProject = :role")
    List<User> findUsersByProjectAndRole(@Param("project") Project project, @Param("role") ProjectMember.RoleInProject role);
    
    boolean existsByProjectAndUser(Project project, User user);
}