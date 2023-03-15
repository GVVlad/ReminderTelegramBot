package com.hryshko.reminder.telegram.bot.controller;

import com.hryshko.reminder.telegram.bot.enums.Position;
import com.hryshko.reminder.telegram.bot.enums.Status;
import com.hryshko.reminder.telegram.bot.model.Reminder;
import com.hryshko.reminder.telegram.bot.service.BotService;
import com.hryshko.reminder.telegram.bot.service.ReminderService;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@Service
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;
    private final BotService botService;


    public ReminderController(ReminderService reminderService,
                              BotService botService) {
        this.reminderService = reminderService;
        this.botService = botService;
    }

    public void createNewReminder(Reminder reminder, Message message, TelegramLongPollingBot pollingBot) {
        if (reminder.getUser().getChatId() == message.getChatId()) {

            switch (reminder.getPosition()) {
                case INPUT_REMINDER_TEXT:

                    reminder.setTextOfReminder(message.getText());
                    reminder.setPosition(Position.INPUT_REMINDER_DATA);

                    if (message.getText().length() < 255) {
                        if (reminder.getTextOfReminder().equals("/addreminder")) {
                            reminderService.removeReminder(reminder.getId());
                            botService.sendMessage(message.getChatId(),
                                "Сталася помилка через те що було двічі натиснуто на /addreminder",
                                pollingBot);
                        } else {

                            botService.sendMessage(message.getChatId(),
                                "Надішли дату коли необдіно сповістити тебе про цю дату у форматі \"yyyy-mm-dd\"",
                                pollingBot);
                            reminderService.update(reminder);
                        }
                    } else {
                        botService.sendMessage(message.getChatId(), "Текс для нагадування занадто довге", pollingBot);
                        botService.sendMessage(message.getChatId(), "Максимальна довжина 255 символів", pollingBot);

                    }
                    break;

                case INPUT_REMINDER_DATA:
                    setReminderData(message, reminder, Position.INPUT_REMINDER_TIME, pollingBot);
                    break;

                case INPUT_REMINDER_TIME:
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();

                    row.add(InlineKeyboardButton.builder()
                        .text("Редагувати")
                        .callbackData("update")
                        .build());
                    row.add(InlineKeyboardButton.builder()
                        .text("Видалити")
                        .callbackData("delete")
                        .build());

                    keyboard.add(row);
                    keyboard.add(Collections.singletonList(
                        InlineKeyboardButton.builder()
                            .text("Підтвердити та зберегти")
                            .callbackData("confirm")
                            .build()));

                    setReminderTime(message, reminder, keyboard, pollingBot);
                    break;
            }

        }
    }


    public void showInformationAboutReminder(Reminder reminder, Message message,
                                             List<List<InlineKeyboardButton>> keyboard,
                                             TelegramLongPollingBot pollingBot) {
        Time time = reminder.getReminderTime();
        Date date = reminder.getReminderDate();
        LocalDateTime data = LocalDateTime.of(date.toLocalDate(), time.toLocalTime());

        String answer = "Інформація про нагадування:\n\n" +
            "<b>Текст нагадування:</b>\n" + reminder.getTextOfReminder() + "\n\n" +
            "<b>Час нагадування:</b> " + data.format(getFormatter("yyyy-MM-dd о HH:mm"));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        botService.sendMessageWithReplyKeyBoard(message.getChatId(), answer, inlineKeyboardMarkup, pollingBot);
    }

    public void startReminding(TelegramLongPollingBot pollingBot) {
        List<Reminder> reminders = reminderService.listReminders();
        DateTimeFormatter formatter = getFormatter("yyyy-MM-dd о HH:mm");
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();


        row.add(InlineKeyboardButton.builder()
            .text("Підтвердити")
            .callbackData("finished")
            .build());
        row.add(InlineKeyboardButton.builder()
            .text("Відкласти на 5 хвилин")
            .callbackData("moveToFive")
            .build());
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        for (Reminder reminder : reminders) {
            if (reminder.getReminderTime() != null) {
                LocalTime time = reminder.getReminderTime().toLocalTime();
                LocalDateTime remindTime = LocalDateTime.of(reminder.getReminderDate().toLocalDate(), time);

                if (LocalDateTime.now().format(formatter).equals(remindTime.format(formatter))) {
                    String answer = reminder.getTextOfReminder();
                    botService.sendMessageWithReplyKeyBoard(reminder.getUser().getChatId(), answer,
                        inlineKeyboardMarkup, pollingBot);
                }
            }
        }

    }

    public void setReminderData(Message message, Reminder reminder, Position position,
                                TelegramLongPollingBot pollingBot) {
        DateTimeFormatter formatter = getFormatter("yyyy-MM-dd");

        try {
            LocalDate.parse(message.getText(), formatter);
            if (LocalDate.now().isBefore(LocalDate.parse(message.getText(), formatter)) ||
                LocalDate.now().isEqual(LocalDate.parse(message.getText(), formatter))) {
                reminder.setPosition(position);

                reminder.setReminderDate(Date.valueOf(LocalDate.parse(message.getText(), formatter)));
                botService.sendMessage(message.getChatId(), "О котрій годині зробити нагадування", pollingBot);
                reminderService.update(reminder);
            } else {
                botService.sendMessage(message.getChatId(), "Вибач але дата не може бути у минулому",
                    pollingBot);

            }

        } catch (RuntimeException e) {
            botService.sendMessage(message.getChatId(), "Неправильна дата", pollingBot);
        }
    }


    public void setReminderTime(Message message, Reminder reminder, List<List<InlineKeyboardButton>> keyboard,
                                TelegramLongPollingBot pollingBot) {
        try {
            DateTimeFormatter formatter = getFormatter("yyyy-MM-ddHH:mm");
            LocalTime time = LocalTime.parse(message.getText());

            LocalDateTime dateTime = LocalDateTime.of(reminder.getReminderDate().toLocalDate(), time);

            if (LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter).isBefore(dateTime)) {

                reminder.setReminderTime(Time.valueOf(LocalTime.parse(message.getText(), getFormatter("HH:mm"))));
                reminder.setPosition(Position.REGISTERED);
                reminder.setStatus(Status.CREATED);
                reminderService.update(reminder);

                showInformationAboutReminder(reminder, message, keyboard, pollingBot);
            } else {
                botService.sendMessage(message.getChatId(), "Вибач але час не може бути у минулому",
                    pollingBot);
            }

        } catch (RuntimeException e) {
            botService.sendMessage(message.getChatId(), "Неправильний формат часу", pollingBot);
        }
    }

    public void showAllRemindsByUserId(Message message, TelegramLongPollingBot pollingBot) {
        List<Reminder> reminders = reminderService.listRemindersByUsersId(message.getChatId());
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
            .text("Редагувати")
            .callbackData("update")
            .build());
        row.add(InlineKeyboardButton.builder()
            .text("Видалити")
            .callbackData("delete")
            .build());

        keyboard.add(row);

        keyboard.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("Приховати")
                .callbackData("skip")
                .build()));

        if (reminders.isEmpty()) {
            botService.sendMessage(message.getChatId(), "На даний момент у вас не створенно жодгого нагадування!",
                pollingBot);
            botService.sendMessage(message.getChatId(), "Щоб створити нагадування натисни /addreminder", pollingBot);
        }

        for (Reminder reminder : reminders) {
            showInformationAboutReminder(reminder, message, keyboard, pollingBot);
        }
    }

    public DateTimeFormatter getFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

}
