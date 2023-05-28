package com.guham.guham.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guham.guham.WithAccount;
import com.guham.guham.modules.account.AccountRepository;
import com.guham.guham.modules.account.AccountService;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.Zone;
import com.guham.guham.modules.settings.form.TagForm;
import com.guham.guham.modules.settings.form.ZoneForm;
import com.guham.guham.modules.tag.TagRepository;
import com.guham.guham.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.guham.guham.modules.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    private Zone testZone = Zone.builder().city("test")
            .localNameOfCity("testLocal")
            .province("testPro")
            .build();
    @BeforeEach
    public void beforeEach(){
        zoneRepository.save(testZone);
    }
    @AfterEach
    public void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정 폼")
    @WithAccount("bebe")
    public void updateProfileForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "profile"));
    }

    @Test
    @DisplayName("프로필 수정 - 입력값 정상")
    @WithAccount("bebe")
    public void updateProfile() throws Exception {
        String changeBio = "짧은 소개";
        mockMvc.perform(post(ROOT+SETTINGS+PROFILE)
                        .param("bio", changeBio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT+SETTINGS+PROFILE))
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

        mockMvc.perform(post(ROOT+SETTINGS+PROFILE)
                        .param("bio", changeBio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PROFILE))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account", "profile"));

        Account bebe = accountRepository.findByNickname("bebe");
        assertNull(bebe.getBio());
    }

    @Test
    @DisplayName("패스워드 수정 폼")
    @WithAccount("bebe")
    public void updatePasswordForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "passwordForm"));
    }

    @Test
    @DisplayName("패스워드 수정 - 입력값 정상")
    @WithAccount("bebe")
    public void updatePassword() throws Exception {
        String newPassword = "1234qwer";
        mockMvc.perform(post(ROOT+SETTINGS+PASSWORD)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT+SETTINGS+PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account bebe = accountRepository.findByNickname("bebe");
        assertTrue(passwordEncoder.matches(newPassword, bebe.getPassword()));
    }

    @Test
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @WithAccount("bebe")
    public void updatePasswordWithWrongInput() throws Exception {
        String newPassword = "1q2w3e4r";
        mockMvc.perform(post(ROOT+SETTINGS+PASSWORD)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm", newPassword + "123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("닉네임 수정 폼")
    @WithAccount("bebe")
    public void updateNicknameForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "nicknameForm"));
    }

    @Test
    @DisplayName("닉네임 수정 - 입력값 정상")
    @WithAccount("bebe")
    public void updateNickname() throws Exception {
        String newNickname = "hello";
        mockMvc.perform(post(ROOT+SETTINGS+ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT+SETTINGS+ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        Account byNewNickname = accountRepository.findByNickname(newNickname);
        assertNotNull(byNewNickname);
    }

    @Test
    @DisplayName("닉네임 수정 - 입력값 에러 - 짧은 닉네임")
    @WithAccount("bebe")
    public void updateNicknameWithWrongInput() throws Exception {
        String newNickname = "12";
        mockMvc.perform(post(ROOT+SETTINGS+ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("닉네임 수정 - 입력값 에러 - 중복 닉네임")
    @WithAccount("bebe")
    public void updateNicknameWithDuplication() throws Exception {
        String usedNickname = "hello";
        accountRepository.save(Account.builder()
                .nickname(usedNickname).build());

        mockMvc.perform(post(ROOT+SETTINGS+ACCOUNT)
                        .param("nickname", usedNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("알림 수정 폼")
    @WithAccount("bebe")
    public void updateNotificationsForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+NOTIFICATIONS))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "notifications"));
    }

    @Test
    @DisplayName("태그 수정 폼")
    @WithAccount("bebe")
    public void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+TAGS))
                .andExpect(model().attributeExists("account", "whitelist", "tags"));
    }

    @Test
    @DisplayName("태그 추가 - 입력값 정상")
    @WithAccount("bebe")
    public void updateTags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT+SETTINGS+TAGS + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("bebe").getTags().contains(newTag));
    }

    @Test
    @DisplayName("태그 제거 - 입력값 정상")
    @WithAccount("bebe")
    public void removeTags() throws Exception {
        Account bebe = accountRepository.findByNickname("bebe");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(bebe.getId(), newTag);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT+SETTINGS+TAGS + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(bebe.getTags().contains(newTag));
    }

    @Test
    @WithAccount("bebe")
    @DisplayName("지역정보 폼")
    public void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ZONES))
                .andExpect(model().attributeExists("account", "whitelist", "zones"));
    }

    @Test
    @DisplayName("지역정보 추가 - 입력값 정상")
    @WithAccount("bebe")
    public void updateZones() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT+SETTINGS+ZONES + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());


        Account bebe = accountRepository.findByNickname("bebe");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(bebe.getZones().contains(zone));
    }

    @Test
    @DisplayName("지역정보 삭제 - 입력값 정상")
    @WithAccount("bebe")
    public void removeZones() throws Exception {
        Account bebe = accountRepository.findByNickname("bebe");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(bebe.getId(), zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT+SETTINGS+ZONES + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(bebe.getZones().contains(zone));
    }

}