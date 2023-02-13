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
                    // 로그인 유저의 해당 모임 참석유무
                    ExpressionUtils.as(
                         select(attendant.user.id.isNotNull())
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "attend"),
                    // 모임 입장여부
                    ExpressionUtils.as(
                         select(
                              attendant.entrance)
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "entrance"),
                    // 모임 리뷰 여부
                    ExpressionUtils.as(
                         select(
                              attendant.review)
                              .from(attendant)
                              .where(attendant.meeting.id.eq(meetingId), eqAttendantUser(loggedId)), "review"),
                    // 로그인 유저의 해당모임 알림활성화 유무
                    ExpressionUtils.as(
                         select(alarm.user.id.isNotNull())
                              .from(alarm)
                              .where(alarm.meeting.id.eq(meetingId), eqReviewUser(loggedId)), "alarm"),
                    // 해당 모임의 좋아요 수
                    ExpressionUtils.as(
                         select(
                              review1.review.count())
                              .from(review1)
                              .where(review1.meeting.id.eq(meetingId), review1.review.eq(true)), "likeNum"),
                    // 해당 모임의 싫어요 수
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
     
     // 검색리스트
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllBySearchAndCategory(String search, CategoryCode category, Long meetingIdx) {
          // 1) 커버링 인덱스로 대상 조회
          List<Long> ids = jpaQueryFactory
               .select(meeting.id)
               .from(meeting)
               .where(eqCategory(category), // 카테고리 필터링
                    meeting.startDate.goe(LocalDateTime.now().toLocalDate()),
//                    meeting.title.contains(search), // 검색어 필터링
                    match(search), // full text search
                    meeting.deleted.eq(false),
                    ltMeetingId(meetingIdx))// 무한스크롤용
               .orderBy(meeting.id.desc())
               .limit(5)
               .fetch();
          // 1-1) 대상이 없을 경우 추가 쿼리 수행 할 필요 없이 바로 반환
          if (CollectionUtils.isEmpty(ids)) {
               return new ArrayList<>();
          }
          // 2) 해당 id를 가진 meeting 리스트
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
          // 1) 커버링 인덱스로 대상 조회
          List<Long> ids = jpaQueryFactory
               .select(meeting.id) // 참석자명단의 미팅id
               .from(meeting)
               .where(eqCategory(category),
                    meeting.startDate.goe(LocalDateTime.now().toLocalDate()),
                    meeting.deleted.eq(false)
               )
               .orderBy(meeting.attendantsNum.desc(), meeting.id.desc())
               .offset((pageNum == null) ? 0 : pageNum * 5)
               .limit(5)
               .fetch();
          
          // 1-1) 대상이 없을 경우 추가 쿼리 수행 할 필요 없이 바로 반환
          if (CollectionUtils.isEmpty(ids)) {
               return new ArrayList<>();
          }
          // 2) 해당 id를 가진 meeting 리스트 + 유저정보 +
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(attendant).on(meeting.id.eq(attendant.meeting.id))
               .leftJoin(user).on(attendant.user.id.eq(user.id))
               .where(meeting.id.in(ids))
               .transform(getList(category));
     }
     
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllSortByNewAndCategory(CategoryCode category, Long meetingIdx) {
          // 1) 커버링 인덱스로 대상 조회
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
     
          // 1-1) 대상이 없을 경우 추가 쿼리 수행 할 필요 없이 바로 반환
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
     // full text search용
     private BooleanExpression match(String search) {
          if( search == null){
               return null;
          }
          return Expressions.numberTemplate(Double.class, "function('match',{0},{1})", meeting.title, search).gt(0);
     }
     
     // 무한스크롤용. 해당 idx 보다 작은것들 불러오기 (sortBy new 일때만 이거)
     private BooleanExpression ltMeetingId(Long meetingIdx) {
          if (meetingIdx == null) {
               return null; // BooleanExpression 자리에 null이 반환되면 조건문에서 자동으로 제거된다
          }
          return meeting.id.lt(meetingIdx);
     }
     
     // category 조건문. 없을경우 null
     private BooleanExpression eqCategory(CategoryCode category) {
          if (ObjectUtils.isEmpty(category)) {
               return null;
          }
          return meeting.category.eq(category);
     }
     
     // 참석자 로그인유저 확인 조건문. 없을경우 null
     private BooleanExpression eqAttendantUser(Long loggedId) {
          if (ObjectUtils.isEmpty(loggedId)) {
               return attendant.user.id.eq(0L);
          }
          return attendant.user.id.eq(loggedId);
     }
     // 리뷰 로그인유저 확인 조건문. 없을경우 null
     private BooleanExpression eqReviewUser(Long loggedId) {
          if (ObjectUtils.isEmpty(loggedId)) {
               return alarm.user.id.eq(0L);
          }
          return alarm.user.id.eq(loggedId);
     }
     // 인기순 : attendant 테이블에서 참석자 많은 meeting_id 순으로 정렬해와야 함
     // >> 무한스크롤 : 기존 meetingIdx 말고, 참석자 순으로 정렬필요
     // 참석자테이블 left join & group by 한후 정렬  & where meetingId = idx
     // https://jojoldu.tistory.com/529?category=637935
     // meetingIdx = null & sortby = popular >> .offset(pageNo * pageSize)
}
