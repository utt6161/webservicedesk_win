package com.wsd.ska.providers;

import com.wsd.ska.exceptions.PasswordMismatchException;
import com.wsd.ska.exceptions.UserIsInactiveException;
import com.wsd.ska.exceptions.UserIsNotValidException;
import com.wsd.ska.security.MyUserDetailsService;
import com.wsd.ska.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private MyUserDetailsService userService;

    @Autowired
    private UserRepository userRepository;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails user;
        try {
            user = userService.loadUserByUsername(username);
            userService.passwordMatch(user.getPassword(), password);
            userService.userIsVerified(username);
            userService.userIsActive(username);
        }
        catch (UsernameNotFoundException ex){
            throw new BadCredentialsException("Username not found");
        }
        catch (PasswordMismatchException ex){
            throw new BadCredentialsException("Username not found");
        }
        catch (UserIsInactiveException ex){
            throw ex;
        }
        catch (UserIsNotValidException ex){
            throw ex;
        }


        userRepository.updateTokenByUsername(username, UUID.randomUUID().toString());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    public boolean supports(Class<?> arg0) {
        return true;
    }

}