package com.hanghae.finalProject.rest.meeting.repository;

import com.hanghae.finalProject.rest.alarm.dto.MeetingAlarmListDto;
import com.hanghae.finalProject.rest.meeting.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingCustomRepository {
     
     
     Optional<Meeting> findByIdAndDeletedIsFalse(Long meetingId);
    List<Meeting> findAllByStartDateAndStartTime(LocalDate today, LocalTime nowAfter30);

    List<Meeting> findAllByStartDateAndStartTimeAndDeletedIsFalse(LocalDate today, LocalTime nowAfter30);
}