package com.guham.guham.modules.event;

import com.guham.guham.infra.MockMVCTest;
import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.account.AccountFactory;
import com.guham.guham.modules.account.AccountRepository;
import com.guham.guham.modules.account.WithAccount;
import com.guham.guham.modules.team.Team;
import com.guham.guham.modules.team.TeamFactory;
import com.guham.guham.modules.team.TeamRepository;
import com.guham.guham.modules.team.TeamService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.guham.guham.modules.event.EventType.CONFIRMATIVE;
import static com.guham.guham.modules.event.EventType.FCFS;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@MockMVCTest
class EventControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    EventService eventService;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private AccountFactory accountFactory;
    @Autowired
    private TeamFactory teamFactory;

    @AfterEach
    public void afterEach() {
        teamRepository.deleteAll();
        accountRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("선착순 모임 참가 신청 - 자동수락")
    @WithAccount("bebe")
    public void newEnrollment_FCFS_event_accepted() throws Exception {
        Account managerAccount = accountFactory.createAccount("manager");
        Team team = teamFactory.createTeam("test-path", managerAccount);
        Event event = createEvent("test-event", FCFS,2, team, managerAccount);

        mockMvc.perform(post("/team/" + team.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getPath() + "/events/" + event.getId()));

        Account bebe = accountRepository.findByNickname("bebe");
        assertTrue(enrollmentRepository.findByEventAndAccount(event, bebe).isAccepted());
    }

    @Test
    @DisplayName("선착순 모임 참가 신청 - 대기")
    @WithAccount("bebe")
    public void newEnrollment_FCFS_event_not_accepted() throws Exception {
        Account managerAccount = accountFactory.createAccount("manager");
        Team team = teamFactory.createTeam("test-path", managerAccount);
        Event event = createEvent("test-event", FCFS,2, team, managerAccount);

        Account one = accountFactory.createAccount("One");
        Account two = accountFactory.createAccount("Two");
        eventService.newEnrollment(event, one);
        eventService.newEnrollment(event, two);

        mockMvc.perform(post("/team/" + team.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getPath() + "/events/" + event.getId()));

        Account bebe = accountRepository.findByNickname("bebe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, bebe).isAccepted());
    }

    @Test
    @DisplayName("선착순 모임 참가 확정자가 참가 취소 시 첫번째 대기자 자동 신청")
    @WithAccount("bebe")
    public void acceptor_cancelEnrollment_FCFS_event() throws Exception {
        Account managerAccount = accountFactory.createAccount("manager");
        Team team = teamFactory.createTeam("test-path", managerAccount);
        Event event = createEvent("test-event", FCFS,2, team, managerAccount);

        Account one = accountFactory.createAccount("One");
        Account two = accountFactory.createAccount("Two");
        Account bebe = accountRepository.findByNickname("bebe");
        eventService.newEnrollment(event, one);
        eventService.newEnrollment(event, bebe);
        eventService.newEnrollment(event, two);


        mockMvc.perform(post("/team/" + team.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event, one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, two).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, bebe));
    }

    @Test
    @DisplayName("선착순 모임 참가 대기자 참가 취소 시, 확정자 변경 없음")
    @WithAccount("bebe")
    public void not_acceptor_cancelEnrollment_FCFS_event() throws Exception {
        Account managerAccount = accountFactory.createAccount("manager");
        Team team = teamFactory.createTeam("test-path", managerAccount);
        Event event = createEvent("test-event", FCFS,2, team, managerAccount);

        Account one = accountFactory.createAccount("One");
        Account two = accountFactory.createAccount("Two");
        Account bebe = accountRepository.findByNickname("bebe");
        eventService.newEnrollment(event, one);
        eventService.newEnrollment(event, two);
        eventService.newEnrollment(event, bebe);

        assertTrue(enrollmentRepository.findByEventAndAccount(event, one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, two).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event, bebe).isAccepted());

        mockMvc.perform(post("/team/" + team.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event, one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, two).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, bebe));
    }


    @Test
    @DisplayName("관리자 확인 모임 참가 신청 - 대기")
    @WithAccount("bebe")
    public void newEnrollment_CONFIMATIVE_event_not_accepted() throws Exception {
        Account managerAccount = accountFactory.createAccount("manager");
        Team team = teamFactory.createTeam("test-path", managerAccount);
        Event event = createEvent("test-event", CONFIRMATIVE,2, team, managerAccount);



        mockMvc.perform(post("/team/" + team.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team/" + team.getPath() + "/events/" + event.getId()));

        Account bebe = accountRepository.findByNickname("bebe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, bebe).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Team team, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, team, account);

    }


}