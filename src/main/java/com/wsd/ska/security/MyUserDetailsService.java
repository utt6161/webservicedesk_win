package com.wsd.ska.security;

import com.wsd.ska.exceptions.PasswordMismatchException;
import com.wsd.ska.exceptions.UserIsInactiveException;
import com.wsd.ska.exceptions.UserIsNotValidException;
import com.wsd.ska.user.Role;
import com.wsd.ska.user.User;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private Request request;

    @Autowired
    private com.wsd.ska.user.UserService UserService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException{

        User user = UserService.findUserByUserName(userName);
        if(user == null){

            System.out.println("User: " + userName + " not found");

            throw new UsernameNotFoundException("User not found");
        }

        System.out.println("User: " + userName + " is here");

        List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
        return buildUserForAuthentication(user, authorities);
    }

    private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
        for (Role role : userRoles) {
            roles.add(new SimpleGrantedAuthority(role.getRole()));
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
        return grantedAuthorities;
    }

    public void userIsVerified(String username) throws UserIsNotValidException {
        User user = UserService.findUserByUserName(username);
        if(!user.isVerified()){

            System.out.println("User: " + username + " isnt verified");

            throw new UserIsNotValidException("User isnt verified");
        }

        System.out.println("User: " + username + " is verified");
    }

    public void userIsActive(String userName) throws UserIsInactiveException{
        User user = UserService.findUserByUserName(userName);
        if(!user.isActive()){

            System.out.println("User: " + userName + " is inactive");

            throw new UserIsInactiveException("User is inactive");
        }

        System.out.println("User: " + userName + " is active");
    }

    public void passwordMatch(String encrypted_password, String password) throws PasswordMismatchException {
        if(!bCryptPasswordEncoder.matches(password,encrypted_password)){
            System.out.println("Request password: " + encrypted_password);
            System.out.println("Found password: " + bCryptPasswordEncoder.encode(password));
            System.out.println("Users password is wrong");
            throw new PasswordMismatchException("Password mismatch");
        }
        System.out.println("Users password is alright");
    }

    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                user.isNotBanned(), true, true, true, authorities);
    }
}
