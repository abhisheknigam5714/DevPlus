package com.devplus.controller;

import com.devplus.dto.MemberStatsResponse;
import com.devplus.model.User;
import com.devplus.service.AuthService;
import com.devplus.service.CommitService;
import com.devplus.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {
    
    private final CommitService commitService;
    private final ProjectService projectService;
    private final AuthService authService;
    
    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<?> getProjectStats(@PathVariable Long projectId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<MemberStatsResponse> stats = commitService.getMemberStats(projectId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/projects/{projectId}/members/{userId}/stats")
    public ResponseEntity<?> getMemberStats(@PathVariable Long projectId,
                                            @PathVariable Long userId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        // Get all stats and filter for specific user
        List<MemberStatsResponse> allStats = commitService.getMemberStats(projectId);
        MemberStatsResponse memberStats = allStats.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (memberStats == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(memberStats);
    }
}