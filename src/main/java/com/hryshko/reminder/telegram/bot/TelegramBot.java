package com.hryshko.reminder.telegram.bot;

import com.hryshko.reminder.telegram.constants.ButtonConstants;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.enums.Status;
import com.hryshko.reminder.telegram.service.CommandProcessor;
import com.hryshko.reminder.telegram.service.api.ReminderService;
import com.hryshko.reminder.telegram.service.api.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final CommandProcessor commandProcessor;
    private final UserService userService;
    private final ReminderService reminderService;
    @Value("${bot.name}")
    private String botUsername;

    public TelegramBot(@Value("${bot.token}") String botToken, CommandProcessor commandProcessor,
                       UserService userService, ReminderService reminderService) {
        super(botToken);
        this.commandProcessor = commandProcessor;
        this.userService = userService;
        this.reminderService = reminderService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command;
        Long chatId;
        if(update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if(update.getMessage().hasText()) {
                command = update.getMessage().getText();
            } else {
                command = "/reg";
            }
        } else {
            chatId = update.getCallbackQuery().getFrom().getId();
            command = update.getCallbackQuery().getData();
        }

        if(command.equals(ButtonConstants.START)) {
            commandProcessor.processCommand(ButtonConstants.START, chatId, update);
        } else if(command.equals(ButtonConstants.REGISTRATION) && userService.findUserByChatId(chatId) == null) {
            commandProcessor.processCommand(ButtonConstants.REGISTRATION, chatId, update);
        } else if(userService.findUserByChatId(chatId) != null &&
            (!userService.findUserByChatId(chatId).getPosition().equals(
                Position.REGISTERED.toString()) || update.getMessage().hasContact())) {
            commandProcessor.processCommand(ButtonConstants.REGISTRATION, chatId, update);
        } else if(reminderService.findByUserAndStatus(chatId, Status.IN_PROGRESS) != null) {
            commandProcessor.processCommand(ButtonConstants.ADD_REMINDER, chatId, update);
        } else {
            commandProcessor.processCommand(command, chatId, update);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
