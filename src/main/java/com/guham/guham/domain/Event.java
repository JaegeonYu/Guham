package com.guham.guham.domain;

import com.guham.guham.account.UserAccount;
import com.guham.guham.event.form.EventForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@NamedEntityGraph(name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments"))
public class Event {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Team team;

    @ManyToOne
    private Account createdBy;

    @Builder
    public Event(String title, String description, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime, int limitOfEnrollments) {
        this.title = title;
        this.description = description;
        this.endEnrollmentDateTime = endEnrollmentDateTime;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.limitOfEnrollments = limitOfEnrollments;
    }

    @Column(nullable = false)
    private String title;
    @Lob
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdDateTime;
    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private int limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) return true;
        }
        return false;
    }

    public boolean isAttended(UserAccount userAccount){
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public int numberOfRemainSpots() {
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void create(Account account, Team team) {
        this.createdBy = account;
        this.team = team;
        this.createdDateTime = LocalDateTime.now();
    }

    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void update(EventForm eventForm){
        title = eventForm.getTitle();
        description = eventForm.getDescription();
        eventType = eventForm.getEventType();
        startDateTime = eventForm.getStartDateTime();
        endDateTime = eventForm.getEndDateTime();
        endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
        limitOfEnrollments = eventForm.getLimitOfEnrollments();
    }

    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public void acceptNextWaitingEnrollment() {
        if(this.isAbleToAcceptWaitingEnrollment()){
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if(enrollmentToAccept != null){
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    private Enrollment getTheFirstWaitingEnrollment() {
        for(Enrollment e : this.enrollments)
            if(!e.isAccepted()){
                return e;
            }
        return null;
    }

    public boolean canAccept(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }
}
