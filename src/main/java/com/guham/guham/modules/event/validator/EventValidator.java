package com.guham.guham.modules.event.validator;

import com.guham.guham.modules.event.Event;
import com.guham.guham.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm event = (EventForm) target;

        if(isNotValidateEndEnrollmentDateTime(event)){
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료 일시를 정확하게 입력하세요.");
        }

        if(isNotValidateEndDateTime(event)){
            errors.rejectValue("endDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요.");
        }

        if(isNotValidateStartDateTime(event)){
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidateStartDateTime(EventForm event) {
        return event.getStartDateTime().isBefore(event.getEndEnrollmentDateTime());
    }

    private boolean isNotValidateEndDateTime(EventForm event) {
        return event.getEndDateTime().isBefore(event.getStartDateTime()) || event.getEndDateTime().isBefore(event.getEndEnrollmentDateTime());
    }

    private boolean isNotValidateEndEnrollmentDateTime(EventForm event) {
        return event.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "확인된 참기 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
