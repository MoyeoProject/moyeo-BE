package com.hanghae.finalProject.rest.meeting.service;

import com.hanghae.finalProject.config.S3.S3Uploader;
import com.hanghae.finalProject.config.errorcode.Code;
import com.hanghae.finalProject.config.exception.RestApiException;
import com.hanghae.finalProject.config.util.RedisUtil;
import com.hanghae.finalProject.config.util.SecurityUtil;
import com.hanghae.finalProject.rest.alarm.repository.AlarmRepository;
import com.hanghae.finalProject.rest.alarm.service.AlarmService;
import com.hanghae.finalProject.rest.attendant.model.Attendant;
import com.hanghae.finalProject.rest.attendant.repository.AttendantRepository;
import com.hanghae.finalProject.rest.calendar.repository.CalendarRepository;
import com.hanghae.finalProject.rest.follow.repository.FollowRepository;
import com.hanghae.finalProject.rest.meeting.dto.*;
import com.hanghae.finalProject.rest.meeting.model.Banner;
import com.hanghae.finalProject.rest.meeting.model.CategoryCode;
import com.hanghae.finalProject.rest.meeting.model.Meeting;
import com.hanghae.finalProject.rest.meeting.repository.MeetingRepository;
import com.hanghae.finalProject.rest.review.repository.ReviewRepository;
import com.hanghae.finalProject.rest.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
     private final MeetingRepository meetingRepository;
     private final ReviewRepository reviewRepository;
     private final CalendarRepository calendarRepository;
     private final AlarmRepository alarmRepository;
     private final AttendantRepository attendantRepository;
     private final FollowRepository followRepository;
     private final AlarmService alarmService;
     @Autowired
     private ApplicationContext applicationContext;
     
     private final S3Uploader s3Uploader;
     
     // 모임 상세조회
     @Transactional (readOnly = true)
     public MeetingDetailResponseDto getMeeting(Long id) {
          User user = SecurityUtil.getCurrentUser();
          // 비회원도 공유를 통해서 페이지를 볼 수 있어야 되니까 null 예외 처리 XX
          
          // 모임 존재여부 확인
          MeetingDetailResponseDto meetingDetailResponseDto = meetingRepository.findByIdAndAttendAndAlarmAndLike(id, user);
          if (meetingDetailResponseDto == null) throw new RestApiException(Code.NO_MEETING);
          
          if (user != null && Objects.equals(user.getId(), meetingDetailResponseDto.getMasterId())) {
               meetingDetailResponseDto.isMaster(true);
          }
          return meetingDetailResponseDto;
     }
     
     // 모임생성
     @Transactional
     public MeetingCreateResponseDto createMeeting(MeetingRequestDto requestDto) throws IOException {
          // 유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
          //이미지 데이터 넣기
          String image = null;
          //이미지가 있으면 넣어주고 없으면 넘어가는 if문
          if (!Objects.isNull(requestDto.getImage()) && !requestDto.getImage().isEmpty() && !requestDto.getImage().getContentType().isEmpty()) {
               image = s3Uploader.upload(requestDto.getImage(), "image");
          }
          // 비밀방일경우 비번4글자 확인
          checkPassword(requestDto.isSecret(), requestDto.getPassword());
          
          Meeting meeting = meetingRepository.saveAndFlush(new Meeting(requestDto, user, image));
          
          // 참석자리스트에 방장 추가
          Attendant attendant = new Attendant(meeting, user);
          attendantRepository.save(attendant);

          //글 작성자 팔로워에게 알림
          alarmService.alarmFollowers(meeting, user);
          
          return new MeetingCreateResponseDto(meeting);
     }
     
     // 모임수정
     @Transactional
     public void updateAllMeeting(Long id, MeetingUpdateRequestDto requestDto) throws IOException {
          //유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
          // 비밀방일경우 비번4글자 확인
          checkPassword(requestDto.isSecret(), requestDto.getPassword());
          
          Meeting meeting = meetingRepository.findById(id).orElseThrow(() -> new RestApiException(Code.NO_MEETING));
          LocalDate dateOrigin = meeting.getStartDate();
          // 모임 글 삭제 여부 확인
          if (meeting.isDeleted()) {
               throw new RestApiException(Code.NO_MEETING);
          }
          //이미지 데이터 넣기
          String image = null;
          //이미지가 있으면 넣어주고 없으면 넘어가는 if문
          if (!Objects.isNull(requestDto.getImage()) && !requestDto.getImage().isEmpty() && !requestDto.getImage().getContentType().isEmpty()) {
               image = s3Uploader.upload(requestDto.getImage(), "image");
          }
          // 글 작성자과 일치 여부 확인
          if (Objects.equals(user.getId(), meeting.getUser().getId())) {
               meeting.updateAll(requestDto, image);
               removeCache(meeting, dateOrigin);
               // 알림
               alarmService.alarmUpdateMeeting(meeting);
          } else {
               throw new RestApiException(Code.INVALID_USER);
          }
     }
     
     private static void checkPassword(boolean isSecret, String requestDto) {
          if (isSecret) {
               if (requestDto.length() != 4) {
                    throw new RestApiException(Code.WRONG_SECRET_PASSWORD);
               }
          }
     }
     
     // 모임이미지만 수정
     @Transactional
     public void updateMeetingImage(Long id, MeetingUpdateRequestDto.Image requestDto) throws IOException {
          // 유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
     
          Meeting meeting = meetingRepository.findById(id).orElseThrow(() -> new RestApiException(Code.NO_MEETING));
          LocalDate dateOrigin = meeting.getStartDate();
          if (meeting.isDeleted()) {
               throw new RestApiException(Code.NO_MEETING);
          }
          //이미지 데이터 넣기
          String image = null;
          //이미지가 있으면 넣어주고 없으면 넘어가는 if문
          if (!requestDto.getImage().isEmpty() && !requestDto.getImage().getContentType().isEmpty()) {
               image = s3Uploader.upload(requestDto.getImage(), "image");
          }
          // 방장일경우만 가능
          if (Objects.equals(user.getId(), meeting.getUser().getId())) {
               meeting.updateImage(image);
               // 캘린더 캐시데이터 삭제
               removeCache(meeting, dateOrigin);
          } else {
               throw new RestApiException(Code.INVALID_USER);
          }
     }
     
     private void removeCache(Meeting meeting, LocalDate dateOrigin) {
          List<Attendant> attendantList = attendantRepository.findAllByMeeting(meeting).stream()
               .peek(
                    a -> getSpringProxy().deleteCache(a.getUser().getId(), dateOrigin.getYear(), dateOrigin.getMonthValue())
               ).collect(Collectors.toList());
     }
     
     private static void forLoggedUser(User user) {
          if (user == null) throw new RestApiException(Code.NOT_FOUND_AUTHORIZATION_IN_SECURITY_CONTEXT);
     }
     
     
     // GET 모임수정페이지
     @Transactional (readOnly = true)
     public MeetingUpdatePageResponseDto getUpdatePage(Long id) {
          // 유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
     
          // 모임 글 존재 여부 확인
          Meeting meeting = meetingRepository.findById(id).orElseThrow(() -> new RestApiException(Code.NO_MEETING));

          // 삭제 여부 확인
          if (meeting.isDeleted()) {
               throw new RestApiException(Code.NO_MEETING);
          }

          // 글 작성자와 일치 여부 확인
          if (user.getId() == meeting.getUser().getId()) {
               return new MeetingUpdatePageResponseDto(meeting);
          } else {
               throw new RestApiException(Code.INVALID_USER);
          }
     }
     
     // 링크 업데이트
     @Transactional
     public void updateLink(Long id, MeetingLinkRequestDto requestDto) {
          // 유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
     
          // 모임 글 존재 여부 확인
          Meeting meeting = meetingRepository.findById(id).orElseThrow(() -> new RestApiException(Code.NO_MEETING));

          // 모임 글 삭제 여부 확인
          if (meeting.isDeleted()) {
               throw new RestApiException(Code.NO_MEETING);
          }

          // 글 작성자와 일치 여부 확인
          if (user.getId() == meeting.getUser().getId()) {
               meeting.updateLink(requestDto);

               // 알림
               alarmService.alarmUpdateLink(meeting);
          } else {
               throw new RestApiException(Code.INVALID_USER);
          }
     }
     
     // 모임 삭제
     @Transactional
     public void deleteMeeting(Long id) {
          // 유저 확인
          User user = SecurityUtil.getCurrentUser();
          forLoggedUser(user);
          // 모임 존재 여부 확인
          Meeting meeting = meetingRepository.findById(id).orElseThrow(() -> new RestApiException(Code.NO_MEETING));
          // 삭제 여부 확인
          if (meeting.isDeleted()) {
               throw new RestApiException(Code.NO_MEETING);
          }
          // 작성자 일치 여부 확인
          if (user.getId() == meeting.getUser().getId()) {
               meeting.deleteMeeting();
               //알림
               alarmService.alarmDeleteMeeting(meeting);
          } else {
               throw new RestApiException(Code.INVALID_USER);
          }
          
     }
     
     // 모임 전체리스트 불러오기
     @Transactional (readOnly = true)
     public MeetingListResponseDto getMeetings(String sortBy, CategoryCode category, Long meetingIdx) {
          User user = SecurityUtil.getCurrentUser(); // 비회원일경우(토큰못받았을경우) null
          forLoggedUser(user);
     
          MeetingListResponseDto response = new MeetingListResponseDto();
          List<MeetingListResponseDto.ResponseDto> responseDtoList = (sortBy.equals("new")) ?
               meetingRepository.findAllSortByNewAndCategory(category, meetingIdx) // 신규순
               : meetingRepository.findAllSortByPopularAndCategory(category, meetingIdx); // 인기순
          
          responseDtoList = refactorResponseDtoList(user, responseDtoList);
          
          // 인기순일 경우 : 재정렬 필요 > 인기순 + 마감날짜빠른순 + 최신순
          if (sortBy.equals("popular")) {
               responseDtoList = responseDtoList.stream().sorted(Comparator.comparing(MeetingListResponseDto.ResponseDto::getAttendantsNum).reversed()
                    .thenComparing(MeetingListResponseDto.ResponseDto::getStartTime).reversed()
                    .thenComparing(MeetingListResponseDto.ResponseDto::getId).reversed()).collect(Collectors.toList()
               );
          }
          return response.addMeetingList(responseDtoList);
     }
     
     private static List<MeetingListResponseDto.ResponseDto> refactorResponseDtoList(User user, List<MeetingListResponseDto.ResponseDto> responseDtoList) {
          return responseDtoList.stream()
               // meeting 작성자의 id와 로그인 유저의 아이디 비교
               .peek(m -> {
                    // master 처리
                    m.setMaster(m.getMasterId().equals(user.getId()));
                    // getAttendantsList 안에 null인 경우도 넘어와서 객체 생겨버림
                    if (m.getAttendantsNum()== 0) {
                         m.setAttendantsList(null);
                    } else {
                         // 참석자리스트에 로그인유저가 있는가
                         m.setAttend(m.getAttendantsList().stream().anyMatch(a -> a.getUserId().equals(user.getId())));
                    }
               }).collect(Collectors.toList());
     }
     
     // 제목 검색 모임리스트 불러오기
     @Transactional (readOnly = true)
     public MeetingListResponseDto getMeetingsBySearch(String search, CategoryCode category, Long meetingId) {
          User user = SecurityUtil.getCurrentUser(); // 비회원일경우(토큰못받았을경우) null
          forLoggedUser(user);
     
          MeetingListResponseDto response = new MeetingListResponseDto();
          response.addMeetingList(refactorResponseDtoList(user, meetingRepository.findAllBySearchAndCategory(search, category, meetingId)));
          return response;
     }
     
     private RedisUtil getSpringProxy() {
          return applicationContext.getBean(RedisUtil.class);
     }
     
     public List<String> getBanner() {
          return Banner.getImgList();
     }
}
