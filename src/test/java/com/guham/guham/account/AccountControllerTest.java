package com.guham.guham.account;

import com.guham.guham.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원 가입 화면 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
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
                .andExpect(view().name("account/sign-up"));
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
                .andExpect(view().name("redirect:/"));


        Account account = accountRepository.findByEmail(email);
        assertNotNull(account);
        assertNotEquals(account.getPassword(), rawPassword);
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}