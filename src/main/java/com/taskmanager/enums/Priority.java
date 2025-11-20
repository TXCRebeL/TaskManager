package com.taskmanager.enums;

public enum Priority {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    public final int rank;

    Priority(int rank) {
        this.rank = rank;
    }
}
