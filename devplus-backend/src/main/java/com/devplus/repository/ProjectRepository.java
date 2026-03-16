package com.devplus.repository;

import com.devplus.model.Project;
import com.devplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByCreatedBy(User user);
    
    List<Project> findByMembers_User(User user);
}