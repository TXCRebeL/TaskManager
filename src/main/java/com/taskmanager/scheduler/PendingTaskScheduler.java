package com.taskmanager.scheduler;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.Status;
import com.taskmanager.repos.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
public class PendingTaskScheduler  {

    private final TaskRepository repo;
    private static final Logger log = LoggerFactory.getLogger(PendingTaskScheduler .class);


    public PendingTaskScheduler (TaskRepository repo) {
        this.repo = repo;
    }

    // RUN EVERY DAY AT 9 PM
    @Scheduled(cron = "0 0 21 * * *")
    public void logPendingTasks() {

        LocalDate today = LocalDate.now();

        List<Task> pendingTasks = repo.findAll().stream()
                .filter(t -> t.getStatus() != Status.DONE)
                .toList();

        log.info("----- Pending Task Report (" + today + ") -----");
        log.info("Total Pending Tasks: " + pendingTasks.size());

        pendingTasks.forEach(t ->
                log.info("TASK => ID: " + t.getId()
                        + ", Title: " + t.getTitle()
                        + ", Priority: " + t.getPriority()
                        + ", DueDate: " + t.getDueDate()
                        + ", Status: " + t.getStatus()
                )
        );

        log.info("----------------------------------------------");
    }

}
