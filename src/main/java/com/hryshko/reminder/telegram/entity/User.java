package com.hryshko.reminder.telegram.entity;


import com.hryshko.reminder.telegram.enums.Position;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"first_name", "user_name"})
@Entity(name = "users")
public class User {

    @Id
    private long chatId;

    @Column(name = "first_name")
    private String firstName;


    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_phone_number")
    private String phoneNumber;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Reminder> myReminds;


    @Column(name = "status")
    private String position;
}
