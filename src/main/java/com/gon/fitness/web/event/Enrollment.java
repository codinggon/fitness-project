package com.gon.fitness.web.event;

import com.gon.fitness.domain.account.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt; //참석 시간 -> 선착순에서 중요

    private boolean accepted; //참가상태 -> 확정 여부
    private boolean attended; //참석여부 (실제로 참석했는지 안 했는지)


}
