package com.guham.guham.settings;

import com.guham.guham.WithAccount;
import com.guham.guham.account.AccountRepository;
import com.guham.guham.account.AccountService;
import com.guham.guham.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.guham.guham.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    public void afterEach(){
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정 폼")
    @WithAccount("bebe")
    public void updateProfileForm() throws Exception {
        mockMvc.perform(get(SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "profile"));
    }

    @Test
    @DisplayName("프로필 수정 - 입력값 정상")
    @WithAccount("bebe")
    public void updateProfile() throws Exception {
        String changeBio = "짧은 소개";
        mockMvc.perform(post(SETTINGS_PROFILE_URL)
                        .param("bio", changeBio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account bebe = accountRepository.findByNickname("bebe");
        assertEquals(changeBio, bebe.getBio());
    }

    @Test
    @DisplayName("프로필 수정 - 입력값 오류")
    @WithAccount("bebe")
    public void updateProfileWithWrongInput() throws Exception {
        String changeBio = "too long too long too long too long " +
                "too long too long too long too long too long" +
                " too long too long too long";

        mockMvc.perform(post(SETTINGS_PROFILE_URL)
                        .param("bio", changeBio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account","profile"));

        Account bebe = accountRepository.findByNickname("bebe");
        assertNull(bebe.getBio());
    }

    @Test
    @DisplayName("패스워드 수정 폼")
    @WithAccount("bebe")
    public void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "passwordForm"));
    }

    @Test
    @DisplayName("패스워드 수정 - 입력값 정상")
    @WithAccount("bebe")
    public void updatePassword() throws Exception {
        String newPassword = "1234qwer";
        mockMvc.perform(post(SETTINGS_PASSWORD_URL)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm",newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account bebe = accountRepository.findByNickname("bebe");
        assertTrue(passwordEncoder.matches(newPassword, bebe.getPassword()));
    }

    @Test
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @WithAccount("bebe")
    public void updatePasswordWithWrongInput() throws Exception {
        String newPassword = "1q2w3e4r";
        mockMvc.perform(post(SETTINGS_PASSWORD_URL)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm",newPassword+"123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }
}