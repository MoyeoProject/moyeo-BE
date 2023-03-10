package com.hanghae.finalProject.rest.alarm.model;

import com.hanghae.finalProject.config.model.Timestamped;
import com.hanghae.finalProject.rest.meeting.model.Meeting;
import com.hanghae.finalProject.rest.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class AlarmList extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable = false)
    private User user;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name ="meeting_id")
    private Meeting meeting;

    @Column(nullable = false)
    private String content;

    @Column
    private String url;

    public AlarmList(Meeting meeting, User user, String content, String url) {
        this.meeting = meeting;
        this.user = user;
        this.content = content;
        this.url = url;
    }

}
