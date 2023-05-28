package com.guham.guham.modules.team.event;

import com.guham.guham.modules.team.Team;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class TeamCreatedEvent{
    private Team team;
    public TeamCreatedEvent(Team newTeam) {
        this.team = newTeam;
    }
}
