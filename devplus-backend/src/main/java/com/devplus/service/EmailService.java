package com.devplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Async
    public void sendWeeklyReport(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@devplus.com");
            
            mailSender.send(message);
            log.info("Weekly report email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public String buildWeeklyReportHtml(String managerName, java.util.List<ProjectReport> projectReports) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }");
        html.append(".container { max-width: 800px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; border-bottom: 2px solid #7c3aed; padding-bottom: 20px; margin-bottom: 30px; }");
        html.append(".header h1 { color: #7c3aed; margin: 0; }");
        html.append(".header p { color: #666; margin: 10px 0 0 0; }");
        html.append(".project-section { margin-bottom: 40px; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; }");
        html.append(".project-header { background: linear-gradient(135deg, #7c3aed 0%, #5b21b6 100%); color: white; padding: 15px 20px; }");
        html.append(".project-header h2 { margin: 0; font-size: 18px; }");
        html.append(".project-header a { color: #e0e7ff; }");
        html.append(".project-content { padding: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th { background: #f3f4f6; padding: 12px; text-align: left; font-weight: 600; color: #374151; }");
        html.append("td { padding: 12px; border-bottom: 1px solid #e5e7eb; }");
        html.append("tr:hover { background: #f9fafb; }");
        html.append(".stats-row { display: flex; gap: 20px; margin: 15px 0; }");
        html.append(".stat-box { flex: 1; background: #f3f4f6; padding: 15px; border-radius: 8px; text-align: center; }");
        html.append(".stat-box h3 { margin: 0; color: #7c3aed; font-size: 24px; }");
        html.append(".stat-box p { margin: 5px 0 0 0; color: #6b7280; font-size: 14px; }");
        html.append(".highlight { background: #ecfdf5; border-left: 4px solid #22c55e; padding: 15px; margin: 15px 0; border-radius: 0 8px 8px 0; }");
        html.append(".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 15px 0; border-radius: 0 8px 8px 0; }");
        html.append(".inactive-list { margin: 10px 0; padding-left: 20px; }");
        html.append(".inactive-list li { margin: 5px 0; color: #666; }");
        html.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }");
        html.append("</style></head><body>");
        
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>📊 DevPlus Weekly Report</h1>");
        html.append("<p>Hello ").append(managerName).append(", here's your weekly team summary</p>");
        html.append("</div>");
        
        for (ProjectReport report : projectReports) {
            html.append("<div class='project-section'>");
            html.append("<div class='project-header'>");
            html.append("<h2>").append(escapeHtml(report.getProjectName())).append("</h2>");
            html.append("<a href='").append(escapeHtml(report.getRepoUrl())).append("' target='_blank'>")
               .append(escapeHtml(report.getRepoUrl())).append("</a>");
            html.append("</div>");
            html.append("<div class='project-content'>");
            html.append("<p><strong>Report Period:</strong> ").append(report.getReportPeriod()).append("</p>");
            
            // Member Summary Table
            if (!report.getMemberStats().isEmpty()) {
                html.append("<table>");
                html.append("<tr><th>Member Name</th><th>Commits (Week)</th><th>Tasks Done This Week</th></tr>");
                for (MemberStat stat : report.getMemberStats()) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(stat.getName())).append("</td>");
                    html.append("<td>").append(stat.getWeeklyCommits()).append("</td>");
                    html.append("<td>").append(stat.getWeeklyTasksDone()).append(" / ").append(stat.getTotalTasks()).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }
            
            // Highlights
            if (report.getTopContributor() != null) {
                html.append("<div class='highlight'>");
                html.append("<strong>🏆 Top Contributor This Week:</strong> ")
                   .append(escapeHtml(report.getTopContributor())).append(" (").append(report.getTopContributorCommits()).append(" commits)");
                html.append("</div>");
            }
            
            if (report.getMostTasksCompleted() != null) {
                html.append("<div class='highlight'>");
                html.append("<strong>✅ Most Tasks Completed:</strong> ")
                   .append(escapeHtml(report.getMostTasksCompleted())).append(" (").append(report.getMostTasksCompletedCount()).append(" tasks)");
                html.append("</div>");
            }
            
            // Inactive Members Warning
            if (!report.getInactiveMembers().isEmpty()) {
                html.append("<div class='warning'>");
                html.append("<strong>⚠️ Inactive Members (0 commits this week):</strong>");
                html.append("<ul class='inactive-list'>");
                for (String inactive : report.getInactiveMembers()) {
                    html.append("<li>").append(escapeHtml(inactive)).append("</li>");
                }
                html.append("</ul>");
                html.append("</div>");
            }
            
            // Summary Stats
            html.append("<div class='stats-row'>");
            html.append("<div class='stat-box'><h3>").append(report.getTotalCommits()).append("</h3><p>Total Commits This Week</p></div>");
            html.append("<div class='stat-box'><h3>").append(report.getTotalTasksCompleted()).append("</h3><p>Tasks Completed This Week</p></div>");
            html.append("</div>");
            
            html.append("</div>");
            html.append("</div>");
        }
        
        html.append("<div class='footer'>");
        html.append("<p>Generated by DevPlus • ").append(java.time.LocalDate.now()).append("</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("&quot;", "&quot;")
                   .replace("'", "&#39;");
    }
    
    // Inner classes for report data
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ProjectReport {
        private String projectName;
        private String repoUrl;
        private String reportPeriod;
        private java.util.List<MemberStat> memberStats;
        private String topContributor;
        private Integer topContributorCommits;
        private String mostTasksCompleted;
        private Integer mostTasksCompletedCount;
        private java.util.List<String> inactiveMembers;
        private Long totalCommits;
        private Long totalTasksCompleted;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class MemberStat {
        private String name;
        private Long weeklyCommits;
        private Integer weeklyTasksDone;
        private Integer totalTasks;
    }
}