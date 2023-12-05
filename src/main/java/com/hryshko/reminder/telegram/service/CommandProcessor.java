package com.hryshko.reminder.telegram.service;

import com.hryshko.reminder.telegram.bot.TelegramBot;
import com.hryshko.reminder.telegram.constants.ButtonConstants;
import com.hryshko.reminder.telegram.constants.MessageConstants;
import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Service
public class CommandProcessor {
    private final Map<String, BiConsumer<Long, Update>> commandMap = new HashMap<>();

    private final TelegramBot telegramBot;
    private final UserService userService;

    public CommandProcessor(@Lazy TelegramBot telegramBot,
                            @Lazy ReminderProcessor reminderProcessor,
                            @Lazy UserProcessor userProcessor,
                            UserService userService) {
        this.telegramBot = telegramBot;
        this.userService = userService;
        commandMap.put(ButtonConstants.START, this::startCommand);
        commandMap.put(ButtonConstants.REGISTRATION, userProcessor::registrationCommand);
        commandMap.put(ButtonConstants.ADD_REMINDER, reminderProcessor::createNewRemindCommand);
        commandMap.put(ButtonConstants.SHOW_REMINDERS, reminderProcessor::getAllRemindsCommand);
        commandMap.put(ButtonConstants.USER_INFO, userProcessor::userInformationCommand);
        commandMap.put(ButtonConstants.HELP, this::helpCommand);
    }

    public void processCommand(String command, Long chatId, Update update) {
        commandMap.get(command).accept(chatId, update);
    }

    private void startCommand(Long chatId, Update update) {
        User user = userService.findUserByChatId(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if(user == null) {
            sendMessage.setText(MessageConstants.START_MESSAGE);
            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            InlineKeyboardButton regButton = new InlineKeyboardButton();
            regButton.setText(ButtonConstants.REGISTRATION_COMMAND);
            regButton.setCallbackData(ButtonConstants.REGISTRATION);

            keyboard.add(List.of(regButton));
            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
        } else {
            sendMessage.setText(MessageConstants.START_MESSAGE_REG);
            sendMessage.setReplyMarkup(getMenuKeyBoard());
        }

        telegramBot.sendMessage(sendMessage);
    }

    private void helpCommand(Long chatId, Update update) {
    }

    public ReplyKeyboardMarkup getMenuKeyBoard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        replyKeyboardMarkup.setResizeKeyboard(true);

        keyboardRows.add(new KeyboardRow(
            List.of(
                new KeyboardButton(ButtonConstants.ADD_REMINDER),
                new KeyboardButton(ButtonConstants.SHOW_REMINDERS)
            )));

        keyboardRows.add(new KeyboardRow(
            List.of(
                new KeyboardButton(ButtonConstants.HELP),
                new KeyboardButton(ButtonConstants.USER_INFO)
            )));

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;

    }
}
