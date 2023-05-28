package com.guham.guham.modules.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guham.guham.infra.MockMVCTest;
import com.guham.guham.modules.account.AccountFactory;
import com.guham.guham.modules.account.WithAccount;
import com.guham.guham.modules.account.AccountRepository;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.Zone;
import com.guham.guham.modules.account.form.TagForm;
import com.guham.guham.modules.account.form.ZoneForm;
import com.guham.guham.modules.tag.TagRepository;
import com.guham.guham.modules.zone.ZoneRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMVCTest
class TeamSettingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TeamFactory teamFactory;
    @Autowired
    private AccountFactory accountFactory;

    private Team team;

    private Zone testZone = Zone.builder().city("test")
            .localNameOfCity("testLocal")
            .province("testPro")
            .build();

    @BeforeEach
    public void beforeEach() {
        Account manger = accountRepository.findByNickname("bebe");
        team = teamFactory.createTeam("test-path", manger);
        zoneRepository.save(testZone);
    }

    @AfterEach
    public void afterEach() {
        teamRepository.deleteAll();
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }


    private void addManager(String managerName, Team team) {
        Account account = accountRepository.findByNickname(managerName);
        team.addManager(account);
    }

    @Test
    @DisplayName("팀 설명 수정 화면")
    @WithAccount("bebe") // 접근자 == 매니저
    public void descriptionForm() throws Exception {
        mockMvc.perform(get("/team/"+team.getEncodedPath()+"/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/settings/description"))
                .andExpect(model().attributeExists("team", "account", "teamDescriptionForm"));
    }

    @Test
    @DisplayName("팀 설명 수정 - 입력값 정상")
    @WithAccount("bebe") // 접근자 == 매니저
    public void descriptionSubmit() throws Exception {
        mockMvc.perform(post("/team/test-path/settings/description")
                        .param("shortDescription", "change short")
                        .param("fullDescription", "change full")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getEncodedPath() + "/settings/description"));

        Team byPath = teamRepository.findByPath("test-path");
        assertEquals(byPath.getShortDescription(), "change short");
        assertEquals(byPath.getFullDescription(), "change full");
    }

    @Test
    @DisplayName("팀 배너 수정 화면")
    @WithAccount("bebe") // 접근자 == 매니저
    public void bannerForm() throws Exception {
        mockMvc.perform(get("/team/test-path/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/settings/banner"))
                .andExpect(model().attributeExists("team", "account"));
    }

    @Test
    @DisplayName("팀 배너 수정 - 입력값 정상")
    @WithAccount("bebe") // 접근자 == 매니저
    public void bannerSubmit() throws Exception {

        mockMvc.perform(post("/team/test-path/settings/banner")
                        .param("image", "HTTP URL IMAGE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getEncodedPath() + "/settings/banner"));

        Team byPath = teamRepository.findByPath("test-path");
        assertNotNull(byPath.getImage());
    }

    @Test
    @DisplayName("팀 배너 사용 - 입력값 정상")
    @WithAccount("bebe") // 접근자 == 매니저
    public void bannerEnable() throws Exception {

        mockMvc.perform(post("/team/test-path/settings/banner/enable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getEncodedPath() + "/settings/banner"));

        Team byPath = teamRepository.findByPath("test-path");
        assertTrue(byPath.isUseBanner());
    }

    @Test
    @DisplayName("팀 배너 미사용 - 입력값 정상")
    @WithAccount("bebe") // 접근자 == 매니저
    public void bannerDisable() throws Exception {
        mockMvc.perform(post("/team/test-path/settings/banner/disable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getEncodedPath() + "/settings/banner"));

        Team byPath = teamRepository.findByPath("test-path");
        assertFalse(byPath.isUseBanner());
    }

    @Test
    @DisplayName("팀 태그 수정 폼")
    @WithAccount("bebe")
    public void updateTagsForm() throws Exception {
        mockMvc.perform(get("/team/test-path/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/settings/tags"))
                .andExpect(model().attributeExists("account", "whitelist", "tags", "team"));
    }

    @Test
    @DisplayName("팀 태그 추가 - 입력값 정상")
    @WithAccount("bebe")
    public void updateTags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/team/test-path/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        Team team = teamRepository.findTeamWithTagsByPath("test-path");
        assertTrue(team.getTags().contains(newTag));
    }

    @Test
    @DisplayName("팀 태그 제거 - 입력값 정상")
    @WithAccount("bebe")
    public void removeTags() throws Exception {
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        Team team = teamRepository.findTeamWithTagsByPath("test-path");
        teamService.addTag(team, newTag);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");


        mockMvc.perform(post("/team/test-path/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(team.getTags().contains(newTag));
    }

    @Test
    @DisplayName("팀 지역정보 수정 폼")
    @WithAccount("bebe")
    public void updateZonesForm() throws Exception {
        mockMvc.perform(get("/team/test-path/settings/zones"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/settings/zones"))
                .andExpect(model().attributeExists("account", "whitelist", "zones", "team"));
    }

    @Test
    @DisplayName("팀 지역정보 추가 - 입력값 정상")
    @WithAccount("bebe")
    public void addZones() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/team/test-path/settings/zones/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        Team team = teamRepository.findTeamWithZonesByPath("test-path");
        assertTrue(team.getZones().contains(zone));
    }

    @Test
    @DisplayName("팀 지역정보 제거 - 입력값 정상")
    @WithAccount("bebe")
    public void removeZones() throws Exception {

        //Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        Team team = teamRepository.findTeamWithTagsByPath("test-path");
        teamService.addZone(team, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/team/test-path/settings/zones/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(team.getZones().contains(zone));
    }

    @Test
    @DisplayName("팀 정보 수정 폼")
    @WithAccount("bebe")
    public void updateTeamForm() throws Exception {
        mockMvc.perform(get("/team/test-path/settings/team"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/settings/team"))
                .andExpect(model().attributeExists("account", "team"));
    }

    @Test
    @DisplayName("팀 공개 - 입력값 정상")
    @WithAccount("bebe")
    public void publishTeam() throws Exception {
        mockMvc.perform(post("/team/test-path/settings/team/publish")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/test-path/settings/team"))
                .andExpect(flash().attributeExists("message"));

        Team team = teamRepository.findTeamWithManagersByPath("test-path");
        assertTrue(team.isPublished());
    }

    @Test
    @DisplayName("팀 종료 - 입력값 정상")
    @WithAccount("bebe")
    public void closeTeam() throws Exception {
        Team team = teamRepository.findTeamWithManagersByPath("test-path");
        teamService.publish(team);

        mockMvc.perform(post("/team/test-path/settings/team/close")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/test-path/settings/team"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(team.isClosed());
    }

    @Test
    @DisplayName("팀 모집 시작 - 입력값 정상")
    @WithAccount("bebe")
    public void recruitStartTeam() throws Exception {
        Team team = teamRepository.findTeamWithManagersByPath("test-path");
        teamService.publish(team);

        mockMvc.perform(post("/team/test-path/settings/recruit/start")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/test-path/settings/team"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(team.isRecruiting());
    }

    @Test
    @DisplayName("팀 모집 시작 -상태변경 시간 오류 - 1시간 이내")
    @WithAccount("bebe")
    public void recruitStartTeamWithChangeStatusInOneHour() throws Exception {
        Team team = teamRepository.findTeamWithManagersByPath("test-path");
        teamService.publish(team);
        teamService.startRecruit(team);

        mockMvc.perform(post("/team/test-path/settings/recruit/start")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/test-path/settings/team"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(team.isRecruiting());
    }

//    @Test TODO MockMVC 요청 시간 변경하는법 알아보기
//    @DisplayName("팀 모집 종료 - 입력값 정상")
//    @WithAccount("bebe")
//    public void recruitStopTeam() throws Exception {
//        Team team = teamRepository.findTeamWithManagersByPath("test-path");
//        teamService.publish(team);
//        teamService.startRecruit(team);
//        team.setRecruitingUpdatedDateTime(LocalDateTime.now().minusHours(1));
//
//        mockMvc.perform(post("/team/test-path/settings/recruit/stop")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/team/test-path/settings/team"))
//                .andExpect(flash().attributeExists("message"));
//
//        assertFalse(team.isRecruiting());
//    }
}