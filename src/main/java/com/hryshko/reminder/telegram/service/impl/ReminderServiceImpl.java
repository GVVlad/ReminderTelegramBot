package com.hryshko.reminder.telegram.service.impl;

import com.hryshko.reminder.telegram.entity.Reminder;
import com.hryshko.reminder.telegram.repository.ReminderRepository;
import com.hryshko.reminder.telegram.service.api.ReminderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository repository;

    public ReminderServiceImpl(ReminderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reminder createReminder(Reminder reminder) {
        if (reminder != null) {
            return repository.save(reminder);
        } else {
            throw new NullPointerException("Cannot be null");
        }
    }

    @Override
    public List<Reminder> listReminders() {
        List<Reminder> reminders = repository.findAll();
        return reminders.isEmpty() ? new ArrayList<>() : reminders;
    }

    @Override
    public List<Reminder> listRemindersByUsersId(long userId) {
        List<Reminder> list = repository.findAll().stream().filter(r -> r.getUser().getChatId() == userId).collect(
            Collectors.toList());
        return list.isEmpty() ? new ArrayList<>() : list;
    }

    @Override
    public void update(Reminder reminder) {
        repository.save(reminder);
    }

    @Override
    public Reminder findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NullPointerException("Cannot find Reminder with given ID"));

    }

    @Override
    public Reminder findByTextName(String text) {
        List<Reminder> reminders = repository.findAll();
        Optional<Reminder> optional = reminders.stream()
            .filter(r -> r.getTextOfReminder().equals(text))
            .findFirst();

        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new NullPointerException("Cannot find Reminder with given reminder's text");
        }
    }

    @Override
    public void removeReminder(Long id) {
        repository.delete(findById(id));
    }
}
