package com.wsd.ska.user;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wsd.ska.messages.Message;
import com.wsd.ska.user_requests.UserRequests;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;


    @Column(nullable = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY,
            cascade =  CascadeType.REMOVE,
            mappedBy = "user")
    private PassToken passwordToken;

    @OneToOne(fetch = FetchType.LAZY,
            cascade =  CascadeType.REMOVE,
            mappedBy = "user")
    private RegToken registerToken;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean notBanned; // идет в Service ответственный за аутентификацию (MyUserDetailsService)

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean verified;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private Set<Message> userMessages;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private Set<UserRequests> userRequests;

    @Column(nullable = false, unique = true)
    private String phonenumber;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    private String post;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User(String username, String password, String email, String phone_number, String full_name, String post) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.notBanned = true;
        this.active = false;
        this.phonenumber = phone_number;
        this.fullname = full_name;
        this.post = post;
    }
}