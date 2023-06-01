package com.guham.guham.modules.team.event;

import com.guham.guham.modules.team.Team;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
@Getter
@RequiredArgsConstructor
public class TeamCreatedEvent{
    private final Team team;
}
