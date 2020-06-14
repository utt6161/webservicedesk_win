package com.wsd.ska.user_requests;

import com.wsd.ska.messages.Message;
import com.wsd.ska.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "complex_user_requests")
//public class ComplexUserRequests{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(nullable = false)
//    private String description;
//
//    @Column(nullable = false)
//    private int status;
//
//    @OneToMany(mappedBy = "complex_request", fetch = FetchType.LAZY,
//            cascade = CascadeType.ALL)
//    private Set<Message> requestMessages;
//
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "user_requests_id", referencedColumnName  = "id")
//    private UserRequests userRequests;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
//    private User user;
//
//    public ComplexUserRequests(String title, String description, UserRequests userRequest, int status, User user){
//        this.title = title;
//        this.userRequests = userRequest;
//        this.description = description;
//        this.status = status;
//        this.user = user;
//    }
//
//}
