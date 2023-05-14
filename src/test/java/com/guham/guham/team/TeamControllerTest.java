package com.guham.guham.team;

import com.guham.guham.WithAccount;
import com.guham.guham.account.AccountRepository;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void afterEach() {
        teamRepository.deleteAll();
    }

    @Test
    @DisplayName("팀 생성 화면 테스트")
    @WithAccount("bebe")
    public void newTeamForm() throws Exception {
        mockMvc.perform(get("/new-team"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/form"))
                .andExpect(model().attributeExists("teamForm", "account"));
    }

    @Test
    @DisplayName("팀 개설 - 입력값 정상")
    @WithAccount("bebe")
    public void test() throws Exception {

        mockMvc.perform(post("/new-team")
                        .param("path", "test-path")
                        .param("title", "study title")
                        .param("shortDescription", "short description of a study")
                        .param("fullDescription", "full description of a study")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/test-path"));

        Team byPath = teamRepository.findByPath("test-path");
        assertNotNull(byPath);

        Account byNickname = accountRepository.findByNickname("bebe");
        assertTrue(byPath.getManagers().contains(byNickname));
    }

    @Test
    @DisplayName("팀 개설 - 입력값 오류")
    @WithAccount("bebe")
    public void newTeamWithWrongInput() throws Exception {

        mockMvc.perform(post("/new-team")
                        .param("path", "wrong path")
                        .param("title", "team title")
                        .param("shortDescription", "short description of a team")
                        .param("fullDescription", "full description of a team")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("team/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("teamForm", "account"));
    }

    @Test
    @DisplayName("팀 개설 - 입력값 중복")
    @WithAccount("bebe")
    public void newTeamWithDuplicationInput() throws Exception {
        teamRepository.save(Team.builder()
                .path("test-path")
                .title("team title")
                .shortDescription("short Description")
                .fullDescription("full Description")
                .build());

        mockMvc.perform(post("/new-team")
                        .param("path", "test-path")
                        .param("title", "new title")
                        .param("shortDescription", "short description of a study")
                        .param("fullDescription", "full description of a study")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("team/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("teamForm", "account"));
    }
}