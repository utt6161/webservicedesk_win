package com.wsd.ska.user;

import com.wsd.ska.Email.EmailService;
import com.wsd.ska.site.Site;
import com.wsd.ska.site.SiteRepository;
import com.wsd.ska.utils.Auth_tabs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class AuthController {

    Auth_tabs register_tab = new Auth_tabs("active show", "true", "", "false");
    Auth_tabs login_tab = new Auth_tabs("", "false", "active show", "true");
    String currentHost = "https://localhost:8443/";

//    register_tab = Tab('active show', 'true', '', 'false')
//    login_tab = Tab('', 'false', 'active show', 'true')

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private RegTokenRepository regTokenRepository;

    @Autowired
    private PassTokenRepository passTokenRepository;

    @Autowired
    private SessionRegistry sessions;

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public ModelAndView resetPage(HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("reset");
        EmailDTO email = new EmailDTO();
        modelAndView.addObject("email",email);
        return modelAndView;
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ModelAndView resetPagePOST(@Valid EmailDTO email, HttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("reset");
            modelAndView.addObject("email",email);
            return modelAndView;
        }
        User userExists = userService.findUserByEmail(email.getEmail());
        if(userExists==null){
            ModelAndView modelAndView = new ModelAndView("reset");
            modelAndView.addObject("message","Такого пользователя не существует");
            modelAndView.addObject("email",email);
            return modelAndView;
        } else if (!userExists.isVerified()){
            ModelAndView modelAndView = new ModelAndView("reset");
            modelAndView.addObject("message","Пользователь не подтвердил почту, восстановление пароля невозможно");
            modelAndView.addObject("email",email);
            return modelAndView;
        } else if (!userExists.isActive()){
            ModelAndView modelAndView = new ModelAndView("reset");
            modelAndView.addObject("message","Пользователь не активирован, восстановление пароля невозможно");
            modelAndView.addObject("email",email);
            return modelAndView;
        }

        PassToken isUserTokenExists = passTokenRepository.findByUserUsername(userExists.getUsername());
        if(isUserTokenExists!=null){
            System.out.println("deleting password token for user: " + userExists.getUsername() + " with token: \n" + isUserTokenExists.getToken()
            + "\nfor reason: duplicate user tokens");
            passTokenRepository.delete(isUserTokenExists);
        }
        PassToken passToken = new PassToken(UUID.randomUUID().toString(),userExists, LocalDateTime.now().plus(2, ChronoUnit.MINUTES));
        userExists.setPasswordToken(passToken);
        passTokenRepository.save(passToken);
        userRepository.save(userExists);



        emailService.sendSimpleMessage(email.getEmail(),"Восстановление пароля","Ссылка станет нерабочей через 2 минуты: " + "<a href='" +
                currentHost + "reset/"+ passToken.getToken()
                + "'>Восстановить пароль</a>");
        redirectAttributes.addFlashAttribute("successMessage","Ожидайте сообщения о смене пароля на вашей почте");
        redirectAttributes.addFlashAttribute("user",new UserDTO());
        redirectAttributes.addFlashAttribute("tab", login_tab);
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/reset/{token}",method = RequestMethod.GET)
    public ModelAndView checkToken(@PathVariable String token, RedirectAttributes redirectAttributes) {
//        List<PassToken> passtoken = passTokenRepository.findByToken(token);
//        if(!passtoken.isEmpty()){
//            for (PassToken passToken : passtoken){
//                System.out.println(passToken.getToken());
//            }
//        }
        PassToken passtoken = passTokenRepository.findByToken(token);
        if(passtoken != null && passtoken.getExpireDate().isBefore(LocalDateTime.now())){
            System.out.println("deleting password token:\n" + passtoken.getToken()
                    + "\nfor reason: expired");
            passTokenRepository.delete(passtoken);
            return new ModelAndView("404");
        }
        User user = userRepository.findByPasswordTokenToken(token);
        if(user == null){
//            redirectAttributes.addFlashAttribute("errorMessage","Данная ссылка либо просрочена, либо её несуществует");
//            redirectAttributes.addFlashAttribute("user",new UserDTO());
//            redirectAttributes.addFlashAttribute("tab", login_tab);
            return new ModelAndView("404");
        }

        ModelAndView modelAndView = new ModelAndView("proceed_reset");
        PasswordDTO password = new PasswordDTO();
        modelAndView.addObject("token",token);
        modelAndView.addObject("password",password);
        return modelAndView;
    }

    @RequestMapping(value = "/reset/{token}",method = RequestMethod.POST)
    public ModelAndView checkToken(@PathVariable String token, @Valid PasswordDTO password, RedirectAttributes redirectAttributes,
                                   BindingResult bindingResult){
        PassToken passtoken = passTokenRepository.findByToken(token);
        if(passtoken != null && passtoken.getExpireDate().isBefore(LocalDateTime.now())){
            System.out.println("deleting password token:\n" + passtoken.getToken()
                    + "\nfor reason: expired");
            passTokenRepository.delete(passtoken);
            return new ModelAndView("404");
        }

        User user = userRepository.findByPasswordTokenToken(token);
        if(user == null){
//            redirectAttributes.addFlashAttribute("errorMessage","Данная ссылка либо просрочена, либо её несуществует");
//            redirectAttributes.addFlashAttribute("user",new UserDTO());
//            redirectAttributes.addFlashAttribute("tab", login_tab);
            return new ModelAndView("404");
        }
        if(bindingResult.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("proceed_reset");
            modelAndView.addObject("token",token);
            modelAndView.addObject("password",password);
            return modelAndView;
        }
        List<Object> principals = sessions.getAllPrincipals();
        for (Object principal: principals) {
            if(((UserDetails)principal).getUsername().equals(user.getUsername())) {
                System.out.println("found user for forced logout: " + ((UserDetails) principal).getUsername());
                List<SessionInformation> sessionInformations = sessions.getAllSessions(principal, true);
                for (SessionInformation sessionInformation : sessionInformations) {
                    sessionInformation.expireNow();
                }
                break;
            }
        }
        passTokenRepository.delete(passtoken);
        user = userService.setNewPassword(user,password.getPassword());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage","Пароль был обновлен");
        redirectAttributes.addFlashAttribute("tab", login_tab);
        redirectAttributes.addFlashAttribute("user",new UserDTO());
        return new ModelAndView("redirect:/");
    }



    @RequestMapping(value = "/edit_site_data", method = RequestMethod.GET)
    public ModelAndView setSiteDataPage(HttpServletRequest request, Principal principal){
        if(!isAdmin(principal)){
            return new ModelAndView("404");
        }
        return new ModelAndView("edit_site_data");
    }

    @RequestMapping(value = "/edit_site_data", method = RequestMethod.POST)
    public ModelAndView setSiteData(HttpServletRequest request, Principal principal){
        if(!isAdmin(principal)){
            return new ModelAndView("404");
        }
        Site current_data = siteRepository.findFirstByOrderByIdAsc();
        current_data.setCompany_name(request.getParameter("company"));
        current_data.setEmail(request.getParameter("email"));
        current_data.setPhone_number(request.getParameter("phone"));
        siteRepository.save(current_data);
        return new ModelAndView("redirect:/requests");
    }



    @RequestMapping(value="/", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            /* поведение если юзер уже зашел */
            return new ModelAndView("redirect:/requests");
        }
        Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("auth");
        if (map == null) {
            UserDTO user = new UserDTO();
            modelAndView.addObject("user", user);
            modelAndView.addObject("tab", login_tab);
            return modelAndView;
        } else {
            return modelAndView;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView loginFallback(RedirectAttributes redirectAttributes){
        UserDTO user = new UserDTO();
        ModelAndView modelAndView = new ModelAndView();
        redirectAttributes.addFlashAttribute("tab", login_tab);
        redirectAttributes.addFlashAttribute("user", user);
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registrationFallback( RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("tab", register_tab);
        UserDTO user = new UserDTO();
        redirectAttributes.addFlashAttribute("user", user);
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public RedirectView createNewUser(@Valid UserDTO user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            /* редирект если авторизован */
            return new RedirectView("/requests");
        }
        User userExists = userService.findUserByUserName(user.getUsername());
        User emailExists = userService.findUserByEmail(user.getEmail());
        User phoneExists = userService.findUserByPhone(user.getPhone_number());
        redirectAttributes.addFlashAttribute("tab", register_tab);
        if (userExists != null) {
            bindingResult
                    .rejectValue("username", "error.user",
                            "Пользователь с таким именем уже существует");

            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("tab", register_tab);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            return new RedirectView("/");

        } else if (emailExists!=null) {
            bindingResult
                    .rejectValue("email", "error.email",
                            "Пользователь с такой почтой уже существует");

            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("tab", register_tab);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            return new RedirectView("/");
        }else if (phoneExists!=null){
            bindingResult
                    .rejectValue("phone_number", "error.phone_number",
                            "Пользователь с таким номером уже существует");

            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("tab", register_tab);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            return new RedirectView("/");
        } else if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("tab", register_tab);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            return new RedirectView("/");
        } else {
            User new_user = new User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getPhone_number(),
                    StringUtils.capitalize(user.getLast_name().toLowerCase()) + " "
                            + StringUtils.capitalize(user.getFirst_name().toLowerCase()) + " "
                            + StringUtils.capitalize(user.getMiddle_name().toLowerCase()),
                    " "
            );
            new_user.setVerified(false);
            new_user = userService.saveUser(new_user);
            RegToken token = new RegToken(UUID.randomUUID().toString(), new_user, LocalDateTime.now().plus(1, ChronoUnit.DAYS));
            regTokenRepository.save(token);
            new_user.setRegisterToken(token);
            userRepository.save(new_user);
            emailService.sendSimpleMessage(user.getEmail(),"Подтверждение регистрации", "" +
                    "Ссылка для завершения процесса регистрации в Web Service Desk:\n" +
                    "<a href='" + currentHost + "register/" + token.getToken() + "'>Нажмите сюда</a>");
            redirectAttributes.addFlashAttribute("tab", register_tab);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("errorMessage", "Ожидается подтверждение почты, у вас есть один день");
            return new RedirectView("/");
        }
    }

    @RequestMapping(value = "/register/{token}", method = RequestMethod.GET)
    public ModelAndView confirmReg(@PathVariable String token, RedirectAttributes redirectAttributes){
        User user = userRepository.findByRegisterTokenToken(token);
        if(user != null && user.getRegisterToken().getExpireDate().isBefore(LocalDateTime.now())){
            System.out.println("deleting password token:\n" + token
                    + "\nfor reason: expired");
            RegToken regToken = regTokenRepository.findByUserId(user.getId());
            regTokenRepository.delete(regToken);
            System.out.println("attempt to confirm email, token of this user is expired: " + user.getUsername());
            return new ModelAndView("404");
        } else if(user == null){
            System.out.println("attempt to confirm email, user by token is null");
            return new ModelAndView("404");
        }
        user.setVerified(true);
        System.out.println("user: " + user.getUsername() + " was set to verified");
        System.out.println("sending redirect to root page");
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("tab", register_tab);
        redirectAttributes.addFlashAttribute("user", new UserDTO());
        redirectAttributes.addFlashAttribute("successMessage", "Почта подтверждена, ожидайте активации вашего аккаунта");
        return new ModelAndView("redirect:/");

    }

    public boolean isAdmin(Principal principal){
        User user=userRepository.findByUsername(principal.getName());
        if (user!=null){
            Role role = (Role)user.getRoles().toArray()[0];
            if (role.getRole().equals("ADMIN")) {
                System.out.println("user: " + user.getUsername() + " is admin");
                return true;
            }
        }
        System.out.println("user: " + user.getUsername() + " isnt admin");
        return false;
    }
//    @RequestMapping(value="/admin", method = RequestMethod.GET)
//    public ModelAndView admin(){
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("admin");
//        return modelAndView;
//    }

//    @RequestMapping(value="/tickets", method = RequestMethod.GET)
//    public ModelAndView tickets(){
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("user_requests");
//        return modelAndView;
//    }

//    @RequestMapping(value="/admin/home", method = RequestMethod.GET)
//    public ModelAndView home(){
//        ModelAndView modelAndView = new ModelAndView();
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = userService.findUserByUserName(auth.getName());
//        modelAndView.addObject("userName", "Welcome " + user.getUsername() + "/" + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
//        modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
//        modelAndView.setViewName("admin/home");
//        return modelAndView;
//    }


}
