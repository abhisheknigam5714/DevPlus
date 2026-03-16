package com.devplus.repository;

import com.devplus.model.CommitLog;
import com.devplus.model.Project;
import com.devplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommitLogRepository extends JpaRepository<CommitLog, Long> {
    
    List<CommitLog> findByProjectOrderByCommittedAtDesc(Project project);
    
    List<CommitLog> findByUserOrderByCommittedAtDesc(User user);
    
    List<CommitLog> findByProjectAndUserOrderByCommittedAtDesc(Project project, User user);
    
    @Query("SELECT c FROM CommitLog c WHERE c.project = :project ORDER BY c.committedAt DESC LIMIT :limit")
    List<CommitLog> findTopByProject(@Param("project") Project project, @Param("limit") int limit);
    
    @Query("SELECT COUNT(c) FROM CommitLog c WHERE c.user = :user AND c.project = :project")
    Long countByUserAndProject(@Param("user") User user, @Param("project") Project project);
    
    @Query("SELECT COUNT(c) FROM CommitLog c WHERE c.user = :user AND c.project = :project AND c.committedAt BETWEEN :start AND :end")
    Long countByUserAndProjectAndCommittedAtBetween(@Param("user") User user, @Param("project") Project project, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(c) FROM CommitLog c WHERE c.project = :project AND c.committedAt BETWEEN :start AND :end")
    Long countByProjectAndCommittedAtBetween(@Param("project") Project project, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT c FROM CommitLog c WHERE c.project = :project AND c.committedAt BETWEEN :start AND :end ORDER BY c.committedAt DESC")
    List<CommitLog> findByProjectAndCommittedAtBetween(@Param("project") Project project, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT c FROM CommitLog c WHERE c.user = :user ORDER BY c.committedAt DESC LIMIT 1")
    CommitLog findLatestByUser(@Param("user") User user);
    
    @Query("SELECT c FROM CommitLog c WHERE c.project = :project AND c.user = :user ORDER BY c.committedAt DESC LIMIT 1")
    CommitLog findLatestByProjectAndUser(@Param("project") Project project, @Param("user") User user);
}