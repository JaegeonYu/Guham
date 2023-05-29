package com.guham.guham.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {
    private final AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = Account.builder()
                .nickname(nickname)
                .email(nickname + "@email.com")
                .build();
        accountRepository.save(account);
        return account;
    }
}
