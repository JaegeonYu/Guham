package com.guham.guham.event.form;

import com.guham.guham.domain.Event;
import com.guham.guham.domain.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class EventForm {
    @NotBlank
    @Length(max = 50)
    private String title;
    private String description;
    private EventType eventType = EventType.FCFS;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;
    @Min(2)
    private Integer limitOfEnrollments = 2;

    public Event getEvent(){
        return Event.builder()
                .title(title)
                .description(description)
                .endEnrollmentDateTime(endEnrollmentDateTime)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .limitOfEnrollments(limitOfEnrollments)
                .build();
    }
}
