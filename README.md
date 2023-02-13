 <img width="250" alt="짤방" src="https://blog.kakaocdn.net/dn/MFfKe/btrZa2ei4oG/yfksBvTWbCUXsS04cXfOD1/img.gif"> 

# 모여 | Moyeo

이불 밖은 위험해! <br />
메타버스의 시대가 펼쳐지면서 방구석 모임이 늘어났습니다. <br />
좋아하는 취미생활을 방구석에서 공유하고 싶은 사람들을 위한 모임 <br />

 <img width="650" alt="스크린샷_20221229_051305" src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FUEwjn%2FbtrX6oXDnJL%2FpzzdRMhRRWjHKeHKvgyfKk%2Fimg.png">
  <img width="650" alt="스크린샷_20221229_051305" src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbKB0ev%2FbtrYaphpRsY%2FvmkAdPoobKpkL5kNNiWOeK%2Fimg.png">
   <img width="650" alt="스크린샷_20221229_051305" src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb7jduA%2FbtrYidNXHRK%2F4rqYbRnbuM0xiEmmJGruzK%2Fimg.png">
 
### 🔗 배포 링크 Hosting URL

https://moyeo.vercel.app


### ✨ 개발기간

2022.12.30 ~ 2023.02.10


### ⭐ 주요기능

1. 온라인 모임 약속을 만들어 함께할 사람들을 모으는 모임 생성 기능

2. 원하는 모임을 찾을 수 있는 검색 및 카테고리 기능

3. 원하는 모임을 참석 예약하기 및 참석한 모임 시작을 잊지 않도록 알림 받기

4. 내가 참석한 모임들을 한눈에 볼 수 있는 나의 모임 달력

5. 모임 방장에게 모임의 신규 인원 참석 및 댓글에 대한 실시간 알림 전송

6. 내가 원하는 사람을 팔로우하여 해당 유저의 모임이 생기면 알림 받기

7. 참석/입장했던 모임에 대해 좋아요/싫어요 후기 남기기
 

### ⭐ 트러블 슈팅
 
#### 1. 인기순조회 트러블슈팅
1) 문제 상황
- 인기모임 조회 속도향상을 위해 커버링인덱스를 적용하였음에도, 실행계획 확인결과 Total Cost 가 매우 높게 발생

2) 문제 원인
- 참여자수 비교를 위해 attendant테이블과 left join, group by가 필요하여 meeting table의 full index scan을 통해 
  전체 컬럼에 대해 join이 실행되어 total cost가 높게 나타는 것

3) 해결 방법
- meeting 테이블에 참여자수 컬럼을 새로 추가하여 join없이 바로 인기순 정렬이 가능하도록 변경하고, 참석자수 + id 인덱스테이블을 생성하여
  최초 actual rows 9126 > 5 로 감소, jmeter 로 테스트해본 결과 평균응답시간은 60% 감소, 초당 처리건수의 경우 260%가 증가

#### 2. 검색조회 트러블슈팅
1) 문제 상황
- 모임검색의 경우 like 쿼리를 이용하여 검색을 진행하였는데, 
  5개의 limit와, 커버링인덱싱을 적용하여 속도를 높였으나,  최초 Actual Rows가 모임 전체수만큼 나오는것을 확인

2) 문제 원인
-  like 쿼리의 경우 검색할 단어의 인덱싱적용이 되지않아 전체 모임을 조회

3) 해결 방법
- like보다 인덱스를 효율적으로 사용할 수 있는 Full Text Search engine을 적용하였으며 그결과 최초 Actual Rows 1/100 으로 감소, total cost : 1/3로 감소
- jmeter 테스트 결과 평균응답시간은 60% 감소, 초당 처리건수의 경우 244%가 증가한것을 확인

### 🧩swagger [LINK](https://sparta-hippo.shop/swagger-ui/index.html)


### ⭐ API 명세서 [LINK](https://descriptive-handbell-23e.notion.site/b53182796d1940959c2223cdf8792b44?v=2e290f73ddaf429eb21e9efffed7121d)


### 🛠 기술스택

