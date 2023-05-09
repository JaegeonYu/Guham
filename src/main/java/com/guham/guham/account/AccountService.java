package com.guham.guham.account;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Tag;
import com.guham.guham.settings.form.Notifications;
import com.guham.guham.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;


    public Account signUp(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmMail(newAccount);
        return newAccount;
    }

    // Controller + 재전송 호출 시 Managed Entity 넘기기
    public void sendSignUpConfirmMail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("구함, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = signUpForm.toEntity();
        account.generateEmailCheckToken();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void logIn(Account account) {
        // LawPassWord 이용하지 않기 위한 방법
        syncAuthenticationAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nicknameOrEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(nicknameOrEmail);
        if (account == null) {
            account = accountRepository.findByNickname(nicknameOrEmail);
        }

        if (account == null) {
            throw new UsernameNotFoundException(nicknameOrEmail);
        }
        return new UserAccount(account);
    }


    public void completeSignUp(Account account) {
        account.completeSignUp();
        logIn(account);
    }

    private void syncAuthenticationAccount(Account account) {
        Authentication token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);
    }


    public void updateProfile(Long accountId, Profile profile) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateProfile(profile);
        syncAuthenticationAccount(findAccount);
    }

    public void updatePassword(Long accountId, String newPassword) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updatePassword(passwordEncoder.encode(newPassword));
        syncAuthenticationAccount(findAccount);
    }

    public void updateNotifications(Long accountId, Notifications notifications) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateNotifications(notifications);
        syncAuthenticationAccount(findAccount);
    }

    public void updateNickname(Long accountId, String nickname) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateNickname(nickname);
        syncAuthenticationAccount(findAccount);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("팀빌딩구함, 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
    }

    public void addTag(Long accountId, Tag tag) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getTags().add(tag));
    }

    public Set<Tag> getTag(Long accountId) {
        Optional<Account> byId = accountRepository.findById(accountId);
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Long accountId, Tag tag) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getTags().remove(tag));
    }
}
