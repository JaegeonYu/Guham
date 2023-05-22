package com.guham.guham.event;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Event;
import com.guham.guham.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    public Event createEvent(Event event, Team team, Account account) {
        event.create(account, team);
        return eventRepository.save(event);
    }
}
