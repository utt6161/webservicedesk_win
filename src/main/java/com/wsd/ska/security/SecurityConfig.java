package com.wsd.ska.security;

import com.wsd.ska.exceptions.CustomAuthenticationFailureHandler;
import com.wsd.ska.handlers.CustomAccessDeniedHandler;
import com.wsd.ska.providers.CustomAuthenticationProvider;
import com.wsd.ska.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http


                .requiresChannel()
                .anyRequest()
                .requiresSecure()
                .and()

                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/reset/**").permitAll()
                .antMatchers("/register/**").permitAll()
//                .antMatchers("/404").permitAll()
                .antMatchers("/requests/**").authenticated()
                .antMatchers("/app/send**").authenticated()
                .antMatchers("/complex/**").hasAuthority("ADMIN")
                .antMatchers("/users/**").hasAnyAuthority("ADMIN","EDIT")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/")
//                    .failureUrl("/?error=true")
                    .loginProcessingUrl("/ska_login")
                    .defaultSuccessUrl("/requests")
                    .usernameParameter("user_name")
                    .passwordParameter("pass_word")
                    .failureHandler(customAuthenticationFailureHandler())
//                    (req,res,exp)->{  // Failure handler invoked after authentication failure
//                        String errMsg="";
//                        if(userService.findUserByUserName(req.getParameter("user_name")) == null){
//                            throw new UsernameNotFoundException("User not found");
//                        }
//                        else if(!userService.findUserByUserName(req.getParameter("user_name")).isActive()){
//                            System.out.println("user is inactive");
//                            errMsg="Ожидайте активации вашего аккаунта";
//                            req.getSession().setAttribute("acitvation_message", errMsg);
//                            res.sendRedirect("/");
//                        }
//                        else if(exp.getClass().isAssignableFrom(BadCredentialsException.class)){
//                            System.out.println("BadCredentialsException");
//                            errMsg="Имя пользователя и/или пароль неверны, повторите попытку";
//                            req.getSession().setAttribute("message", errMsg);
//                            res.sendRedirect("/");
//                        }
//                        else{
//                            System.out.println("User is disabled");
//                            errMsg="Неизвестная ошибка: "+exp.getMessage();
//                            req.getSession().setAttribute("message", errMsg);
//                            res.sendRedirect("/");
//                        }
//                        System.out.println("ЗАЕБАЛ");
//                        res.sendRedirect("/");
//                    })
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/")
                .and()
                    .exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler())
                .and()
                    .sessionManagement()
                    .maximumSessions(-1)
                    .sessionRegistry(sessionRegistry())
                    .expiredUrl("/");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }

    @Bean
    public CustomAuthenticationProvider authenticationProvider(){
        return new CustomAuthenticationProvider();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}
