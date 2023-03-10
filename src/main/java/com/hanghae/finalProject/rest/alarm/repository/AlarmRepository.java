package com.hanghae.finalProject.rest.alarm.repository;

import com.hanghae.finalProject.rest.alarm.model.Alarm;
import com.hanghae.finalProject.rest.meeting.model.Meeting;
import com.hanghae.finalProject.rest.user.model.User;
import com.mysql.cj.MysqlConnection;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Optional<Alarm> findByMeetingAndUser(Meeting meeting, User user);

    List<Alarm> findAllByMeeting(Meeting meeting);
}