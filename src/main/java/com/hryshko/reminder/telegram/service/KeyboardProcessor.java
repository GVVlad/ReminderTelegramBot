package com.hryshko.reminder.telegram.service;

import com.hryshko.reminder.telegram.constants.ButtonConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
public class KeyboardProcessor {

    public InlineKeyboardMarkup getUpdateButtons(Long reminderId) {

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.UPDATE_TEXT_COMMAND)
            .callbackData(ButtonConstants.UPDATE_TEXT + reminderId)
            .build());

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.UPDATE_DATA_COMMAND)
            .callbackData(ButtonConstants.UPDATE_DATA + reminderId)
            .build());

        keyboard.add(row);

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
            .text(ButtonConstants.CONFIRM_COMMAND)
            .callbackData(ButtonConstants.CONFIRM + reminderId)
            .build()));

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getUpdateDeleteButtons(Long reminderId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.UPDATE_COMMAND)
            .callbackData(ButtonConstants.UPDATE + reminderId)
            .build());

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.DELETE_COMMAND)
            .callbackData(ButtonConstants.DELETE + reminderId)
            .build());

        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getUpdateDeleteTurnBackButtons(Long reminderId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = getUpdateDeleteButtons(reminderId).getKeyboard();

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
            .text(ButtonConstants.TURN_BACK_COMMAND)
            .callbackData(ButtonConstants.TURN_BACK)
            .build()));

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
    public InlineKeyboardMarkup getUpdateDeleteConfirmButtons(Long reminderId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = getUpdateDeleteButtons(reminderId).getKeyboard();

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
            .text(ButtonConstants.CONFIRM_COMMAND)
            .callbackData(ButtonConstants.CONFIRM)
            .build()));

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReScheduledButtons(Long reminderId) {

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.RESCHEDULED_COMMAND)
            .callbackData(ButtonConstants.RESCHEDULED + reminderId)
            .build());

        row.add(InlineKeyboardButton.builder()
            .text(ButtonConstants.FINISH_COMMAND)
            .callbackData(ButtonConstants.FINISH + reminderId)
            .build());

        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}
