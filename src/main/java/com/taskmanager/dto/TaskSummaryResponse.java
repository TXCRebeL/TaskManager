package com.taskmanager.dto;

import java.util.Map;

public class TaskSummaryResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;

    private Map<String, Long> countByPriority;

    private TaskResponse nextDueTask;

    // Getters & Setters
    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }

    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }

    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }

    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }

    public Map<String, Long> getCountByPriority() { return countByPriority; }
    public void setCountByPriority(Map<String, Long> countByPriority) { this.countByPriority = countByPriority; }

    public TaskResponse getNextDueTask() { return nextDueTask; }
    public void setNextDueTask(TaskResponse nextDueTask) { this.nextDueTask = nextDueTask; }
}
