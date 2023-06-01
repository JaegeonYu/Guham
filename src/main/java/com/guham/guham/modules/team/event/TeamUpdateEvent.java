package com.guham.guham.modules.team.event;

import com.guham.guham.modules.team.Team;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TeamUpdateEvent {
    private final Team team;
    private final String message;
}
