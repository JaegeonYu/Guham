package com.guham.guham.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, java.lang.Long> , QuerydslPredicateExecutor<Account> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    Account findByEmail(String email);
    Account findByNickname(String nickname);
}
