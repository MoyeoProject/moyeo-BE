package com.hanghae.finalProject.rest.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.finalProject.config.jwt.JwtUtil;
import com.hanghae.finalProject.config.security.UserDetailsImpl;
import com.hanghae.finalProject.rest.user.dto.KakaoLoginResponseDto;
import com.hanghae.finalProject.rest.user.dto.KakaoUserInfoDto;
import com.hanghae.finalProject.rest.user.model.User;
import com.hanghae.finalProject.rest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
     
     private final PasswordEncoder passwordEncoder;
     private final UserRepository userRepository;
     private final JwtUtil jwtUtil;
     @Value ("${kakao.api.key}")
     private String KAKAO_REST_API_KEY;
     
     public KakaoLoginResponseDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
          // 1. "인가 코드"로 "액세스 토큰" 요청
          String accessToken = getToken(code);
          
          // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
          KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
          
          // 3. 필요시에 회원가입
          User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
          
          // 4. JWT 토큰 반환
          String createToken = jwtUtil.createToken(kakaoUser.getUsername());
          response.addHeader(JwtUtil.AUTHORIZATION_HEADER, createToken);
          // 강제로그인
          forceLogin(kakaoUser);
          return new KakaoLoginResponseDto(kakaoUser.getId(), kakaoUser.getUsername(), kakaoUser.getProfileUrl(), createToken);
     }
     
     // 1. "인가 코드"로 "액세스 토큰" 요청
     private String getToken(String code) throws JsonProcessingException {
          // HTTP Header 생성
          HttpHeaders headers = new HttpHeaders();
          headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
          
          // HTTP Body 생성
          MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
          body.add("grant_type", "authorization_code");
          body.add("client_id", KAKAO_REST_API_KEY);
//          body.add("redirect_uri", "http://localhost:8080/api/user/kakao/callback");
//          body.add("redirect_uri", "https://sparta-hippo.shop/api/user/kakao/callback");
          body.add("redirect_uri", "http://localhost:3000/api/users/kakao/callback");
          body.add("code", code);
          
          // HTTP 요청 보내기
          HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
               new HttpEntity<>(body, headers);
          RestTemplate rt = new RestTemplate();
          ResponseEntity<String> response = rt.exchange(
               "https://kauth.kakao.com/oauth/token",
               HttpMethod.POST,
               kakaoTokenRequest,
               String.class
          );
          
          // HTTP 응답 (JSON) -> 액세스 토큰 파싱
          String responseBody = response.getBody();
          ObjectMapper objectMapper = new ObjectMapper();
          JsonNode jsonNode = objectMapper.readTree(responseBody);
          return jsonNode.get("access_token").asText();
     }
     
     // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
     private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
          // HTTP Header 생성
          HttpHeaders headers = new HttpHeaders();
          headers.add("Authorization", "Bearer " + accessToken);
          headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
          
          // HTTP 요청 보내기
          HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
          RestTemplate rt = new RestTemplate();
          ResponseEntity<String> response = rt.exchange(
               "https://kapi.kakao.com/v2/user/me",
               HttpMethod.POST,
               kakaoUserInfoRequest,
               String.class
          );
          String responseBody = response.getBody();
          log.info("responseBody!! >> {} ", responseBody);
          
          ObjectMapper objectMapper = new ObjectMapper();
          JsonNode jsonNode = objectMapper.readTree(responseBody);
          Long id = jsonNode.get("id").asLong();
          String nickname = jsonNode.get("properties")
               .get("nickname").asText();
          String profile_image = jsonNode.get("properties")
               .get("profile_image").asText();
          String email = "";
          if(jsonNode.get("kakao_account").get("email").isNull()){
               email = id + "@kakao.com";
          }else{
               email = jsonNode.get("kakao_account")
                    .get("email").asText();
          }
          log.info("email : {}", email);
          log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email + ", " + profile_image);
          return new KakaoUserInfoDto(id, nickname, email, profile_image);
     }
     
     // 3. 필요시에 회원가입
     @Transactional
     User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
          log.info("registerKakaoUserIfNeeded : {}", kakaoUserInfo);
          // DB 에 중복된 Kakao Id 가 있는지 확인
          Long kakaoId = kakaoUserInfo.getId();
          User kakaoUser = userRepository.findByKakaoId(kakaoId).orElseGet(new User());
          log.info("kakao User : {}", kakaoUser);
          if (kakaoUser == null) {
               log.info("kakao User : {}", kakaoUser);
               // 신규 회원가입
               // password: random UUID
               String password = UUID.randomUUID().toString();
               String encodedPassword = passwordEncoder.encode(password);
               
               // email: kakao email
               String email = kakaoUserInfo.getEmail();
               String profileUrl = kakaoUserInfo.getProfile_image();
               
               kakaoUser = new User(kakaoUserInfo.getNickname(), kakaoId, encodedPassword, email, profileUrl);
               userRepository.save(kakaoUser);
          }
          return kakaoUser;
     }
     
     // 강제 로그인 및 토큰생성
     private void forceLogin(User kakaoUser) {
          UserDetails userDetails = new UserDetailsImpl(kakaoUser);
          Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
     }
}
