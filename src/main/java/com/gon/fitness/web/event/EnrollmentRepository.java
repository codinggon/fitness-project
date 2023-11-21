package com.gon.fitness.web.event;

import com.gon.fitness.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account gon);

    Enrollment findByEventAndAccount(Event event, Account account);
}
