package com.hryshko.reminder.telegram.bot.service;

import com.hryshko.reminder.telegram.bot.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void addUser(User user);

    List<User> listUsers();

    User readUserByChatId(Long chatId);

    void update(User user);

    User findByUserName(String userName);

    User findById(Long id);


    User findUserByPhoneNumber(String phoneNumber);

    void removeUser(Long chatId);
}
