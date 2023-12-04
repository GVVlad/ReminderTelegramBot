package com.hryshko.reminder.telegram.service.api;

import com.hryshko.reminder.telegram.entity.Reminder;
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
