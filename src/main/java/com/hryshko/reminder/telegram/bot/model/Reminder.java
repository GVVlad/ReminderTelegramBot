package com.hryshko.reminder.telegram.bot.model;


import com.hryshko.reminder.telegram.bot.enums.Position;
import com.hryshko.reminder.telegram.bot.enums.Status;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text")
    private String textOfReminder;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "position")
    private Position position;

    @Column(name = "status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
