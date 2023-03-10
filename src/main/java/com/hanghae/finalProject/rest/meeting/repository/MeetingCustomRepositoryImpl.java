package com.hanghae.finalProject.rest.meeting.repository;

import com.hanghae.finalProject.rest.alarm.dto.MeetingAlarmListDto;
import com.hanghae.finalProject.rest.attendant.dto.AttendantResponseDto;
import com.hanghae.finalProject.rest.meeting.dto.MeetingDetailResponseDto;
import com.hanghae.finalProject.rest.meeting.dto.MeetingListResponseDto;
import com.hanghae.finalProject.rest.meeting.model.CategoryCode;
import com.hanghae.finalProject.rest.user.model.QUser;
import com.hanghae.finalProject.rest.user.model.User;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.jsonwebtoken.lang.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.hanghae.finalProject.rest.alarm.model.QAlarm.alarm;
import static com.hanghae.finalProject.rest.attendant.model.QAttendant.attendant;
import static com.hanghae.finalProject.rest.meeting.model.QMeeting.meeting;
import static com.hanghae.finalProject.rest.review.model.QReview.review1;
import static com.hanghae.finalProject.rest.user.model.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.jpa.JPAExpressions.select;

@Slf4j
@RequiredArgsConstructor
@Repository
public class MeetingCustomRepositoryImpl implements MeetingCustomRepository {
     
     private final JPAQueryFactory jpaQueryFactory;
     
     @Override
     public MeetingDetailResponseDto findByIdAndAttendAndAlarmAndLike(Long meetingId, User user) {
          Long loggedId = user != null ? user.getId() : null;
          return jpaQueryFactory
               .select(Projections.fields(
                    MeetingDetailResponseDto.class,
                    Expressions.asNumber(meetingId).as("id"),
//                    meeting.id.as("id"),
                    meeting.user.id.as("masterId"),
                    meeting.title,
                    meeting.category,
                    meeting.startDate,
                    meeting.startTime,
                    meeting.duration,
                    meeting.platform,
                    meeting.link,
                    meeting.content,
                    meeting.maxNum,
                    meeting.secret,
                    meeting.password,
                    meeting.image,
                    // ????????? ????????? ?????? ?????? ????????????
                    ExpressionUtils.as(
                         select(attendant.user.id.isNotNull())
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "attend"),
                    // ?????? ????????????
                    ExpressionUtils.as(
                         select(
                              attendant.entrance)
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "entrance"),
                    // ?????? ?????? ??????
                    ExpressionUtils.as(
                         select(
                              attendant.review)
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "review"),
                    // ????????? ????????? ???????????? ??????????????? ??????
                    ExpressionUtils.as(
                         select(alarm.user.id.isNotNull())
                              .from(alarm)
                              .where(alarm.meeting.id.eq(meetingId), eqReviewUser(loggedId)), "alarm"),
                    // ?????? ????????? ????????? ???
                    ExpressionUtils.as(
                         select(
                              review1.review.count())
                              .from(review1)
                              .where(review1.meeting.id.eq(meetingId), review1.review.eq(true)), "likeNum"),
                    // ?????? ????????? ????????? ???
                    ExpressionUtils.as(
                         select(
                              review1.review.count())
                              .from(review1)
                              .where(review1.meeting.id.eq(meetingId), review1.review.eq(false)), "hateNum")
                    )
               )
               .from(meeting)
               .where(meeting.id.eq(meetingId), meeting.deleted.isFalse())
               .fetchOne();
     }
     
     // ???????????????
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllBySearchAndCategory(String search, CategoryCode category, Long meetingIdx) {
          // 1) ????????? ???????????? ?????? ??????
          List<Long> ids = jpaQueryFactory
               .select(meeting.id)
               .from(meeting)
               .where(eqCategory(category), // ???????????? ?????????
                    meeting.startDate.goe(LocalDateTime.now().toLocalDate()),
//                    meeting.title.contains(search), // ????????? ?????????
                    match(search), // full text search
                    meeting.deleted.eq(false),
                    ltMeetingId(meetingIdx))// ??????????????????
               .orderBy(meeting.id.desc())
               .limit(5)
               .fetch();
          // 1-1) ????????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ?????? ?????? ??????
          if (CollectionUtils.isEmpty(ids)) {
               return new ArrayList<>();
          }
          // 2) ?????? id??? ?????? meeting ?????????
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(attendant).on(meeting.id.eq(attendant.meeting.id))
               .leftJoin(user).on(attendant.user.id.eq(user.id))
               .where(meeting.id.in(ids))
               .orderBy(meeting.id.desc())
               .transform(getList(category));
     }
     
     
     
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllSortByPopularAndCategory(CategoryCode category, Long pageNum) {
          // 1) ????????? ???????????? ?????? ??????
          List<Long> ids = jpaQueryFactory
               .select(meeting.id) // ?????????????????? ??????id
               .from(meeting)
               .where(eqCategory(category),
                    meeting.startDate.goe(LocalDateTime.now().toLocalDate()),
                    meeting.deleted.eq(false)
               )
               .orderBy(meeting.attendantsNum.desc(), meeting.id.desc())
               .offset((pageNum == null) ? 0 : pageNum * 5)
               .limit(5)
               .fetch();
          
          // 1-1) ????????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ?????? ?????? ??????
          if (CollectionUtils.isEmpty(ids)) {
               return new ArrayList<>();
          }
          // 2) ?????? id??? ?????? meeting ????????? + ???????????? +
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(attendant).on(meeting.id.eq(attendant.meeting.id))
               .leftJoin(user).on(attendant.user.id.eq(user.id))
               .where(meeting.id.in(ids))
               .transform(getList(category));
     }
     
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllSortByNewAndCategory(CategoryCode category, Long meetingIdx) {
          // 1) ????????? ???????????? ?????? ??????
          List<Long> ids = jpaQueryFactory
               .select(meeting.id)
               .from(meeting)
               .where(eqCategory(category),
                    meeting.startDate.goe(LocalDateTime.now().toLocalDate()),
                    ltMeetingId(meetingIdx),
                    eqCategory(category),
                    meeting.deleted.eq(false)
               )
               .orderBy(meeting.id.desc())
               .limit(5)
               .fetch();
     
          // 1-1) ????????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ?????? ?????? ??????
          if (CollectionUtils.isEmpty(ids)) {
               return new ArrayList<>();
          }
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(attendant).on(meeting.id.eq(attendant.meeting.id))
               .leftJoin(user).on(attendant.user.id.eq(user.id))
               .where(
                    meeting.id.in(ids)
               )
               .orderBy(meeting.id.desc())
               .transform(getList(category));
     }
     
