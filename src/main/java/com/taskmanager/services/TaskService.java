package com.taskmanager.services;

import com.taskmanager.dto.*;
import com.taskmanager.enums.Priority;
import com.taskmanager.enums.Status;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.CsvExportException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repos.TaskRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    // CREATE TASK
    @Transactional
    public TaskResponse createTask(TaskCreateRequest dto) {

        Task task = TaskMapper.toEntity(dto);

        LocalDateTime now = LocalDateTime.now();

        // Validate due date
        if (task.getDueDate() != null && task.getDueDate().isBefore(now.toLocalDate())) {
            throw new IllegalArgumentException("dueDate must not be earlier than createdAt");
        }

        Task saved = repo.save(task);

        return TaskMapper.toResponse(saved);
    }

    // GET TASK
    public TaskResponse getTaskById(Integer id) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        return TaskMapper.toResponse(task);
    }

    // UPDATE TASK
    @Transactional
    public TaskResponse updateTask(Integer id, TaskUpdateRequest dto) {

        Task existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // --- Update only non-null fields from DTO ---

        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());

        if (dto.getTags() != null) existing.setTags(dto.getTags());

        if (dto.getPriority() != null)
            existing.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));

        if (dto.getStatus() != null)
            existing.setStatus(Status.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getDueDate() != null) {

            // Validate due date
            LocalDate createdDate = existing.getCreatedAt().toLocalDate();
            if (dto.getDueDate().isBefore(createdDate)) {
                throw new IllegalArgumentException("dueDate cannot be earlier than createdAt");
            }

            existing.setDueDate(dto.getDueDate());
        }

        Task updated = repo.save(existing);

        return TaskMapper.toResponse(updated);
    }

    // DELETE TASK
    @Transactional
    public void deleteTask(Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        repo.deleteById(id);
    }

    // GET TASK LIST WITH FILTER
    public List<TaskResponse> listTasks(String status, String priority) {

        List<Task> tasks = repo.findAll();

        return tasks.stream()
                .filter(t -> status == null || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> priority == null || t.getPriority().name().equalsIgnoreCase(priority))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(TaskMapper::toResponse)
                .toList();
    }

    // SEARCH TASK WITH PARAMS
    public List<TaskResponse> searchTasks(
            String keyword,
            String tag,
            LocalDate before,
            LocalDate after
    ) {

        List<Task> tasks = repo.findAll();

        return tasks.stream()
                // KEYWORD FILTER
                .filter(t -> keyword == null ||
                        (t.getTitle() != null && t.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                )
                // TAG FILTER
                .filter(t -> tag == null ||
                        (t.getTags() != null && t.getTags().toLowerCase().contains(tag.toLowerCase()))
                )
                // AFTER DATE
                .filter(t -> after == null ||
                        (t.getDueDate() != null && !t.getDueDate().isBefore(after))
                )
                // BEFORE DATE
                .filter(t -> before == null ||
                        (t.getDueDate() != null && !t.getDueDate().isAfter(before))
                )
                .sorted(
                        Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(t -> t.getPriority().rank)
                )
                .map(TaskMapper::toResponse)
                .toList();
    }

    // GET SUMMARY
    public TaskSummaryResponse getSummary() {

        List<Task> tasks = repo.findAll();
        LocalDate today = LocalDate.now();

        TaskSummaryResponse res = new TaskSummaryResponse();

        res.setTotalTasks(tasks.size());

        long completed = tasks.stream()
                .filter(t -> t.getStatus() == Status.DONE)
                .count();
        res.setCompletedTasks(completed);

        long pending = tasks.stream()
                .filter(t -> t.getStatus() != Status.DONE)
                .count();
        res.setPendingTasks(pending);

        long overdue = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isBefore(today))
                .filter(t -> t.getStatus() != Status.DONE)
                .count();
        res.setOverdueTasks(overdue);

        Map<String, Long> countByPriority = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().name(),
                        Collectors.counting()
                ));
        res.setCountByPriority(countByPriority);

        tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> !t.getDueDate().isBefore(today))
                .sorted(Comparator.comparing(Task::getDueDate))
                .findFirst()
                .ifPresent(t -> res.setNextDueTask(TaskMapper.toResponse(t)));

        return res;
    }

    // RECOMMENDED TASKS
    public List<TaskResponse> getRecommendedTasks() {

        List<Task> tasks = repo.findAll();

        return tasks.stream()
                .sorted(
                        Comparator.comparingInt((Task t) -> t.getPriority().ordinal())
                                .thenComparing(
                                        Comparator.comparing(
                                                Task::getDueDate,
                                                Comparator.nullsLast(Comparator.naturalOrder())
                                        )
                                )
                                .thenComparing((Task t) ->
                                        t.getDescription() == null ? 0 : t.getDescription().length() * -1
                                )
                )
                .map(TaskMapper::toResponse)
                .toList();
    }

    // PAGINATION TASKS
    public TaskPageResponse getPaginatedTasks(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage = repo.findAll(pageable);

        TaskPageResponse response = new TaskPageResponse();

        response.setTasks(
                taskPage.getContent()
                        .stream()
                        .map(TaskMapper::toResponse)
                        .toList()
        );

        response.setPageNumber(taskPage.getNumber());
        response.setPageSize(taskPage.getSize());
        response.setTotalElements(taskPage.getTotalElements());
        response.setTotalPages(taskPage.getTotalPages());
        response.setLast(taskPage.isLast());

        return response;
    }

    // CSV EXPORT
    public void exportTasksToCsv(HttpServletResponse response) {

        try {
            List<Task> tasks = repo.findAll();

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"tasks.csv\"");

            PrintWriter writer = response.getWriter();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);

            Field[] fields = Task.class.getDeclaredFields();

            // Header
            List<String> headers = new ArrayList<>();
            for (Field f : fields) {
                f.setAccessible(true);
                headers.add(f.getName());
            }
            printer.printRecord(headers);

            // Rows
            for (Task t : tasks) {
                List<Object> row = new ArrayList<>();
                for (Field f : fields) {
                    f.setAccessible(true);
                    row.add(f.get(t));
                }
                printer.printRecord(row);
            }

            printer.flush();
            printer.close();

        } catch (Exception ex) {
            throw new CsvExportException("Failed to export CSV data", ex);
        }
    }

}
