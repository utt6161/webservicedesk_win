package com.wsd.ska.init_db;

import com.wsd.ska.messages.Message;
import com.wsd.ska.messages.MessagesRepository;
import com.wsd.ska.site.Site;
import com.wsd.ska.site.SiteRepository;
import com.wsd.ska.user.UserService;
import com.wsd.ska.user.User;
import com.wsd.ska.user_requests.UserRequests;
import com.wsd.ska.user_requests.UserRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class InitUsers implements CommandLineRunner {
    private UserService userService;

    @Autowired
    private UserRequestsRepository userRequestsRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private MessagesRepository messagesRepository;

    public InitUsers(UserService userService) {
        this.userService= userService;
    }

    @Override
    public void run(String... args) throws Exception {

        User admin = new User("admin","15881588","admin@yandex.ru","+79586521452","Жмышенко В. А.","грибокомбинат им. Ленина");
        User user = new User("user","15881588","user@yandex.ru","+79854513547","Пользователь","Обычный пользователь");
        User activeUser = new User("activeUser","15881588","useruser@yandex.ru","+79854614547","Валентина Александровна","ОРГК, секретарь");
        User activeUser2 = new User("utt6161","15881588","utt6161@yandex.ru","+79854644547","Валентина Александровна","ОРГК, секретарь");
        Site site_data = new Site("+79563254514","ЗАО«Путевод»","putevod@yandex.ru");

        siteRepository.save(site_data);
        userService.saveUser(user);
        userService.saveAdmin(admin);
        userService.saveActiveUser(activeUser);
        userService.saveActiveUser(activeUser2);

        List<UserRequests> userRequests = new ArrayList<UserRequests>();

        for(int i = 0; i<15; i++){
            userRequests.add(new UserRequests("Заявка: "+i, UUID.randomUUID().toString(), 0, user));
            userRequests.add(new UserRequests("Заявка: "+i*2, UUID.randomUUID().toString(), 1, user));
        }


        List<Message> messagesList = new ArrayList<Message>();
        for(int i = 0; i<15; i++){
            messagesList.add(new Message("Ничего себе вот это сервис", user, userRequests.get(0)));
            messagesList.add(new Message("да-да-да", admin, userRequests.get(0)));
        }

        userRequestsRepository.saveAll(userRequests);

        UserRequests userRequest = new UserRequests("Заявка: 146", "детали заявки", 0, admin); // заявка от админа
        userRequestsRepository.save(userRequest);

        messagesRepository.saveAll(messagesList);

    }
}
