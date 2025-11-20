package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.services.TaskService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    // CREATE TASK
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request) {

        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(201).body(response);
    }

    // GET TASK BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Integer id) {

        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    // UPDATE TASK
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Integer id,
            @RequestBody TaskUpdateRequest request) {

        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Integer id) {

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // GET TASK LIST WITH FILTER
    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority
    ) {
        List<TaskResponse> response = taskService.listTasks(status, priority);
        return ResponseEntity.ok(response);
    }

    // SEARCH TASK WITH PARAMS
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate before,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate after
    ) {
        List<TaskResponse> response = taskService.searchTasks(keyword, tag, before, after);
        return ResponseEntity.ok(response);
    }

    // GET SUMMARY
    @GetMapping("/summary")
    public ResponseEntity<TaskSummaryResponse> getSummary() {
        TaskSummaryResponse summary = taskService.getSummary();
        return ResponseEntity.ok(summary);
    }

    // RECOMMENDED TASKS
    @GetMapping("/recommended")
    public ResponseEntity<List<TaskResponse>> getRecommendedTasks() {
        List<TaskResponse> response = taskService.getRecommendedTasks();
        return ResponseEntity.ok(response);
    }

    // PAGINATION TASK
    @GetMapping("/page")
    public ResponseEntity<TaskPageResponse> getPaginatedTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        TaskPageResponse response = taskService.getPaginatedTasks(page, size);
        return ResponseEntity.ok(response);
    }

}
