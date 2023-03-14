package com.hryshko.reminder.telegram.bot.repository;

import com.hryshko.reminder.telegram.bot.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}
