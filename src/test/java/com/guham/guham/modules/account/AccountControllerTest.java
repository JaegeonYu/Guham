package com.guham.guham.modules.account;

import com.guham.guham.infra.AbstractContainerTest;
import com.guham.guham.infra.MockMVCTest;
import com.guham.guham.infra.mail.EmailMessage;
import com.guham.guham.infra.mail.EmailService;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.account.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMVCTest
class AccountControllerTest extends AbstractContainerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("회원 가입 화면 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 오류")
    public void signUpSubmitWithWrongInput() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "jaegeon")
                .param("email","email")
                .param("password", "123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    public void signUpSubmitWithRightInput() throws Exception {
        String email = "yjk9805@naver.com";
        String rawPassword = "1q2w3e4r";
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "jaegeon")
                        .param("email",email)
                        .param("password", rawPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated());


        Account account = accountRepository.findByEmail(email);
        assertNotNull(account);
        assertNotNull(account.getEmailCheckToken());
        assertNotEquals(account.getPassword(), rawPassword);
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    public void checkEmailTokenWithWrongInput() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token","dkjflkwjdflk")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    public void checkEmailTokenWithWrightInput() throws Exception {
        //given
        Account account = Account.builder()
                .email("email@email.com")
                .password("1q2w3e4r")
                .nickname("bebe")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        //when, then
        mockMvc.perform(get("/check-email-token")
                        .param("token",newAccount.getEmailCheckToken())
                        .param("email",newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attributeExists("nickname","numberOfUser"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(authenticated());
    }
}