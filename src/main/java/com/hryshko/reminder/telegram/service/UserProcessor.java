package com.hryshko.reminder.telegram.service;

import com.hryshko.reminder.telegram.bot.TelegramBot;
import com.hryshko.reminder.telegram.constants.ButtonConstants;
import com.hryshko.reminder.telegram.constants.MessageConstants;
import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Service
public class UserProcessor {
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final CommandProcessor commandProcessor;

    public UserProcessor(@Lazy TelegramBot telegramBot,
                         @Lazy CommandProcessor commandProcessor,
                         UserService userService) {
        this.userService = userService;
        this.telegramBot = telegramBot;
        this.commandProcessor = commandProcessor;
    }

    public void registrationCommand(Long chatId, Update update) {
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
                setUsersName(user, chatId, update);
            } else if(user.getPosition().equals(Position.INPUT_PHONE_NUMBER.toString())) {
                setUsersPhoneNumber(user, chatId, update);
            }
        }
    }

    private void setUsersName(User user, Long chatId, Update update) {
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
    }

    private void setUsersPhoneNumber(User user, Long chatId, Update update) {
        if(update.getMessage().hasContact()) {
            user.setPhoneNumber(update.getMessage().getContact().getPhoneNumber());
            user.setPosition(Position.REGISTERED.toString());
            userService.update(user);

            SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(MessageConstants.REG_FINISHED)
                .replyMarkup(commandProcessor.getMenuKeyBoard())
                .build();

            telegramBot.sendMessage(sendMessage);
        } else {
            telegramBot.sendMessage(new SendMessage(chatId.toString(), MessageConstants.REG_PHONE_ERROR));
            telegramBot.sendMessage(
                new SendMessage(chatId.toString(), MessageConstants.REG_ENTER_PHONE_NUMBER));
        }
    }


    public void userInformationCommand(Long chatId, Update update) {
    }
}