![자바](https://user-images.githubusercontent.com/108880977/209101862-e833ffc2-7cab-4114-8b74-5766d25b226b.svg)
![스프링부트](https://user-images.githubusercontent.com/108880977/209099782-f0f6fbb6-8c55-4a0e-a7a2-53fd5a000493.svg)
![시큐리티](https://user-images.githubusercontent.com/108880977/209101809-e972b9cf-36e1-4db3-a9ed-6474bc88770e.svg)
![JPA](https://user-images.githubusercontent.com/108880977/209104203-cccd4e80-5279-4e89-9453-c9d2333570b5.svg)
![JWT](https://user-images.githubusercontent.com/108880977/209102757-eb3f840f-ca24-4c89-a2b5-c60fff46bf49.svg)
![GRADLE](https://user-images.githubusercontent.com/108880977/209101888-8ea11829-e1b1-4de2-b7b4-8716e99dcf05.svg)
![INTELLIJ](https://camo.githubusercontent.com/699cfd7f3bb6a4e1764449f9b0da88a99a8d46bee71b93752b15ee8fbca5026a/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f496e74656c6c694a20494445412d3030303030303f7374796c653d666f722d7468652d6261646765266c6f676f3d496e74656c6c694a2049444541266c6f676f436f6c6f723d7768697465)
![MYSQL](https://user-images.githubusercontent.com/108880977/209101897-c8a4fa60-6fb0-4501-b30f-06269e75ce11.svg)
![아마존 RDS](https://user-images.githubusercontent.com/108880977/209103424-828b0d5b-9419-4ebb-8a85-24bbc3072213.svg)
![아마존 AWS](https://user-images.githubusercontent.com/108880977/209103421-1cf57ef4-8620-4932-8704-60d0ec14ed1f.svg)
![EC22](https://user-images.githubusercontent.com/108880977/209104209-b04b40b7-a847-4263-aeb8-de19bc7fa8d9.svg)
![UBUNTU](https://camo.githubusercontent.com/51b0015a5bd40a05477f41af2f74c18d4b3d67388e3a0fed881a1243e0766f95/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f5562756e74752d4539353432303f7374796c653d666f722d7468652d6261646765266c6f676f3d5562756e7475266c6f676f436f6c6f723d7768697465)
![아마존S3](https://camo.githubusercontent.com/f5e36b504a7091d22de49844ec28d7b50723774c367b6133fb25dd73e4876b92/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f416d617a6f6e2053332d3536394133313f7374796c653d666f722d7468652d6261646765266c6f676f3d416d617a6f6e205333266c6f676f436f6c6f723d7768697465)
![CODEDEPLOY](https://camo.githubusercontent.com/f0cede42e8391ba6bb70096f58bc63c8f5c846ea5cde8f27327e571a99e9a3e0/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f636f64656465706c6f792d3644423333463f7374796c653d666f722d7468652d6261646765266c6f676f3d636f64656465706c6f79266c6f676f436f6c6f723d7768697465)
![REDIS](https://camo.githubusercontent.com/a069a550246061f739515a814dfcb2825db8fcce1017180544585ac1982b0426/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f52656469732d4443333832443f7374796c653d666f722d7468652d6261646765266c6f676f3d5265646973266c6f676f436f6c6f723d7768697465)
![GIT](https://camo.githubusercontent.com/fdb91eb7d32ba58701c8e564694cbe60e706378baefa180dbb96e2c1cfb9ec0f/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f4769742d4630353033323f7374796c653d666f722d7468652d6261646765266c6f676f3d476974266c6f676f436f6c6f723d7768697465)
![GITHUB](https://camo.githubusercontent.com/ad176bb5a61237550550e47d7e77dd5d1a846518df44c522d2ba9c0a7da6379c/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f6769746875622d3138313731373f7374796c653d666f722d7468652d6261646765266c6f676f3d676974687562266c6f676f436f6c6f723d7768697465)
![GITHUB ACTIONS](https://camo.githubusercontent.com/848a56128bd7fb616d4513033e90bdd63c7af1cf66a0e4e96c817cc514638499/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f47697448756220416374696f6e732d3230383846463f7374796c653d666f722d7468652d6261646765266c6f676f3d47697448756220416374696f6e73266c6f676f436f6c6f723d7768697465)
![KAKAO LOGIN](https://camo.githubusercontent.com/83ef272c4b02509566e09bfa9d4bde5c1c2fbfbfe00346c28b8cd01f005a4932/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f6b616b616f206c6f67696e2d4646434430303f7374796c653d666f722d7468652d6261646765266c6f676f3d6b616b616f266c6f676f436f6c6f723d626c61636b)
![소스트리](https://camo.githubusercontent.com/3a8be4d33166b6bd1610e3af88b5236ee322a3773aaaad8ae74d0455dd570466/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f536f75726365747265652d3030353243433f7374796c653d666f722d7468652d6261646765266c6f676f3d536f7572636574726565266c6f676f436f6c6f723d7768697465)
![POSTMAN](https://camo.githubusercontent.com/879423585ed087f3c973857c43ba7e7d84f52c993d2c937055726339fbf921d9/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f506f73746d616e2d4646364333373f7374796c653d666f722d7468652d6261646765266c6f676f3d506f73746d616e266c6f676f436f6c6f723d7768697465)
![NOTION](https://camo.githubusercontent.com/e68c6b9ddf0d0aa553f47fa6a1beb2b60176552e8a7862fe2932c03078a45376/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f4e6f74696f6e2d3030303030303f7374796c653d666f722d7468652d6261646765266c6f676f3d4e6f74696f6e266c6f676f436f6c6f723d7768697465)
 ---
 
 
 ### ⭐ Member 
 
 BE 😶장영주, 😶김지민, 😶윤덕현
 
 FE 😀박선영, 😀정소영

 - FE 프론트엔드 깃허브로 이동
 https://github.com/MoyeoProject/moyeo-FE


 ### ⚙️ ERD
 
 <img width="784" alt="스크린샷_20221229_051305" src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcCtqz9%2FbtrWO2BiCvA%2FICUfcK9BXKbukblypkwOgK%2Fimg.png">
 
 
 ### ⚙️ Service Architecture
 
<img width="1078" alt="스크린샷 2023-01-20 오후 2 36 01" src="https://blog.kakaocdn.net/dn/drZCFW/btrX40CBCva/cpmpIqPKkKerKxnEfk4fv0/tfile.svg">
 
