package com.hanghae.finalProject.rest.meeting.repository;

import com.hanghae.finalProject.rest.attendant.dto.AttendantResponseDto;
import com.hanghae.finalProject.rest.attendant.repository.AttendantRepository;
import com.hanghae.finalProject.rest.meeting.dto.MeetingListResponseDto;
import com.hanghae.finalProject.rest.meeting.model.CategoryCode;
import com.hanghae.finalProject.rest.meeting.model.Meeting;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import static com.hanghae.finalProject.rest.attendant.model.QAttendant.attendant;
import static com.hanghae.finalProject.rest.meeting.model.QMeeting.meeting;
import static com.hanghae.finalProject.rest.user.model.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@Repository
public class MeetingCustomRepositoryImpl implements MeetingCustomRepository {
     
     private final JPAQueryFactory jpaQueryFactory;
     private final AttendantRepository attendantRepository;
     
     public MeetingCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory,
                                        AttendantRepository attendantRepository) {
          this.jpaQueryFactory = jpaQueryFactory;
          this.attendantRepository = attendantRepository;
     }
     
     // 검색리스트
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllBySearchAndCategory(String search, CategoryCode category, Long meetingIdx) {
          // 1) 커버링 인덱스로 대상 조회
          List<Long> ids = jpaQueryFactory
               .select(meeting.id)
               .from(meeting)
               .where(eqCategory(category), // 카테고리 필터링
                    meeting.title.contains(search), // 검색어 필터링
                    ltBookId(meetingIdx),// 무한스크롤용
                    meeting.deleted.eq(false))
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
               .where(meeting.id.in(ids))
               .orderBy(meeting.id.desc())
               .transform(getList());
     }
     
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllSortByPopularAndCategory(CategoryCode category, Long pageNum) {
          // 1) 커버링 인덱스로 대상 조회
          List<Long> ids = jpaQueryFactory
               .select(attendant.meeting.id)
               .from(attendant)
               .join(meeting)
               .on(attendant.id.eq(meeting.id))
               .groupBy(attendant.meeting.id)
               .where(eqCategory(category),
                    meeting.deleted.eq(false)
               )
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
               .transform(getList());
     }
     
     @Override
     public List<MeetingListResponseDto.ResponseDto> findAllSortByNewAndCategory(CategoryCode category, Long meetingIdx) {
          return jpaQueryFactory
               .from(meeting)
               .leftJoin(attendant).on(meeting.id.eq(attendant.meeting.id))
               .leftJoin(user).on(attendant.user.id.eq(user.id))
               .where(
                    ltBookId(meetingIdx),
                    eqCategory(category),
                    meeting.deleted.eq(false)
               )
               .orderBy(meeting.id.desc())
               .limit(5)
               .transform(getList());
     }
     
     private static ResultTransformer<List<MeetingListResponseDto.ResponseDto>> getList() {
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
                    meeting.content,
                    meeting.maxNum,
                    meeting.secret,
                    meeting.password,
                    list(
                         Projections.fields(
                              AttendantResponseDto.simpleResponseDto.class,
                              attendant.user.id.as("userId"),
                              user.profileUrl.as("userProfileImg")
                         )
                    ).as("attendantsList")
               ));
     }
     
     // 무한스크롤용. 해당 idx 보다 작은것들 불러오기 (sortBy new 일때만 이거)
     private BooleanExpression ltBookId(Long meetingIdx) {
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
     
     // 인기순 : attendant 테이블에서 참석자 많은 meeting_id 순으로 정렬해와야 함
     // >> 무한스크롤 : 기존 meetingIdx 말고, 참석자 순으로 정렬필요
     // 참석자테이블 left join & group by 한후 정렬  & where meetingId = idx
     // https://jojoldu.tistory.com/529?category=637935
     // meetingIdx = null & sortby = popular >> .offset(pageNo * pageSize)
}