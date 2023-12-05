package com.hryshko.reminder.telegram.repository;

import com.hryshko.reminder.telegram.entity.Reminder;
import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Reminder findByUserAndPosition(User user, Position position);
    Reminder findByUserAndStatus(User user, Status status);
}
