package com.hryshko.reminder.telegram.bot.repository;

import com.hryshko.reminder.telegram.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User readUserByChatId(Long chatId);

    User findByUserName(String userName);

    User findUserByPhoneNumber(String phoneNumber);
}
