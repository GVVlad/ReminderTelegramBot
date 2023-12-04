package com.hryshko.reminder.telegram.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Position {
    INPUT_USERNAME("INPUT_USERNAME"),
    INPUT_PHONE_NUMBER("INPUT_PHONE_NUMBER"),
    REGISTERED("REGISTERED"),
    INPUT_REMINDER_TEXT("INPUT_REMINDER_TEXT"),
    INPUT_REMINDER_DATA("INPUT_REMINDER_DATA"),
    INPUT_REMINDER_TIME("INPUT_REMINDER_TIME"),

    UPDATE_REMINDER_TEXT("UPDATE_REMINDER_TEXT"),
    UPDATE_REMINDER_TIME("UPDATE_REMINDER_TIME"),
    UPDATE_REMINDER_DATA("UPDATE_REMINDER_DATA");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
