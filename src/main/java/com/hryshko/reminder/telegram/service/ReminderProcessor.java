package com.hryshko.reminder.telegram.service;

import com.hryshko.reminder.telegram.bot.TelegramBot;
import com.hryshko.reminder.telegram.constants.ButtonConstants;
import com.hryshko.reminder.telegram.constants.MessageConstants;
import com.hryshko.reminder.telegram.entity.Reminder;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.enums.Status;
import com.hryshko.reminder.telegram.service.api.ReminderService;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
@RequiredArgsConstructor
public class ReminderProcessor {
    @Lazy
    private final TelegramBot telegramBot;
    private final UserService userService;
    private final ReminderService reminderService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void createNewRemindCommand(Long chatId, Update update) {
        Reminder reminder = reminderService.findByUserAndStatus(chatId, Status.IN_PROGRESS);
        if(reminder == null) {
            createNewRemind(chatId);
        } else {
            if(reminder.getStatus().equals(Status.IN_PROGRESS) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_TEXT)) {
                setReminderText(reminder, Position.INPUT_REMINDER_DATA, chatId, update);
            } else if(reminder.getStatus().equals(Status.IN_PROGRESS) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_DATA)) {
                setReminderData(reminder, Position.INPUT_REMINDER_TIME, chatId, update);
            } else if(reminder.getStatus().equals(Status.IN_PROGRESS) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_TIME)) {
                setReminderTime(reminder, chatId, update);
            }
        }
    }

//    public void updateRemindCommand(Long chatId, Update update) {
//        Reminder reminder = reminderService.findById(Long.valueOf(update.getCallbackQuery().getData()));
//        if(reminder.getStatus().equals(Status.CREATED)) {
//            reminder.setStatus(Status.UPDATE);
//            reminderService.update(reminder);
//
//
//
//        } else if(reminder.getStatus().equals(Status.UPDATE) &&
//            reminder.getPosition().equals(Position.UPDATE_REMINDER_TEXT)) {
//            setReminderText(reminder, Position.UPDATE_REMINDER_DATA, chatId, update);
//        } else if(reminder.getStatus().equals(Status.UPDATE) &&
//            reminder.getPosition().equals(Position.UPDATE_REMINDER_DATA)) {
//            setReminderData(reminder, Position.UPDATE_REMINDER_TIME, chatId, update);
//        } else if(reminder.getStatus().equals(Status.UPDATE) &&
//            reminder.getPosition().equals(Position.UPDATE_REMINDER_TIME)) {
//            setReminderTime(reminder, chatId, update);
//        }
//    }

    private void createNewRemind(Long chatId) {
        reminderService.createReminder(Reminder.builder()
            .user(userService.findUserByChatId(chatId))
            .status(Status.IN_PROGRESS)
            .position(Position.INPUT_REMINDER_TEXT)
            .build());

        telegramBot.sendMessage(SendMessage.builder()
            .chatId(chatId)
            .text(MessageConstants.REMINDER_REGISTRATION)
            .build());

        telegramBot.sendMessage(SendMessage.builder()
            .chatId(chatId)
            .text(MessageConstants.REMINDER_ENTER_TEXT)
            .build());
    }


    public void showInformationAboutReminder(Reminder reminder, Long chatId, InlineKeyboardMarkup keyboard) {
        String time = reminder.getReminderTime().toLocalTime().format(timeFormatter);
        String date = reminder.getReminderDate().toString();
        String answer = String.format(MessageConstants.REMINDER_INFO, reminder.getTextOfReminder(), date, time);

        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(answer)
            .parseMode("HTML")
            .build();

        if(keyboard != null) {
            sendMessage.setReplyMarkup(keyboard);
        }
        telegramBot.sendMessage(sendMessage);
    }

    private void setReminderText(Reminder reminder, Position position, Long chatId, Update update) {
        if(update.getMessage().getText().length() <= 255) {
            reminder.setTextOfReminder(update.getMessage().getText());
            reminder.setPosition(position);
            reminderService.update(reminder);

            telegramBot.sendMessage(
                SendMessage.builder().chatId(chatId).text(MessageConstants.REMINDER_ENTER_DATA).build());

        } else {
            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Текс для нагадування занадто довгий")
                .build());

            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Максимальна довжина 255 символів")
                .build());
        }
    }

    private void setReminderData(Reminder reminder, Position position, Long chatId, Update update) {
        try {
            LocalDate.parse(update.getMessage().getText(), dateFormatter);
            if(LocalDate.now().isBefore(LocalDate.parse(update.getMessage().getText(), dateFormatter)) ||
                LocalDate.now().isEqual(LocalDate.parse(update.getMessage().getText(), dateFormatter))) {
                reminder.setPosition(position);

                reminder.setReminderDate(
                    Date.valueOf(LocalDate.parse(update.getMessage().getText(), dateFormatter)));
                telegramBot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageConstants.REMINDER_ENTER_TIME)
                    .build());

                reminderService.update(reminder);
            } else {
                telegramBot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("Вибач але дата не може бути у минулому")
                    .build());
            }
        } catch (RuntimeException e) {
            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Неправильна дата")
                .build());
        }
    }

    private void setReminderTime(Reminder reminder, Long chatId, Update update) {
        try {
            LocalTime time = LocalTime.parse(update.getMessage().getText(), timeFormatter);
            if(LocalDate.now().isBefore(reminder.getReminderDate().toLocalDate())) {

                reminder.setReminderTime(Time.valueOf(time));
                reminder.setPosition(Position.REGISTERED);
                reminder.setStatus(Status.CREATED);
                reminderService.update(reminder);

                telegramBot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageConstants.REMINDER_PRE_INFO)
                    .build());

                showInformationAboutReminder(reminder, chatId, getUpdateDeleteButtons(reminder.getId()));
            } else {
                telegramBot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("Вибач але час не може бути у минулому")
                    .build());
            }
        } catch (RuntimeException e) {
            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Неправильний формат часу")
                .build());
        }
    }

    private InlineKeyboardMarkup getUpdateButtons(Long reminderId){

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
            .callbackData(ButtonConstants.CONFIRM)
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

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
            .text(ButtonConstants.CONFIRM_COMMAND)
            .callbackData(ButtonConstants.CONFIRM)
            .build()));

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public void getAllRemindsCommand(Long chatId, Update update) {
    }
}
