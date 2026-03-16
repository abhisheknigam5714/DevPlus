package com.devplus.repository;

import com.devplus.model.Project;
import com.devplus.model.Status;
import com.devplus.model.Task;
import com.devplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByProject(Project project);
    
    List<Task> findByAssignedTo(User user);
    
    List<Task> findByProjectAndAssignedTo(Project project, User user);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.project = :project")
    Long countByAssignedToAndProject(@Param("user") User user, @Param("project") Project project);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.project = :project AND t.status = :status")
    Long countByAssignedToAndProjectAndStatus(@Param("user") User user, @Param("project") Project project, @Param("status") Status status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.status = :status AND t.updatedAt BETWEEN :start AND :end")
    Long countByAssignedToAndStatusAndUpdatedAtBetween(@Param("user") User user, @Param("status") Status status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.updatedAt BETWEEN :start AND :end AND t.status = 'DONE'")
    List<Task> findByProjectAndUpdatedAtBetweenAndStatus(@Param("project") Project project, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}