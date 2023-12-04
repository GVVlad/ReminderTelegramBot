package com.hryshko.reminder.telegram.service;

import com.hryshko.reminder.telegram.bot.TelegramBot;
import com.hryshko.reminder.telegram.constants.ButtonConstants;
import com.hryshko.reminder.telegram.constants.MessageConstants;
import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.time.LocalDateTime;
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

    public CommandProcessor(@Lazy TelegramBot telegramBot, UserService userService) {
        this.telegramBot = telegramBot;
        this.userService = userService;
        commandMap.put(ButtonConstants.START, this::startCommand);
        commandMap.put(ButtonConstants.REGISTRATION, this::registrationCommand);

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
        }else {
            sendMessage.setText(MessageConstants.START_MESSAGE_REG);
            sendMessage.setReplyMarkup(getMenuKeyBoard());
        }

        telegramBot.sendMessage(sendMessage);
    }

    private void registrationCommand(Long chatId, Update update) {
        if(userService.findUserByChatId(chatId) == null) {

            User user = User.builder()
                .chatId(chatId)
                .userName(update.getCallbackQuery().getMessage().getChat().getUserName())
                .firstName(update.getCallbackQuery().getMessage().getFrom().getFirstName())
                .registeredAt(LocalDateTime.now())
                .position(Position.INPUT_USERNAME.toString())
                .build();

            userService.addUser(user);

            telegramBot.sendMessage(new SendMessage(chatId.toString(), MessageConstants.REG_ENTER_NAME));
        } else {
            User user = userService.findUserByChatId(chatId);
            if(user.getPosition().equals(Position.INPUT_USERNAME.toString())) {
                user.setFirstName(update.getMessage().getText());
                user.setPosition(Position.INPUT_PHONE_NUMBER.toString());

                SendMessage sendMessage = new SendMessage();

                sendMessage.setChatId(chatId);
                sendMessage.setText(MessageConstants.REG_ENTER_PHONE_NUMBER);
                sendMessage.setReplyMarkup(ReplyKeyboardMarkup.builder()
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true)
                    .keyboardRow(new KeyboardRow() {{
                        add(KeyboardButton.builder()
                            .text(ButtonConstants.SEND_PHONE)
                            .requestContact(true)
                            .build());
                    }})
                    .build());
                userService.update(user);
                telegramBot.sendMessage(sendMessage);
            } else if(user.getPosition().equals(Position.INPUT_PHONE_NUMBER.toString())) {
                if(update.getMessage().hasContact()) {
                    user.setPhoneNumber(update.getMessage().getContact().getPhoneNumber());
                    user.setPosition(Position.REGISTERED.toString());
                    userService.update(user);

                    telegramBot.sendMessage(new SendMessage(chatId.toString(), MessageConstants.REG_FINISHED));
                }else {
                    telegramBot.sendMessage(new SendMessage(chatId.toString(), MessageConstants.REG_PHONE_ERROR));
                    telegramBot.sendMessage(new SendMessage(chatId.toString(), MessageConstants.REG_ENTER_PHONE_NUMBER));
                }
            }
        }
    }

    private ReplyKeyboardMarkup getMenuKeyBoard() {
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
