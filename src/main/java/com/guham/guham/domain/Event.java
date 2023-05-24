package com.guham.guham.domain;

import com.guham.guham.account.UserAccount;
import com.guham.guham.event.form.EventForm;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
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
    private List<Enrollment> enrollments;

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
}
