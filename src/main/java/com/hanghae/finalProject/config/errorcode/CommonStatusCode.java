package com.hanghae.finalProject.config.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum CommonStatusCode implements StatusCode {
     OK(true,"정상", HttpStatus.OK.value()),
     FILE_SAVE_FAIL("파일 저장에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
     WRONG_IMAGE_FORMAT("지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST.value()),
     POST_LIKE(true, "좋아요", HttpStatus.OK.value()),
     POST_LIKE_CANCEL(true, "좋아요취소", HttpStatus.OK.value()),
     DELETE_COMMENT(true, "댓글 삭제 성공", HttpStatus.OK.value()),
     CREATE_COMMENT(true, "댓글 작성 성공", HttpStatus.OK.value()),
     UPDATE_COMMENT(true, "댓글 수정 성공", HttpStatus.OK.value()),
     CREATE_POST(true, "게시글 작성 성공", HttpStatus.OK.value()),
     UPDATE_POST(true, "게시글 수정 성공", HttpStatus.OK.value()),
     DELETE_POST(true, "게시글 삭제 성공", HttpStatus.OK.value()),
     INVALID_PARAMETER("Invalid parameter included",HttpStatus.BAD_REQUEST.value()),
     INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()),
     NO_ARTICLE("게시글이 존재하지 않습니다", HttpStatus.NOT_FOUND.value()),
     NO_COMMENT("댓글이 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),
     INVALID_USER("작성자만 삭제/수정할 수 있습니다.", HttpStatus.BAD_REQUEST.value()),
     DELETE_USER(true, "회원 탈퇴 성공", HttpStatus.OK.value());
     
     private boolean success = false;
     private final String StatusMsg;
     private final int statusCode;
     
}
