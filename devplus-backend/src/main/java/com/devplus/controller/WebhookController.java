package com.devplus.controller;

import com.devplus.model.CommitLog;
import com.devplus.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @PostMapping("/github/{projectId}")
    public ResponseEntity<?> handleGithubWebhook(@PathVariable Long projectId,
                                                 @RequestBody String payload,
                                                 @RequestHeader(value = "X-GitHub-Event", required = false) String eventType) {
        
        log.info("Received GitHub webhook for project {}: Event type = {}", projectId, eventType);
        
        if (!"push".equals(eventType)) {
            log.info("Ignoring non-push event: {}", eventType);
            return ResponseEntity.ok(Map.of("message", "Event ignored", "eventType", eventType));
        }
        
        try {
            List<CommitLog> commits = webhookService.processGithubWebhook(projectId, payload);
            
            List<Map<String, Object>> commitSummaries = commits.stream()
                    .map(c -> Map.<String, Object>of(
                            "sha", c.getCommitSha().substring(0, Math.min(7, c.getCommitSha().length())),
                            "author", c.getGithubUsername(),
                            "message", c.getCommitMessage()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Webhook processed successfully",
                    "commitsProcessed", commits.size(),
                    "commits", commitSummaries
            ));
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Failed to process webhook",
                    "error", e.getMessage()
            ));
        }
    }
}