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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
    private final KeyboardProcessor keyboardProcessor;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void createNewRemindCommand(Long chatId, Update update) {
        Reminder reminder = reminderService.findByUserAndStatus(chatId, Status.IN_PROGRESS);
        if(reminder == null) {
            createNewRemind(chatId);
        } else {
            if(reminder.getStatus().equals(Status.IN_PROGRESS.toString()) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_TEXT.toString())) {
                setReminderText(reminder, Position.INPUT_REMINDER_DATA, chatId, update);
                telegramBot.sendMessage(
                    SendMessage.builder().chatId(chatId).text(MessageConstants.REMINDER_ENTER_DATA).build());
            } else if(reminder.getStatus().equals(Status.IN_PROGRESS.toString()) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_DATA.toString())) {
                setReminderData(reminder, Position.INPUT_REMINDER_TIME, chatId, update);
            } else if(reminder.getStatus().equals(Status.IN_PROGRESS.toString()) &&
                reminder.getPosition().equals(Position.INPUT_REMINDER_TIME.toString())) {
                setReminderTime(reminder, chatId, update);
            }
        }
    }

    public void updateReminderTextCommand(Long chatId, Update update) {

        Reminder reminder = reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_TEXT);

        if(reminder != null) {
            setReminderText(reminder, Position.REGISTERED, chatId, update);
            collectInformationAboutReminder(reminder, chatId, keyboardProcessor.getUpdateButtons(reminder.getId()));
        }
    }

    public void updateReminderDateCommand(Long chatId, Update update) {
        Reminder reminder = reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_DATA);

        if(reminder != null) {
            setReminderData(reminder, Position.UPDATE_REMINDER_TIME, chatId, update);
        } else {
            reminder = reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_TIME);
            setReminderTime(reminder, chatId, update);
        }
    }

    public void updateRemindCommand(Long chatId, Update update) {
        long reminderId;
        Reminder reminder;

        if(update.hasCallbackQuery()) {
            if(update.getCallbackQuery().getData().contains(ButtonConstants.UPDATE_DATA) ||
                update.getCallbackQuery().getData().contains(ButtonConstants.UPDATE_TEXT)) {
                reminderId = Long.parseLong(update.getCallbackQuery().getData().substring(11));
            } else {
                reminderId = Long.parseLong(update.getCallbackQuery().getData().substring(7));
            }
            reminder = reminderService.findById(reminderId);
        } else {
            reminder = reminderService.findByStatus(Status.UPDATE);
        }

        if(reminder != null) {
            if(reminder.getStatus().equals(Status.CREATED.toString())) {

                reminder.setStatus(Status.UPDATE.toString());
                reminderService.update(reminder);

                telegramBot.sendMessage(DeleteMessage.builder().chatId(chatId)
                    .messageId(update.getCallbackQuery().getMessage().getMessageId()).build());

                collectInformationAboutReminder(reminder, chatId, keyboardProcessor.getUpdateButtons(reminder.getId()));

            } else if(update.hasCallbackQuery()) {
                if(update.getCallbackQuery().getData().contains(ButtonConstants.UPDATE_TEXT)) {
                    telegramBot.sendMessage(
                        SendMessage.builder()
                            .chatId(chatId)
                            .text(MessageConstants.REMINDER_ENTER_TEXT)
                            .build());

                    reminder.setPosition(Position.UPDATE_REMINDER_TEXT.toString());
                    reminderService.update(reminder);
                } else if(update.getCallbackQuery().getData().contains(ButtonConstants.UPDATE_DATA)) {
                    telegramBot.sendMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text(MessageConstants.REMINDER_ENTER_DATA)
                        .build());

                    reminder.setPosition(Position.UPDATE_REMINDER_DATA.toString());
                    reminderService.update(reminder);
                }
            }
        }
    }

    public void confirmReminderCommand(Long chatId, Update update) {
        if(!update.getCallbackQuery().getData().equals(ButtonConstants.CONFIRM)) {
            Long reminderId = Long.valueOf(update.getCallbackQuery().getData().replace(ButtonConstants.CONFIRM, ""));
            Reminder reminder = reminderService.findById(reminderId);
            reminder.setStatus(Status.CREATED.toString());
            reminderService.update(reminder);
        }
        telegramBot.sendMessage(
            DeleteMessage.builder().chatId(chatId).messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build());

        telegramBot.sendMessage(SendMessage.builder()
            .chatId(chatId)
            .text("Нагадування успішно створено!")
            .build());
    }

    @Scheduled(fixedDelay = 60000)
    public void remind() {
        List<Reminder> reminders = reminderService.getActualRemind();
        reminders.forEach(reminder -> {
                telegramBot.sendMessage(SendMessage.builder()
                    .text(reminder.getTextOfReminder())
                    .chatId(reminder.getUser().getChatId())
                    .replyMarkup(keyboardProcessor.getReScheduledButtons(reminder.getId()))
                    .build());
            }
        );
    }

    public void getAllRemindsCommand(Long chatId, Update update) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<Reminder> reminders = reminderService.listRemindersByUsersId(chatId);

        if(!reminders.isEmpty()) {
            reminders.forEach(reminder -> {
                keyboard.add(Collections.singletonList(InlineKeyboardButton.builder()
                    .text(reminder.getTextOfReminder())
                    .callbackData(ButtonConstants.REMINDER + reminder.getId())
                    .build()));
            });
            keyboardMarkup.setKeyboard(keyboard);

            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageConstants.GET_ALL_REMINDERS)
                .replyMarkup(keyboardMarkup)
                .build());
        } else {
            telegramBot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageConstants.EMPTY_LIST_OF_REMINDERS)
                .build());
        }
    }

    public void turnBackToAllReminds(Long chatId, Update update) {
        telegramBot.sendMessage(DeleteMessage.builder()
            .chatId(chatId).messageId(update.getCallbackQuery().getMessage().getMessageId())
            .build());

        getAllRemindsCommand(chatId, update);
    }

    public void updateRemindTime(Long chatId, Update update) {
        String reminderId = update.getCallbackQuery().getData().replace(ButtonConstants.RESCHEDULED, "");
        Reminder reminder = reminderService.findById(Long.valueOf(reminderId));
        reminder.setReminderTime(
            Time.valueOf(LocalTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm")) + ":00"));
        reminderService.update(reminder);

        telegramBot.sendMessage(DeleteMessage.builder()
            .chatId(chatId)
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .build());

        telegramBot.sendMessage(SendMessage.builder()
            .chatId(chatId)
            .text("Нагадування було успішно перенесено на 5 хвилин")
            .build());
    }

    public void finishedRemind(Long chatId, Update update) {
        telegramBot.sendMessage(DeleteMessage.builder()
            .chatId(chatId)
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .build());

        String reminderId = update.getCallbackQuery().getData().replace(ButtonConstants.RESCHEDULED, "");
        reminderService.removeReminder(Long.valueOf(reminderId));
    }

    public void showRemind(Long chatId, Update update) {
        String remindId = update.getCallbackQuery().getData().replace(ButtonConstants.REMINDER, "");
        Reminder reminder = reminderService.findById(Long.valueOf(remindId));

        String time = reminder.getReminderTime().toLocalTime().format(timeFormatter);
        String date = reminder.getReminderDate().toString();

        telegramBot.sendMessage(EditMessageText.builder()
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .chatId(chatId)
            .text(String.format(MessageConstants.REMINDER_INFO, reminder.getTextOfReminder(), date, time))
            .parseMode("HTML")
            .replyMarkup(keyboardProcessor.getUpdateDeleteTurnBackButtons(reminder.getId()))
            .build());
    }

    private void collectInformationAboutReminder(Reminder reminder, Long chatId, InlineKeyboardMarkup keyboard) {
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
            reminder.setPosition(position.toString());
            reminderService.update(reminder);


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
                reminder.setPosition(position.toString());

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
            if(!LocalDate.now().isAfter(reminder.getReminderDate().toLocalDate())) {
                LocalDateTime localDateTime = LocalDateTime.of(reminder.getReminderDate().toLocalDate(), time);

                if(localDateTime.isAfter(LocalDateTime.now())) {
                    reminder.setReminderTime(Time.valueOf(time));
                    reminder.setPosition(Position.REGISTERED.toString());
                    reminder.setStatus(Status.CREATED.toString());
                    reminderService.update(reminder);

                    telegramBot.sendMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text(MessageConstants.REMINDER_PRE_INFO)
                        .build());

                    collectInformationAboutReminder(reminder, chatId,
                        keyboardProcessor.getUpdateDeleteConfirmButtons(reminder.getId()));
                } else {
                    telegramBot.sendMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text("Вибач але час не може бути у минулому")
                        .build());
                }
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

    private void createNewRemind(Long chatId) {
        reminderService.createReminder(Reminder.builder()
            .user(userService.findUserByChatId(chatId))
            .status(Status.IN_PROGRESS.toString())
            .position(Position.INPUT_REMINDER_TEXT.toString())
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

}
