package com.devplus.dto;

import com.devplus.model.Priority;
import com.devplus.model.Status;
import com.devplus.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    private Long assignedToId;
    
    private Priority priority;
    
    private LocalDate dueDate;
    
    private Status status;
}