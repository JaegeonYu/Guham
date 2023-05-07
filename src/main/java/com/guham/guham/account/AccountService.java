package com.guham.guham.account;

import com.guham.guham.domain.Account;
import com.guham.guham.settings.Notifications;
import com.guham.guham.settings.Profile;
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

    public void sendSignUpConfirmMail(Account newAccount) {
        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("구함, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

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
        if(account == null){
            account = accountRepository.findByNickname(nicknameOrEmail);
        }

        if(account == null){
            throw new UsernameNotFoundException(nicknameOrEmail);
        }
        return new UserAccount(account);
    }


    public void completeSignUp(Account account) {
        account.completeSignUp();
        logIn(account);
    }

    private void syncAuthenticationAccount(Account accountId){
        Authentication token = new UsernamePasswordAuthenticationToken(
                new UserAccount(accountId),
                accountId.getPassword(),
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

    public void updateNickname(Long accountId, String nickname){
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateNickname(nickname);
        syncAuthenticationAccount(findAccount);
    }
}
