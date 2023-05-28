package com.guham.guham.modules.team.event;

import com.guham.guham.modules.team.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@Transactional(readOnly = true)
public class TeamEventListener {
    @EventListener
    public void handleTeamCreatedEvent(TeamCreatedEvent teamCreatedEvent){
        Team team = teamCreatedEvent.getTeam();
        log.info(team.getTitle()+"is created");
    }
}
