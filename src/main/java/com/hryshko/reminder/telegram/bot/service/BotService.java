package com.hryshko.reminder.telegram.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Service
public interface BotService {
    void sendMessageWithReplyKeyBoard(long chatId, String textToSend, ReplyKeyboard markup,
                                      TelegramLongPollingBot pollingBot);


    void sendMessage(long chatId, String textToSend, TelegramLongPollingBot pollingBot);

    void sendEditedMessage(long chatId,long messageId, String textToSend, TelegramLongPollingBot pollingBot);
    void deleteSentMessage(long chatId,long messageId, TelegramLongPollingBot pollingBot);

    void showInformationAboutUser(Message message, TelegramLongPollingBot pollingBot);
    void deleteInfo(Message message,TelegramLongPollingBot pollingBot);
}
