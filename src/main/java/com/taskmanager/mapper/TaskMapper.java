package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.entity.*;
import com.taskmanager.enums.*;

public class TaskMapper {

    public static Task toEntity(TaskCreateRequest dto) {
        Task t = new Task();
        t.setTitle(dto.getTitle());
        t.setDescription(dto.getDescription());
        t.setTags(dto.getTags());
        t.setDueDate(dto.getDueDate());

        // Convert enums safely
        if (dto.getPriority() != null)
            t.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));

        if (dto.getStatus() != null)
            t.setStatus(Status.valueOf(dto.getStatus().toUpperCase()));

        return t;
    }

    public static TaskResponse toResponse(Task t) {
        TaskResponse res = new TaskResponse();
        res.setId(t.getId());
        res.setTitle(t.getTitle());
        res.setDescription(t.getDescription());
        res.setPriority(t.getPriority().name());
        res.setStatus(t.getStatus().name());
        res.setTags(t.getTags());
        res.setCreatedAt(t.getCreatedAt());
        res.setUpdatedAt(t.getUpdatedAt());
        res.setDueDate(t.getDueDate());
        return res;
    }
}
