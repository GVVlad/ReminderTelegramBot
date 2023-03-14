package com.hryshko.reminder.telegram.bot.controller;

import com.hryshko.reminder.telegram.bot.config.BotConfig;
import com.hryshko.reminder.telegram.bot.enums.Position;
import com.hryshko.reminder.telegram.bot.model.User;
import com.hryshko.reminder.telegram.bot.repository.UserRepository;
import com.hryshko.reminder.telegram.bot.service.BotService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private static final String HELP_TEXT = " Привіт!\n\nДаний бот буде корисним для будь-якої людини, " +
        "яка користується Telegram і хоче ефективно використовувати свій час. " +
        "\n\nВін стане незамінним помічником для тих, хто займається бізнесом, планує важливі зустрічі та дедлайни, " +
        "студентів, які мають багато різних завдань та проєктів, а також для всіх, " +
        "хто просто хоче покращити свою продуктивність та організованість прямо в Telegram. \n\n" +
        "Ви можете виконати команди з головного меню зліва або набравши команду:\n\n" +
        "Натисни /start аби розпочати роботу бота\n\n" +
        "Натисни /help щоб отримати дане повідомлення знову";
    final BotConfig config;
    private final UserRepository userRepository;
    private final UserController userController;

    private final BotService botService;

    public TelegramBot(BotConfig config, UserRepository userRepository,
                       UserController userController, BotService botService) {
        this.config = config;
        this.userRepository = userRepository;
        this.userController = userController;

        this.botService = botService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            userController.handleUpdate(update, this);

            if (update.getMessage().hasText()) {
                menu(update, update.getMessage().getChatId());
            }
        }
    }

    private void menu(Update update, Long chatId) {
        switch (update.getMessage().getText()) {
            case "/start":
                botService.sendMessage(chatId, "Привіт\nДля того щоб скористатися послугами цього боту,\n" +
                    "необхідно пройти процес реєстрації\n" +
                    "для цього натисни /reg", this);
                break;
            case "/reg":
                userRepository.save(createNewUser(update.getMessage()));
                botService.sendMessage(chatId, "Введи своє ім'я", this);
                break;

            case "/help":
                botService.sendMessage(chatId, HELP_TEXT, this);
                break;
            case "/mydate":
                botService.showInformationAboutUser(update.getMessage(), this);
                break;
            case "/delete":
                botService.deleteInfo(update.getMessage(), this);
                break;
        }

    }

    private User createNewUser(Message message) {
        Long chatId = message.getFrom().getId();
        var chat = message.getFrom();

        return User.builder()
            .chatId(chatId)
            .userName(chat.getUserName())
            .firstName(chat.getFirstName())
            .lastName(chat.getLastName())
            .registeredAt(LocalDateTime.now())
            .position(Position.INPUT_USERNAME)
            .build();
    }


}
