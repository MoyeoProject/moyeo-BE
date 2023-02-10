package com.hanghae.finalProject.rest.meeting.controller;

import com.hanghae.finalProject.config.errorcode.Code;
import com.hanghae.finalProject.config.dto.DataResponseDto;
import com.hanghae.finalProject.config.dto.ResponseDto;
import com.hanghae.finalProject.rest.meeting.dto.MeetingLinkRequestDto;
import com.hanghae.finalProject.rest.meeting.dto.MeetingRequestDto;
import com.hanghae.finalProject.rest.meeting.dto.MeetingUpdateRequestDto;
import com.hanghae.finalProject.rest.meeting.model.CategoryCode;
import com.hanghae.finalProject.rest.meeting.service.MeetingService;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService  meetingService;
    
    @ApiOperation (value = "모임 상세 조회")
    @GetMapping("/{id}")
    public ResponseDto getMeeting(@PathVariable Long id) {
        return DataResponseDto.of(meetingService.getMeeting(id));
    }
    
    @ApiOperation(value = "GET 배너이미지")
    @GetMapping("/banner")
    public ResponseDto getBanners(){
        return DataResponseDto.of(meetingService.getBanner());
    }
    
    @ApiOperation (value = "모임 전체 조회")
    @GetMapping("")
    public ResponseDto getMeetings(
         @RequestParam(value="sortby", defaultValue = "popular", required = false) String sortBy,
         @RequestParam(value="category", required = false) CategoryCode category,
         @RequestParam(value="meetingId", required = false) Long meetingId
    ){
        return DataResponseDto.of(meetingService.getMeetings(sortBy, category, meetingId));
    }
    
    @ApiOperation (value = "모임명 검색")
    @GetMapping("/search")
    public ResponseDto getMeetingsBySearch(
         @RequestParam(value="searchBy", defaultValue = "", required = false) String search,
         @RequestParam(value="category", required = false) CategoryCode category,
         @RequestParam(value="meetingId", required = false) Long meetingId
    ){
        return DataResponseDto.of(meetingService.getMeetingsBySearch(search, category, meetingId));
    }
    
    @ApiOperation (value = "모임 생성")
    @PostMapping("")
    public ResponseDto createMeeting(@ModelAttribute @Valid MeetingRequestDto requestDto) throws IOException {
        return DataResponseDto.of(meetingService.createMeeting(requestDto), Code.CREATE_MEETING.getStatusMsg());
    }
    
    @ApiOperation (value = "모임 수정")
    @PatchMapping("/{id}")
    public ResponseDto updateAllMeeting(@PathVariable Long id, @ModelAttribute @Valid MeetingUpdateRequestDto requestDto) throws IOException{
        meetingService.updateAllMeeting(id,requestDto);
        return ResponseDto.of(true, Code.UPDATE_MEETING);
    }
    
    @ApiOperation (value = "모임 이미지 수정")
    @PatchMapping("/{id}/image")
    public ResponseDto updateMeetingImage(@PathVariable Long id, @ModelAttribute @Valid MeetingUpdateRequestDto.Image requestDto) throws IOException{
        meetingService.updateMeetingImage(id,requestDto);
        return ResponseDto.of(true, Code.UPDATE_MEETING_IMAGE);
    }

    @ApiOperation (value = "모임 수정 페이지")
    @GetMapping("/{id}/update")
    public ResponseDto getUpdatePage(@PathVariable Long id) {
        return DataResponseDto.of(meetingService.getUpdatePage(id), Code.GET_UPDATE_PAGE.getStatusMsg());
    }


    @ApiOperation (value = "모임 링크 수정")
    @PatchMapping("/{id}/link")
    public ResponseDto updateLink(@PathVariable Long id, @RequestBody @Valid MeetingLinkRequestDto requestDto) {
        meetingService.updateLink(id, requestDto);
        return ResponseDto.of(true, Code.UPDATE_LINK);
    }
    
    @ApiOperation (value = "모임 삭제")
    @DeleteMapping("/{id}")
    public ResponseDto deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseDto.of(true, Code.DELETE_MEETING);
    }
}
