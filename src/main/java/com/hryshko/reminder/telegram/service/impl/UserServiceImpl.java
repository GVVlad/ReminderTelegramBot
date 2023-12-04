package com.hryshko.reminder.telegram.service.impl;

import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.repository.UserRepository;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }


    @Override
    public User findUserByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }

    @Override
    public void update(User user) {
        userRepository.save(user);
    }

    @Override
    public void removeUser(Long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = userRepository.findById(chatId).get();
            userRepository.delete(user);
        }
    }
}
