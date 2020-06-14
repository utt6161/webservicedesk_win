package com.wsd.ska.exceptions;

import com.wsd.ska.user.UserService;
import com.wsd.ska.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        if(exception instanceof BadCredentialsException){
            response.sendRedirect("/?cr_error=true");
        } else if(exception instanceof UserIsNotValidException) {
            response.sendRedirect("/?val_error=true");
        } else if(exception instanceof UserIsInactiveException){
            response.sendRedirect("/?act_error=true");
        } else {
            response.sendRedirect("/?error=true");
        }

    }

}
