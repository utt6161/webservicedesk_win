package com.wsd.ska.messages;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wsd.ska.attachments.Attachments;
import com.wsd.ska.user.User;
//import com.wsd.ska.user_requests.ComplexUserRequests;
import com.wsd.ska.user_requests.RequestsContract;
import com.wsd.ska.user_requests.UserRequests;
import lombok.*;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@Entity
@Table(name = "messages")
public class Message extends AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(nullable = false)
    private String messageText;

    @Column(nullable = false)
    private ZonedDateTime date;

    @PrePersist
    void addTimestamp() {
        date = ZonedDateTime.now(
                ZoneId.of("UTC+3")
        );
    }

    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Attachments> attachments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_requests_id", nullable = true, referencedColumnName = "id")
    @JsonBackReference
    private UserRequests request;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "complex_user_requests_id", nullable = true, referencedColumnName = "id")
//    @JsonBackReference
//    private ComplexUserRequests complex_request;

    public Message(String messageText, User user, UserRequests request, List<Attachments> attachments){
        this.messageText = messageText;
        this.user = user;
        this.request = request;
        this.attachments = attachments;
    }

    public Message(String messageText, User user,  List<Attachments> attachments){
        this.messageText = messageText;
        this.user = user;
//        this.complex_request = complex_request;
        this.attachments = attachments;
    }

    public Message(String messageText, User user, UserRequests request){
        this.messageText = messageText;
        this.user = user;
        this.request = request;
    }

    public Message(String messageText, User user){
        this.messageText = messageText;
        this.user = user;
//        this.complex_request = complex_request;
    }


}