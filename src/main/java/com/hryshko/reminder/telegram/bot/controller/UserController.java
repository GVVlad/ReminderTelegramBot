package com.hryshko.reminder.telegram.bot.controller;

import com.hryshko.reminder.telegram.bot.enums.Position;
import com.hryshko.reminder.telegram.bot.model.User;
import com.hryshko.reminder.telegram.bot.repository.UserRepository;
import com.hryshko.reminder.telegram.bot.service.BotService;
import com.hryshko.reminder.telegram.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
@Slf4j
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final BotService botService;

    public UserController(UserRepository userRepository, UserService userService,
                          BotService botService) {
        this.userRepository = userRepository;
        this.userService = userService;

        this.botService = botService;
    }

    public void handleUpdate(Update update, TelegramLongPollingBot pollingBot) {

        Message message = update.getMessage();
        if (userRepository.findById(message.getChatId()).isPresent()) {
            User user = userService.findById(message.getChatId());
            switch (user.getPosition()) {
                case INPUT_USERNAME:
                    user.setFirstName(message.getText());
                    user.setPosition(Position.INPUT_PHONE_NUMBER);
                    ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true)
                        .keyboardRow(new KeyboardRow() {{
                            add(KeyboardButton.builder()
                                .text("Відправити свій номер телефону")
                                .requestContact(true)
                                .build());
                        }})
                        .build();
                    botService.sendMessageWithReplyKeyBoard(message.getChatId(), "Надішли нам свій номер телефону",
                        markup,
                        pollingBot);
                    userService.update(user);
                    break;

                case INPUT_PHONE_NUMBER:
                    if (message.hasContact()) {
                        user.setPhoneNumber(message.getContact().getPhoneNumber());
                        user.setPosition(Position.REGISTERED);
                        userRepository.save(user);
                    }
                    botService.sendMessage(message.getChatId(), user.getFirstName() + ", реєстрацію завершенно!!!",
                        pollingBot);
                    userService.update(user);
                    break;

            }
        }
    }


}