     @Override
     public List<MeetingAlarmListDto> findMeetingAlarmListDto(LocalTime nowAfter30) {
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(alarm).on(meeting.id.eq(alarm.meeting.id))
               .leftJoin(user).on(alarm.user.id.eq(user.id))
               .where(meeting.startDate.eq(LocalDate.now()),
                    meeting.startTime.eq(nowAfter30))
               .transform(
                    groupBy(meeting.id).list(
                         Projections.fields(
                              MeetingAlarmListDto.class,
                              meeting.id.as("meetingId"),
                              meeting.startDate,
                              meeting.title,
                              meeting.user.id.as("meetingUserId"),
                              list(alarm.user.id).as("alarmUserIdList")
                              )
                         )
               );
     }
     
     private static ResultTransformer<List<MeetingListResponseDto.ResponseDto>> getList(CategoryCode category) {
          return groupBy(meeting.id).list(
               Projections.fields(
                    MeetingListResponseDto.ResponseDto.class,
                    meeting.id.as("id"),
                    meeting.user.id.as("masterId"),
                    meeting.title,
                    meeting.category,
                    meeting.startDate,
                    meeting.startTime,
                    meeting.duration,
                    meeting.platform,
                    ObjectUtils.isEmpty(category)? meeting.category : Expressions.asEnum(category).as("category"),
                    meeting.content,
                    meeting.maxNum,
                    meeting.secret,
                    meeting.password,
                    meeting.image,
                    meeting.attendantsNum,
                    list(
                         Projections.fields(
                              AttendantResponseDto.simpleResponseDto.class,
                              attendant.user.id.as("userId"),
                              user.profileUrl.as("userProfileImg")
                         )
                    ).as("attendantsList")
               ));
     }
     // full text search???
     private BooleanExpression match(String search) {
          if( search == null){
               return null;
          }
          return Expressions.numberTemplate(Double.class, "function('match',{0},{1})", meeting.title, search).gt(0);
     }
     
     // ??????????????????. ?????? idx ?????? ???????????? ???????????? (sortBy new ????????? ??????)
     private BooleanExpression ltMeetingId(Long meetingIdx) {
          if (meetingIdx == null) {
               return null; // BooleanExpression ????????? null??? ???????????? ??????????????? ???????????? ????????????
          }
          return meeting.id.lt(meetingIdx);
     }
     
     // category ?????????. ???????????? null
     private BooleanExpression eqCategory(CategoryCode category) {
          if (ObjectUtils.isEmpty(category)) {
               return null;
          }
          return meeting.category.eq(category);
     }
     
     // ????????? ??????????????? ?????? ?????????. ???????????? null
     private BooleanExpression eqAttendantUser(Long loggedId) {
          if (ObjectUtils.isEmpty(loggedId)) {
               return attendant.user.id.eq(0L);
          }
          return attendant.user.id.eq(loggedId);
     }
     // ?????? ??????????????? ?????? ?????????. ???????????? null
     private BooleanExpression eqReviewUser(Long loggedId) {
          if (ObjectUtils.isEmpty(loggedId)) {
               return alarm.user.id.eq(0L);
          }
          return alarm.user.id.eq(loggedId);
     }
     // ????????? : attendant ??????????????? ????????? ?????? meeting_id ????????? ??????????????? ???
     // >> ??????????????? : ?????? meetingIdx ??????, ????????? ????????? ????????????
     // ?????????????????? left join & group by ?????? ??????  & where meetingId = idx
     // https://jojoldu.tistory.com/529?category=637935
     // meetingIdx = null & sortby = popular >> .offset(pageNo * pageSize)
}
