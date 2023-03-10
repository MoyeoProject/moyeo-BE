package com.hanghae.finalProject.rest.meeting.controller;

import com.hanghae.finalProject.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MeetingControllerTest extends AcceptanceTest {
     private static final String EMAIL = "jojtest123@nate.com";
     private static final String PASSWORD = "joung18@#$";
     private static final Long MEETINGID = 10259L;
     
     @DisplayName ("모임 상세 조회")
     @Test
     void getMeetingTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get("/api/meetings/"+MEETINGID)
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.body().jsonPath().getLong("data.id")).isEqualTo(MEETINGID);
     }
     @DisplayName ("GET 배너이미지")
     @Test
     void getBannersTest() {
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .when()
                    .get("/api/meetings/banner")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getList("data")).isNotNull();
     }
     
     @DisplayName("모임 전체 조회")
     @Test
     void getMeetingsTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .param("sortyby", "popular")
                    .param("category","밥모여")
                    .param("meetingId",0)
                    .when()
                    .get("/api/meetings")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getList("data.meetingList")).isNotNull();
     }
     
     @DisplayName("모임명 검색")
     @Test
     void getMeetingsBySearchTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .param("searchBy", "비번")
                    .param("category","밥모여")
                    .param("meetingId")
                    .when()
                    .get("/api/meetings/search")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getList("data.meetingList")).isNotNull();
     }
     @DisplayName("모임 생성")
     @Test
     void createMeetingTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .multiPart(new MultiPartSpecBuilder(new File("./src/main/resources/image/rian1.jpg")).controlName("image").fileName("rian1.jpg").with().charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("테스트다아아").controlName("title").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("일단모여").controlName("category").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("2023-06-10").controlName("startDate").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("20:00:00").controlName("startTime").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("3").controlName("duration").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("피카피카").controlName("content").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("10").controlName("maxNum").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("ZOOM").controlName("platform").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("").controlName("link").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("false").controlName("secret").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("").controlName("password").charset("utf-8").build())
                    .contentType("multipart/form-data")
                    .when()
                    .post("/api/meetings")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getString("data.title")).isEqualTo("테스트다아아");
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("모임 글 작성 성공");
     }
     
     @DisplayName("모임 수정")
     @Test
     void updateAllMeetingTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .multiPart(new MultiPartSpecBuilder(new File("./src/main/resources/image/rian1.jpg")).controlName("image").fileName("rian1.jpg").with().charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("테스트다아아").controlName("title").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("2023-06-11").controlName("startDate").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("20:00:00").controlName("startTime").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("5").controlName("duration").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("피카피카2").controlName("content").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("ZOOM").controlName("platform").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("").controlName("link").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("false").controlName("secret").charset("utf-8").build())
                    .multiPart(new MultiPartSpecBuilder("").controlName("password").charset("utf-8").build())
                    .contentType("multipart/form-data")
                    .when()
                    .patch("/api/meetings/"+MEETINGID)
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("모임 글 수정 성공");
     }
     
     @DisplayName("모임 이미지 수정")
     @Test
     void updateMeetingImageTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          File file = new File("./src/main/resources/image/rian1.jpg");
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .multiPart(new MultiPartSpecBuilder(file).controlName("image").fileName("rian1.jpg").with().charset("utf-8").build())
                    .contentType("multipart/form-data")
                    .when()
                    .patch("/api/meetings/"+MEETINGID+"/image")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("모임 글 이미지 수정 성공");
     }
     
     @DisplayName("GET 모임 수정 페이지")
     @Test
     void getUpdatePageTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .when()
                    .get("/api/meetings/"+MEETINGID+"/update")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("모임 글 수정 페이지 불러오기 성공");
          assertThat(response.body().jsonPath().getString("data.category")).isEqualTo("일단모여");
     }
     
     @DisplayName("모임 링크 수정")
     @Test
     void updateLinkTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          Map<String, String> params = new HashMap<>();
          params.put("platform", "ZOOM");
          params.put("link", "abcde");
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .body(params)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .patch("/api/meetings/"+MEETINGID+"/link")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("모임 링크 추가 성공");
     }
     
     @DisplayName("모임 삭제")
     @Test
     void deleteMeetingTest() {
          // 로그인 토큰구하기
          String accessToken = getToken();
          // Given
          // When
          ExtractableResponse<Response> response =
               RestAssured
                    .given().log().all()
                    .header("Authorization", accessToken)
                    .when()
                    .delete("/api/meetings/10265")
                    .then().log().all()
                    .extract();
          // Then
          assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
          assertThat(response.body().jsonPath().getString("statusMsg")).isEqualTo("작성자만 삭제/수정할 수 있습니다.");
     }
     
     private static String getToken() {
          // 로그인 토큰구하기
          Map<String, String> loginParams = new HashMap<>();
          loginParams.put("email", EMAIL);
          loginParams.put("password", PASSWORD);
          
          ExtractableResponse<Response> response1 = RestAssured.given().log().all()
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(loginParams)
               .when().post("/api/users/login")
               .then().extract();
          
          String accessToken = response1.header("Authorization");
          return accessToken;
     }
}