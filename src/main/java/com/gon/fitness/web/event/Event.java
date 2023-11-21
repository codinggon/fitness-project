package com.gon.fitness.web.event;

import com.gon.fitness.domain.account.Account;
import com.gon.fitness.web.account.UserAccount;
import com.gon.fitness.web.study.Study;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Study study;

    @ManyToOne(fetch = LAZY)
    private Account createdBy; //모음 생성자

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column
    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;


    public boolean canAccept(Enrollment enrollment) {
        //관리자 선택 타입이면서
        //등록한 상태면서, 한도가 남아있으면
        //
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()
                && !enrollment.isAttended() //참석 X
                && !enrollment.isAccepted(); //수락 X
    }

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended() //참석 X
                && enrollment.isAccepted(); //수락 O
    }

    public boolean isEnrollableFor(UserAccount userAccount) {
        //마지막 등록시간이 이후면 지금보다
        //참석이 완료된 유저가 아니면서
        //참석이 신청된 계정이 아니면
        return isNotClosed()
                && !isAttended(userAccount)
                && !isAlreadyEnrolled(userAccount)
                ;
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        //마지막 등록시간이 이후면 지금보다
        //참석이 완료된 유저가 아니면서
        //참석이 신청된 계정이면
        return isNotClosed()
                && !isAttended(userAccount)
                && isAlreadyEnrolled(userAccount)
                ;
    }


    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }




    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : enrollments) {
            //들어온 계정이 스터디에 신청한 계정이면서 참석이 완료되었으면
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : enrollments) {
            //들어온 계정이 스터디에 신청한 계정
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAbleToWaitingEnrollment(){
        // 선착순 타입 이면서 모음 참가 제한 횟수가 확정횟수보다 크면
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    //확정 갯수
    private long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    //연관관계 편의 메서드
    public void addEnrollment(Enrollment enrollment) {
        this.getEnrollments().add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.getEnrollments().remove(enrollment);
        enrollment.setEvent(null);
    }

    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToWaitingEnrollment()) {
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if (enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    private Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment e : enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }
        return null;
    }
}











