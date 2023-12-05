package com.hryshko.reminder.telegram.service.impl;

import com.hryshko.reminder.telegram.repository.UserRepository;
import com.hryshko.reminder.telegram.service.api.BotService;
import com.vdurmont.emoji.EmojiParser;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final UserRepository userRepository;

    @Override
    public void sendMessageWithReplyKeyBoard(long chatId, String textToSend, ReplyKeyboard markup,
                                             TelegramLongPollingBot pollingBot) {
        SendMessage message = SendMessage.builder()
            .chatId(String.valueOf(chatId))
            .text(textToSend)
            .replyMarkup(markup)
            .parseMode("HTML")
            .build();

        try {
            pollingBot.execute(message);
        } catch (TelegramApiException ignored) {

        }
    }


    @Override
    public void sendMessage(long chatId, String textToSend, TelegramLongPollingBot pollingBot) {
        SendMessage message = SendMessage.builder()
            .chatId(String.valueOf(chatId))
            .text(textToSend)
            .parseMode("HTML")
            .build();

        try {
            pollingBot.execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    @Override
    public void sendEditedMessage(long chatId, long messageId, String textToSend, TelegramLongPollingBot pollingBot) {

        EditMessageText message = EditMessageText.builder()
            .messageId((int) messageId)
            .chatId(String.valueOf(chatId))
            .text(textToSend)
            .build();

        try {
            pollingBot.execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    @Override
    public void deleteSentMessage(long chatId, long messageId, TelegramLongPollingBot pollingBot) {
        DeleteMessage message = DeleteMessage.builder()
            .messageId((int) messageId)
            .chatId(String.valueOf(chatId))
            .build();

        try {
            pollingBot.execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    @Override
    public void showInformationAboutUser(Message message, TelegramLongPollingBot pollingBot) {
        String answer;
        var chatId = message.getChatId();
        if (userRepository.findById(chatId).isPresent()) {
            var chat = message.getChat();

            var user = userRepository.findById(chatId).get();
            String data = user.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            answer = "Ось що ми знаємо про тебе:\n\nІм'я: " + user.getFirstName() + "\n" +
                "Username: @" + chat.getUserName() + "\n" +
                "Спількуєшся з ботом з: " + data + "\n" +
                "Твій номер телефону: " + user.getPhoneNumber();

        } else {
            answer = EmojiParser.parseToUnicode(":scream:Ого!\n\nМи тебе бачимо в перший раз ! :open_mouth:");
        }
        sendMessage(chatId, answer, pollingBot);
    }

    @Override
    public void deleteInfo(Message message, TelegramLongPollingBot pollingBot) {
        long chatId = message.getChatId();
        var user = userRepository.findById(chatId);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            sendMessage(chatId,
                EmojiParser.parseToUnicode(
                    "Ну добре\n\nЯ стираю із своєї пам'яті хто ти такий \uD83D\uDE36\u200D\uD83C\uDF2B️"), pollingBot);
        } else {
            sendMessage(chatId,
                EmojiParser.parseToUnicode(":scream:Ого!\n\nМи тебе бачимо в перший раз ! :open_mouth:"), pollingBot);
        }
    }
}
