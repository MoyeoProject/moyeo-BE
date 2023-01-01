package com.hanghae.finalProject.rest.user.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity (name = "users")
@RequiredArgsConstructor
@Getter
public class User {
     //- username은  `최소 4자 이상, 10자 이하이며 알파벳 소문자(a~z), 숫자(0~9)`로 구성되어야 한다.
     //- password는  `최소 8자 이상, 15자 이하이며 알파벳 대소문자(a~z, A~Z), 숫자(0~9)`로 구성되어야 한다.
     //- DB에 중복된 username이 없다면 회원을 저장하고 Client 로 성공했다는 메시지, 상태코드 반환하기
     @Id
     @GeneratedValue (strategy = GenerationType.IDENTITY)
     private Long id;
     
     @Column (nullable = false, unique = true)
     private String username;
     
     @Column(nullable = false)
     private String password;
     
     @Column(nullable = false)
     private String email; // 카카오 메일주소와 중복가능(다른유저로봄). unique False
     
     @Column
     private String profileUrl;
     
     @Column
     private Boolean deleted = false;
     
     @Column(nullable = true, unique = true)
     private String kakaoId;
     
     
}
