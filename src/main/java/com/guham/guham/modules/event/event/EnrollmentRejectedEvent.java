package com.guham.guham.modules.event.event;

import com.guham.guham.modules.event.Enrollment;

public class EnrollmentRejectedEvent extends EnrollmentEvent{
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 거절했습니다.");
    }
}
