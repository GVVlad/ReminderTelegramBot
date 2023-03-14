package com.hryshko.reminder.telegram.bot.service.impl;

import com.hryshko.reminder.telegram.bot.model.User;
import com.hryshko.reminder.telegram.bot.repository.UserRepository;
import com.hryshko.reminder.telegram.bot.service.UserService;
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
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User readUserByChatId(Long chatId) {
        return userRepository.readUserByChatId(chatId);
    }

    @Override
    public void update(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public User findById(Long id) {
        if (userRepository.findById(id).isPresent()) {
            return userRepository.findById(id).get();
        } else {
            return null;
        }
    }

    @Override
    public User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findUserByPhoneNumber(phoneNumber);
    }

    @Override
    public void removeUser(Long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = userRepository.findById(chatId).get();
            userRepository.delete(user);
        }
    }
}
