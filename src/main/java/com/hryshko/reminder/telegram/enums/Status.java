package com.hryshko.reminder.telegram.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    IN_PROGRESS("IN_PROGRESS"),
    UPDATE("UPDATE"),
    CREATED("CREATED");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
