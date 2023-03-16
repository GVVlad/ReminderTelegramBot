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

    public void updateReminder(Reminder reminder, Message message, TelegramLongPollingBot pollingBot) {
        if (reminder.getUser().getChatId() == message.getChatId()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            row.add(InlineKeyboardButton.builder()
                .text("Змінити текст")
                .callbackData("updateText")
                .build());

            row.add(InlineKeyboardButton.builder()
                .text("Змінити дату")
                .callbackData("updateDataTime")
                .build());

            keyboard.add(row);

            keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
                .text("Підтвердити і зберігти")
                .callbackData("updated")
                .build()));

            switch (reminder.getPosition()) {
                case UPDATE_REMINDER_TEXT:
                    reminder.setTextOfReminder(message.getText());
                    reminder.setStatus(Status.CREATED);
                    reminder.setPosition(Position.REGISTERED);

                    reminderService.update(reminder);

                    showInformationAboutReminder(reminder, message, keyboard, pollingBot);
                    break;

                case UPDATE_REMINDER_DATA:
                    setReminderData(message, reminder, Position.UPDATE_REMINDER_TIME, pollingBot);
                    break;

                case UPDATE_REMINDER_TIME:
                    setReminderTime(message, reminder, keyboard, pollingBot);
                    break;

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

    public String getTitleFromBackQuery(CallbackQuery callbackQuery) {
        String delete = callbackQuery.getMessage().getText()
            .substring(callbackQuery.getMessage().getText().length() - 18);

        return callbackQuery.getMessage().getText()
            .replaceAll("Інформація про нагадування:\n\nТекст нагадування:\n", "")
            .replace(delete, "")
            .replaceAll("\n\nЧас нагадування: ", "");

    }


    public void callBackQuery(CallbackQuery callbackQuery, TelegramLongPollingBot pollingBot) {

        if (callbackQuery.getData().equals("delete")) {
            Reminder reminder = reminderService.findByTextName(getTitleFromBackQuery(callbackQuery));

            reminderService.removeReminder(reminder.getId());

            botService.sendEditedMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                "Нагадування було видалено успішно!",
                pollingBot);

        }

        if (callbackQuery.getData().equals("confirm")) {
            botService.sendEditedMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                "Нагадування було успішно створено!",
                pollingBot);
        }

        if (callbackQuery.getData().equals("skip")) {
            botService.deleteSentMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                pollingBot);
        }

        if (callbackQuery.getData().equals("update")) {
            Reminder reminder = reminderService.findByTextName(getTitleFromBackQuery(callbackQuery));
            List<InlineKeyboardButton> row = new ArrayList<>();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            botService.sendMessage(callbackQuery.getMessage().getChatId(),
                "Процес редагування нагадування розпочато",
                pollingBot);

            row.add(InlineKeyboardButton.builder()
                .text("Змінити текст")
                .callbackData("updateText")
                .build());

            row.add(InlineKeyboardButton.builder()
                .text("Змінити дату")
                .callbackData("updateDataTime")
                .build());

            keyboard.add(row);
            showInformationAboutReminder(reminder, callbackQuery.getMessage(), keyboard, pollingBot);
        }

        if (callbackQuery.getData().equals("updateText")) {
            Reminder reminder = reminderService.findByTextName(getTitleFromBackQuery(callbackQuery));
            reminder.setStatus(Status.UPDATE);
            reminder.setPosition(Position.UPDATE_REMINDER_TEXT);
            reminderService.update(reminder);

            botService.sendEditedMessage(reminder.getUser().getChatId(), callbackQuery.getMessage().getMessageId(),
                "Введи новий текст для нагадування", pollingBot);
        }

        if (callbackQuery.getData().equals("updateDataTime")) {
            Reminder reminder = reminderService.findByTextName(getTitleFromBackQuery(callbackQuery));
            reminder.setStatus(Status.UPDATE);
            reminder.setPosition(Position.UPDATE_REMINDER_DATA);
            reminderService.update(reminder);

            botService.sendEditedMessage(reminder.getUser().getChatId(), callbackQuery.getMessage().getMessageId(),
                "Введи нову дату для нагадування", pollingBot);
        }

        if (callbackQuery.getData().equals("updated")) {
            botService.sendEditedMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                "Нагадування було успішно оновлено!",
                pollingBot);
        }

        if (callbackQuery.getData().equals("moveToFive")) {
            Reminder reminder = reminderService.findByTextName(callbackQuery.getMessage().getText());
            LocalTime newTime = reminder.getReminderTime().toLocalTime().plusMinutes(5);
            reminder.setReminderTime(Time.valueOf(newTime));
            reminderService.update(reminder);

            botService.sendEditedMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(), "Нагадування було відкладено на 5 хвилин",
                pollingBot);
        }

        if (callbackQuery.getData().equals("finished")) {
            Reminder reminder = reminderService.findByTextName(callbackQuery.getMessage().getText());
            reminderService.removeReminder(reminder.getId());

            botService.sendEditedMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                "Нагадування було підтверджено та видалено із списку",
                pollingBot);
        }
    }

    public DateTimeFormatter getFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

}
