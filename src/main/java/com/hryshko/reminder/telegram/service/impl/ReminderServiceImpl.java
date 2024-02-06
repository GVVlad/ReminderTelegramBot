package com.hryshko.reminder.telegram.service.impl;

import com.hryshko.reminder.telegram.entity.Reminder;
import com.hryshko.reminder.telegram.entity.User;
import com.hryshko.reminder.telegram.enums.Position;
import com.hryshko.reminder.telegram.enums.Status;
import com.hryshko.reminder.telegram.repository.ReminderRepository;
import com.hryshko.reminder.telegram.service.api.ReminderService;
import com.hryshko.reminder.telegram.service.api.UserService;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ReminderServiceImpl implements ReminderService {

    public final UserService userService;
    private final ReminderRepository repository;

    @Override
    public Reminder createReminder(Reminder reminder) {
        if(reminder != null) {
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

        if(optional.isPresent()) {
            return optional.get();
        } else {
            throw new NullPointerException("Cannot find Reminder with given reminder's text");
        }
    }

    @Override
    public List<Reminder> getActualRemind() {
        Date date = Date.valueOf(LocalDate.now());
        Time time = Time.valueOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))+":00");
       return repository.findAllByReminderDateAndReminderTime(date,time);
    }

    @Override
    public void removeReminder(Long id) {
        repository.delete(findById(id));
    }

    @Override
    public Reminder findByUserAndPosition(Long userId, Position position) {
        User user = userService.findUserByChatId(userId);

        return repository.findByUserAndPosition(user, position.toString());
    }

    @Override
    public Reminder findByUserAndStatus(Long userId, Status status) {
        User user = userService.findUserByChatId(userId);

        return repository.findByUserAndStatus(user, status.toString());
    }

    @Override
    public Reminder findByStatus(Status status) {
        return repository.findByStatus(status.toString());
    }

    @Override
    public Reminder findByStatusAndPosition(Status status, Position position) {
        return repository.findByStatusAndPosition(status.toString(), position.toString());
    }
}
