package com.hryshko.reminder.telegram.bot.service;

import com.hryshko.reminder.telegram.bot.model.Reminder;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ReminderService {

    Reminder createReminder(Reminder reminder);

    List<Reminder> listReminders();
    List<Reminder> listRemindersByUsersId(long userId);


    void update(Reminder reminder);

    Reminder findById(Long id);

    Reminder findByTextName(String text);

    void removeReminder(Long id);
}
