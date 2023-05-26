package com.guham.guham.event;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Enrollment;
import com.guham.guham.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}