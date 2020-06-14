package com.wsd.ska.user_requests;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wsd.ska.messages.Message;
import com.wsd.ska.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_requests")
public class UserRequests{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int status;

    @Column(nullable = false)
    private ZonedDateTime date;

    @PrePersist
    void addTimestamp() {
        date = ZonedDateTime.now(
                ZoneId.of("UTC+5")
        );
    }

//    @OneToOne(mappedBy = "userRequests")
//    private ComplexUserRequests complexUserRequests;

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Message> requestMessages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @JsonBackReference
    private User user;

    public UserRequests(String title, String description, int status, User user){
        this.title = title;
        this.description = description;
        this.status = status;
        this.user = user;
    }

}
