package com.hanghae.finalProject.rest.review.model;

import com.hanghae.finalProject.rest.meeting.model.Meeting;
import com.hanghae.finalProject.rest.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class Review {
     @Id
     @GeneratedValue (strategy = GenerationType.IDENTITY)
     private Long id;
     
     @ManyToOne (fetch = FetchType.LAZY)
     @JoinColumn(name ="meeting_id")
     private Meeting meeting; // 모임아이디
     
     @ManyToOne (fetch = FetchType.LAZY)
     @JoinColumn(name ="user_id")
     private User user; // 모임생성자

     @Column(nullable = false)
     private boolean review;
     
     public Review(Meeting meeting, User user, boolean review){
          this.meeting = meeting;
          this.user = user;
          this.review = review;
     }
}
