package com.guham.guham.modules.main;

import com.guham.guham.infra.AbstractContainerTest;
import com.guham.guham.modules.account.AccountRepository;
import com.guham.guham.modules.account.AccountService;
import com.guham.guham.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest extends AbstractContainerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    public void beforeEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("biobebe");
        signUpForm.setEmail("email@email.com");
        signUpForm.setPassword("1q2w3e4r");
        accountService.signUp(signUpForm);
    }

    @AfterEach
    public void afterEach(){
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 요청 성공 - 이메일")
    public void loginWithEmail() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "email@email.com")
                        .param("password", "1q2w3e4r")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withAuthenticationName("biobebe"));
    }

    @Test
    @DisplayName("로그인 요청 성공 - 닉네임")
    public void loginWithNickname() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "biobebe")
                        .param("password", "1q2w3e4r")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withAuthenticationName("biobebe"));
    }

    @Test
    @DisplayName("로그인 요청 실패")
    public void loginFail() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "1111111")
                        .param("password", "1q2w3e4r")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃")
    public void logout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}