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
                System.out.println("FROM UPDATE: "+command);
            } else {
                command = ButtonConstants.REGISTRATION;
            }
        } else {
            chatId = update.getCallbackQuery().getFrom().getId();
            command = update.getCallbackQuery().getData();
            System.out.println("FROM CALLBACK: "+command);
        }

        if(command.equals(ButtonConstants.START)) {
            commandProcessor.processCommand(ButtonConstants.START, chatId, update);
        } else if(command.equals(ButtonConstants.REGISTRATION) && userService.findUserByChatId(chatId) == null) {
            commandProcessor.processCommand(ButtonConstants.REGISTRATION, chatId, update);
        } else if(userService.findUserByChatId(chatId) != null &&
            (!userService.findUserByChatId(chatId).getPosition().equals(
                Position.REGISTERED.toString()) || (update.hasMessage() && update.getMessage().hasContact()))) {
            commandProcessor.processCommand(ButtonConstants.REGISTRATION, chatId, update);
        } else if(reminderService.findByUserAndStatus(chatId, Status.IN_PROGRESS) != null) {
            commandProcessor.processCommand(ButtonConstants.ADD_REMINDER, chatId, update);
        } else if(command.contains(ButtonConstants.UPDATE)) {
            commandProcessor.processCommand(ButtonConstants.UPDATE, chatId, update);
        } else if(reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_TEXT) != null &&
            !command.equals(ButtonConstants.CONFIRM)) {
            commandProcessor.processCommand(ButtonConstants.UPDATE_TEXT, chatId, update);
        } else if((reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_DATA) != null ||
            reminderService.findByStatusAndPosition(Status.UPDATE, Position.UPDATE_REMINDER_TIME) != null) &&
            !command.equals(ButtonConstants.CONFIRM)) {
            commandProcessor.processCommand(ButtonConstants.UPDATE_DATA, chatId, update);
        } else if(command.contains(ButtonConstants.RESCHEDULED)) {
            commandProcessor.processCommand(ButtonConstants.RESCHEDULED, chatId, update);
        } else if(command.contains(ButtonConstants.FINISH)) {
            commandProcessor.processCommand(ButtonConstants.FINISH, chatId, update);
        } else if(command.contains(ButtonConstants.REMINDER)){
            commandProcessor.processCommand(ButtonConstants.REMINDER, chatId, update);
        }else if(command.contains(ButtonConstants.CONFIRM)){
            commandProcessor.processCommand(ButtonConstants.CONFIRM, chatId, update);
        } else  {
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
