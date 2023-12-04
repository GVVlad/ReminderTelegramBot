package com.hryshko.reminder.telegram.service.api;

import com.hryshko.reminder.telegram.entity.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void addUser(User user);

    User findUserByChatId(Long chatId);

    void update(User user);

    void removeUser(Long chatId);
}
