package com.devplus.service;

import com.devplus.model.CommitLog;
import com.devplus.model.Project;
import com.devplus.model.User;
import com.devplus.repository.CommitLogRepository;
import com.devplus.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final ProjectService projectService;
    private final CommitLogRepository commitLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public List<CommitLog> processGithubWebhook(Long projectId, String payload) {
        Project project = projectService.getProjectById(projectId);
        List<CommitLog> savedCommits = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(payload);
            
            // Extract branch from ref
            String ref = root.path("ref").asText("");
            String branch = extractBranchName(ref);
            
            // Process each commit
            JsonNode commitsNode = root.path("commits");
            if (commitsNode.isArray()) {
                for (JsonNode commitNode : commitsNode) {
                    CommitLog commitLog = processCommit(commitNode, project, branch);
                    if (commitLog != null) {
                        savedCommits.add(commitLogRepository.save(commitLog));
                    }
                }
            }
            
            log.info("Processed {} commits for project {}", savedCommits.size(), projectId);
            
        } catch (Exception e) {
            log.error("Error processing webhook payload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process webhook payload", e);
        }
        
        return savedCommits;
    }
    
    private CommitLog processCommit(JsonNode commitNode, Project project, String branch) {
        try {
            String commitSha = commitNode.path("id").asText("");
            String message = commitNode.path("message").asText("");
            
            // Extract author info
            JsonNode authorNode = commitNode.path("author");
            String githubUsername = authorNode.path("username").asText("");
            String authorName = authorNode.path("name").asText("");
            
            // Parse timestamp
            String timestampStr = commitNode.path("timestamp").asText("");
            LocalDateTime committedAt = parseTimestamp(timestampStr);
            
            // Try to find matching user
            User user = null;
            if (githubUsername != null && !githubUsername.isEmpty()) {
                user = userRepository.findByGithubUsername(githubUsername).orElse(null);
            }
            
            return CommitLog.builder()
                    .project(project)
                    .user(user)
                    .githubUsername(githubUsername != null && !githubUsername.isEmpty() ? githubUsername : authorName)
                    .commitSha(commitSha)
                    .commitMessage(message.length() > 500 ? message.substring(0, 500) : message)
                    .branchName(branch)
                    .committedAt(committedAt != null ? committedAt : LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing commit: {}", e.getMessage());
            return null;
        }
    }
    
    private String extractBranchName(String ref) {
        if (ref == null || ref.isEmpty()) {
            return "unknown";
        }
        // ref format: refs/heads/branch-name
        String[] parts = ref.split("/");
        if (parts.length >= 3) {
            return parts[parts.length - 1];
        }
        return ref;
    }
    
    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            // ISO 8601 format: 2024-01-15T10:30:00Z
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(timestamp.replace("Z", ""), formatter);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestamp);
            return LocalDateTime.now();
        }
    }
}