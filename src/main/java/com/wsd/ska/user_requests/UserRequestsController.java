package com.wsd.ska.user_requests;

import com.wsd.ska.attachments.Attachments;
import com.wsd.ska.attachments.AttachmentsRepository;
import com.wsd.ska.messages.Message;
import com.wsd.ska.messages.MessagesRepository;
import com.wsd.ska.messages.RequestMessage;
import com.wsd.ska.user.*;
import com.wsd.ska.utils.UrlMatch;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.dom4j.rule.Mode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class UserRequestsController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionRegistry sessions;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public AttachmentsRepository attachmentsRepository;

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRequestsRepository userRequestsRepository;

    @RequestMapping(value="/requests", method = RequestMethod.GET)
    public ModelAndView getRequests(Principal principal){
        ModelAndView modelAndView = new ModelAndView();
        Sort sort = Sort.by(
          Sort.Order.asc("status"),
          Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(0,7, sort);
        Page<UserRequests> userRequests;
        if(isCurrentUserAdminOrEdit()){
            userRequests = userRequestsRepository.findAll(pageable);
        } else {
            userRequests = userRequestsRepository.
                    findByUser(userRepository.findByUsername(principal.getName()), pageable);
        }
        modelAndView.addObject("requests", userRequests);
        modelAndView.setViewName("user_requests");
        return modelAndView;
    }

    @RequestMapping(value="/requests/{strPage}", method = RequestMethod.GET)
    public ModelAndView getRequestsPageable(@PathVariable String strPage, Principal principal, HttpServletRequest request){
        if(isCurrentUserAdminOrEdit()) {
            if (userRequestsRepository.count() == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        else{
            if (userRequestsRepository.countAllByUserUsername(principal.getName()) == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().removeAttribute("mode");
        if(strPage.length()>9){
            modelAndView.setViewName("redirect:/404");
            return modelAndView;
        }
        int page;
        try {
            page = Integer.parseInt(strPage);
        }
        catch (NumberFormatException e){
            modelAndView.setViewName("redirect:/404");
            return modelAndView;
        }
        // 2147483648

        if(page<1){
            modelAndView.setViewName("redirect:/requests/1");
            return modelAndView;
        }
        Sort sort = Sort.by(
                Sort.Order.asc("status"),
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(page - 1, 7, sort);
        Page<UserRequests> userRequests;
        if(isCurrentUserAdminOrEdit()){
            userRequests = userRequestsRepository.findAll(pageable);
        } else {
            userRequests = userRequestsRepository.
                    findByUser(userRepository.findByUsername(principal.getName()), pageable);
        }
        if(page>userRequests.getTotalPages() && (page>1)){
            return new ModelAndView("redirect:/requests/"+userRequests.getTotalPages());
        }
        modelAndView.addObject("requests", userRequests);
        modelAndView.setViewName("user_requests");
        return modelAndView;
    }

    @RequestMapping(value = "/requests/search", method = RequestMethod.POST)
    public ModelAndView getRequestsSearchQuery(Principal principal, HttpServletRequest request){
        if(isCurrentUserAdminOrEdit()) {
            if (userRequestsRepository.count() == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        else{
            if (userRequestsRepository.countAllByUserUsername(principal.getName()) == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        String searchQuery = request.getParameter("search");
        searchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        System.out.println("on first step of search: encoded query: " + searchQuery);
        request.getSession().setAttribute("mode", request.getParameter("mode"));
        return new ModelAndView("redirect:/requests/"+searchQuery+"/1");
    }

//    @RequestMapping(value = "/requests/search", method = RequestMethod.GET)
//    public ModelAndView getEmptyResults(HttpServletRequest request){
//        System.out.println("Requiring empty set");
//        ModelAndView modelAndView = new ModelAndView("requests_search");
//        return modelAndView;
//    }

    @RequestMapping(value = "/requests/{searchQuery}/{strPage}", method = RequestMethod.GET)
    public ModelAndView getRequestsSearchResults(Principal principal,
                                                 HttpServletRequest request,
                                                 @PathVariable String searchQuery,
                                                 @PathVariable String strPage){
        if(isCurrentUserAdminOrEdit()) {
            if (userRequestsRepository.count() == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        else{
            if (userRequestsRepository.countAllByUserUsername(principal.getName()) == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        searchQuery = URLDecoder.decode(searchQuery, StandardCharsets.UTF_8);
        ModelAndView modelAndView = new ModelAndView();
        if(strPage.length()>9){
            modelAndView.setViewName("redirect:/404");
            return modelAndView;
        }
        int page;
        try {
            page = Integer.parseInt(strPage);
        }
        catch (NumberFormatException e){
            modelAndView.setViewName("redirect:/404");
            return modelAndView;
        }
        // 2147483648

        if(page<1){
            modelAndView.setViewName("redirect:/requests/1");
            return modelAndView;
        }
        Sort sort = Sort.by(
                Sort.Order.asc("status"),
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(page-1, 7, sort);
        String mode = "t";
        Page<UserRequests> userRequests = Page.empty();
        if (isCurrentUserAdminOrEdit()) {
            try {
                mode = request.getSession().getAttribute("mode") instanceof String ? (String)request.getSession().getAttribute("mode") : request.getParameter("mode");
                if(mode == null){
                    return new ModelAndView("redirect:/requests");
                }
                System.out.println("Режим поиска: " + mode);
                if ("t".equals(mode)) {

                    userRequests = userRequestsRepository.findByTitleLikeIgnoreCase('%' + searchQuery + '%', pageable);
                    modelAndView.addObject("requests", userRequests);
                    System.out.println("Поиск по названию заявки: " + searchQuery);

                } else if ("d".equals(mode)) {

                    userRequests = userRequestsRepository.findByDescriptionLikeIgnoreCase('%' + searchQuery + '%', pageable);
                    modelAndView.addObject("requests", userRequests);
                    System.out.println("Поиск по описанию заявки: " + searchQuery);

                } else if ("n".equals(mode)) {

                    userRequests = userRequestsRepository.findByUserUsernameLikeIgnoreCase('%' + searchQuery + '%', pageable);
                    modelAndView.addObject("requests", userRequests);
                    System.out.println("Поиск по имени пользователя заявки: " + searchQuery);

                } else {

                    userRequests = userRequestsRepository.findByStatus(Integer.parseInt(searchQuery), pageable);
                    modelAndView.addObject("requests", userRequests);
                    System.out.println("Поиск по статусу заявки: " + searchQuery);

                }
                System.out.println("Найдено совпадений: " + userRequests.getTotalElements());
                modelAndView.addObject("searchQuery",searchQuery);
                modelAndView.setViewName("requests_search");

            } catch (NumberFormatException e) {

                System.out.println("Был произведен нецелочисленный поиск по полю статус: " + searchQuery);
                modelAndView.addObject("status_error","Статус не является числом, повторите запрос");
                modelAndView.addObject("searchQuery",searchQuery);
                modelAndView.setViewName("empty_search");

            }
        } else {
            try {
                mode = request.getSession().getAttribute("mode") instanceof String ? (String)request.getSession().getAttribute("mode") : request.getParameter("mode");
                if(mode == null){
                    return new ModelAndView("redirect:/requests");
                }
                if ("t".equals(mode)) {

                    userRequests = userRequestsRepository.findByTitleLikeIgnoreCaseAndUserUsername('%' + searchQuery + '%', principal.getName(),  pageable);
                    //System.out.println("Поиск по названию заявки: " + searchQuery);

                } else if ("d".equals(mode)) {

                    userRequests = userRequestsRepository.findByDescriptionLikeIgnoreCaseAndUserUsername('%' + searchQuery + '%', principal.getName(), pageable);
                    //System.out.println("Поиск по описанию заявки: " + searchQuery);

                }
//                else {
//
//                    userRequests = userRequestsRepository.findByStatus(Integer.parseInt(searchQuery), pageable);
//                    //System.out.println("Поиск по статусу заявки: " + searchQuery);
//
//                }
                modelAndView.addObject("searchQuery",searchQuery);
                modelAndView.addObject("requests", userRequests);
                modelAndView.setViewName("requests_search");

            } catch (NumberFormatException e){

                System.out.println("Был произведен нецелочисленный поиск по полю статус: " + searchQuery);
                modelAndView.addObject("searchQuery",searchQuery);
                modelAndView.addObject("status_error","Статус не является числом, повторите запрос");
                modelAndView.setViewName("empty_search");

            }
        }
        if(page>userRequests.getTotalPages() && (page>1)){
            return new ModelAndView("redirect:/requests/"+URLEncoder.encode(searchQuery,StandardCharsets.UTF_8)+"/"+userRequests.getTotalPages());
        }
        modelAndView.addObject("mode", mode);
        return modelAndView;
    }


    @RequestMapping(value = "/requests/add", method = RequestMethod.GET)
    public ModelAndView getRequestAddPage(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("request_add");
        return modelAndView;
    }

    @RequestMapping(value = "/requests/add", method = RequestMethod.POST)
    public ModelAndView setRequestAddPage(@RequestParam(name ="files",required = false) MultipartFile[] files,
                                          @RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          Principal principal, HttpServletRequest request, RedirectAttributes redirectAttributes){
        User user = userRepository.findByUsername(principal.getName());
        String[] mess_tokens = description.split(" ");
        for(int i = 0;i<mess_tokens.length;i++){
            System.out.println(mess_tokens[i]);
            if(mess_tokens[i].matches(UrlMatch.REGEX_COMPILED.toString()) && !mess_tokens[i].matches(UrlMatch.NO_PROTOCOL)){
                mess_tokens[i] = "<a href='"+mess_tokens[i]+"' target='_blank' ref='nofollow noopener'>"+mess_tokens[i]+"</a>";
            }
        }
        String description_with_links = String.join(" ", mess_tokens);
        System.out.println(description_with_links);
        UserRequests userRequest = new UserRequests(title,description_with_links,0, user);
        userRequest = userRequestsRepository.save(userRequest);
        System.out.println(files.length);
        final String PATH = "C:\\Users\\utt61\\Desktop\\projects\\ska\\media\\attach\\requests\\" + userRequest.getId();
        File directory = new File(PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("directory  was created");
            } else {
                System.out.println("directory was not created");
            }
        }
        String random;
        String extension = "";
        String filename;
        String editedFilename;
        String orig_filename;
        String urlpath;
        List<MultipartFile> allFiles = new ArrayList<>(Arrays.asList(files));


        int i = 0;
        try {
            for (MultipartFile file : allFiles) {
                List<Attachments> attachments = new ArrayList<>();
                Attachments attachment = new Attachments();
                JSONObject json = new JSONObject();
                if (file.isEmpty()) {
                    System.out.println("empty file skipped");
                    continue; //next pls
                }
                random = UUID.randomUUID().toString();
                orig_filename = file.getOriginalFilename();
                extension = FilenameUtils.getExtension(orig_filename);
                filename = FilenameUtils.getBaseName(orig_filename);
                editedFilename = filename.length()>25?filename.substring(0,25)+".."+extension:filename+"."+extension;
                MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
                //String mimeType = fileTypeMap.getContentType(orig_filename);
                Tika tika=new Tika();
                String mimeType = tika.detect(FilenameUtils.getName(file.getOriginalFilename()));
                Path path = Paths.get(PATH, random + "." + extension);
                urlpath = "media/attach/requests/" + userRequest.getId() + "/" + random + "." + extension;
                System.out.println(orig_filename + " : " + mimeType);
                Files.write(path, file.getBytes());
                if(mimeType.contains("image")){
                    attachment = new Attachments(editedFilename, urlpath,
                            true, false, false, false, false, false, false, false);
                } else if(mimeType.contains("audio")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, true, false, false, false, false, false, false);
                } else if(mimeType.contains("video")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, true, false, false, false, false, false);
                } else if(mimeType.contains("pdf")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, true, false, false, false, false);
                } else if(mimeType.contains("excel") || mimeType.contains("spreadsheetml.sheet")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, false, true, false, false, false);
                } else if(mimeType.contains("word") || mimeType.contains("wordprocessingml.document")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, false, false, true, false, false);
                } else if(mimeType.contains("plain")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, false, false, false, true, false);
                } else if(mimeType.contains("zip") || mimeType.contains("7z") || mimeType.contains("rar")){
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, false, false, false, false, true);
                } else {
                    attachment = new Attachments(editedFilename, urlpath,
                            false, false, false, false, false, false, false, false);
                }

                // what the fuck am i wrote, gotta make a note not to write such trash again, like really, look at this...


                System.out.println("file \"" + orig_filename + "\" was saved");
                JSONArray arr = new JSONArray();
                arr.put(new JSONObject().put("url", attachment.getPaths()));
                arr.put(new JSONObject().put("filename", attachment.getName()));
                arr.put(new JSONObject().put("image", attachment.isImage()));
                arr.put(new JSONObject().put("audio", attachment.isAudio()));
                arr.put(new JSONObject().put("video", attachment.isVideo()));
                arr.put(new JSONObject().put("pdf", attachment.isPdf()));
                arr.put(new JSONObject().put("excel", attachment.isExcel()));
                arr.put(new JSONObject().put("word", attachment.isWord()));
                arr.put(new JSONObject().put("txt", attachment.isTxt()));
                arr.put(new JSONObject().put("archive", attachment.isArchive()));
                i++;
                json.put("file" + i, arr);
                String strmessage = "";
                attachment.setPaths(json.toString());
                System.out.println(json.toString());
                attachments.add(attachment);
                Message message = new Message(strmessage, user, userRequest, attachments);
                message = messagesRepository.save(message);
                for(Attachments att : message.getAttachments()){
                    att.setMessage(message);
                }
                messagesRepository.save(message);
            }


//            for(Attachments att: attachments){
//                att.setMessage(message);
//            }
//            attachmentsRepository.saveAll(attachments);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        ModelAndView modelAndView = new ModelAndView();
        redirectAttributes.addFlashAttribute("successMessage", "Ваша заявка была успешно добавлена, ожидайте обновлений");
        modelAndView.setViewName("redirect:/requests");
        return modelAndView;
    }

    @RequestMapping(value = "/requests/id/{strId}", method = RequestMethod.GET)
    public ModelAndView getRequestView(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        long id;
        try {
            id = Long.parseLong(strId);
        } catch (NumberFormatException e){
            return new ModelAndView("redirect:/404");
        }
        UserRequests userRequest = userRequestsRepository.findById(id);
        if(userRequest == null){
            return new ModelAndView("redirect:/404");
        }
        if(!userRequest.getUser().getUsername().equals(principal.getName()) && !isCurrentUserAdminOrEdit()){
            return new ModelAndView("redirect:/requests");
        }
        List<Message> messages = messagesRepository.findByRequestId(id);
        modelAndView.setViewName("request_view");
        if(userRequest.getStatus()==1){
            modelAndView.setViewName("request_view_disabled");
        }
        User user = userRepository.findByUsername(principal.getName());
        modelAndView.addObject("user", user);
        modelAndView.addObject("request",userRequest);
        modelAndView.addObject("messages",messages);
        Cookie cookie = new Cookie("token",user.getToken());
        response.addCookie(cookie);
        return modelAndView;
    }




    @RequestMapping(value = "/requests/delete/id/{strId}", method = RequestMethod.GET)
    public Object deleteRequest(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        if(!isCurrentUserAdmin()){
            return new ModelAndView("404");
        }
        try {
            long id = Long.parseLong(strId);
            UserRequests userRequest = userRequestsRepository.findById(id);
            userRequestsRepository.delete(userRequest);
        } catch (NumberFormatException e){
            return new ModelAndView("404");
        }
        String referer = request.getHeader("Referer");
        try {
            response.sendRedirect(request.getHeader("Referer"));
        } catch (IOException e ){
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/users/delete/id/{strId}", method = RequestMethod.GET)
    public Object deleteUser(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        if(!isCurrentUserAdmin()){
            return new ModelAndView("404");
        }
        try {
            long id = Long.parseLong(strId);
            User user = userRepository.findById(id);
            if(user == null){
                return new ModelAndView("404");
            }
            if(user.getRoles().toArray()[0].equals("ADMIN")){
                return new ModelAndView("404");
            }
            userRepository.delete(user);
        } catch (NumberFormatException e){
            return new ModelAndView("404");
        }
        String referer = request.getHeader("Referer");
        try {
            response.sendRedirect(request.getHeader("Referer"));
        } catch (IOException e ){
            e.printStackTrace();
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/users/change/id/{strId}", method = RequestMethod.POST)
    public ResponseEntity<?> changeRole(HttpServletRequest request,
                                        @RequestParam("role") String role,
                                        @PathVariable String strId, Principal principal, HttpServletResponse response){
        if(!isCurrentUserAdmin()){
            return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
        }
        try {
            int user_id = Integer.parseInt(strId);
            int role_id = Integer.parseInt(role);
            if((role_id != 2) && (role_id != 3)){
                return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
            User user = userRepository.findById(user_id);
            if(user == null){
                return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
            if(user.getRoles().toArray()[0].equals("ADMIN")){
                return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
            Role objrole = roleRepository.findById(role_id);
            if (objrole.getId() == ((Role)user.getRoles().toArray()[0]).getId()){
                return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
            user.setRoles(new HashSet<Role>(Arrays.asList(objrole)));
            userRepository.save(user);
            List<Object> principals = sessions.getAllPrincipals();
            for (Object sess_principal: principals) {
                if(((UserDetails)sess_principal).getUsername().equals(user.getUsername())) {
                    System.out.println("found user for forced logout: " + ((UserDetails) sess_principal).getUsername());
                    List<SessionInformation> sessionInformations = sessions.getAllSessions(sess_principal, true);
                    for (SessionInformation sessionInformation : sessionInformations) {
                        sessionInformation.expireNow();
                    }
                    break;
                }
            }


        } catch (NumberFormatException e){
            return new ResponseEntity("NOT ALLOWED", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity("OK", HttpStatus.OK);
    }


//    @RequestMapping(value = "/complex", method = RequestMethod.GET)
//    public String getComplex(){
//        return "dummy_page";
//    }

    private boolean isCurrentUserAdmin(){
        String currentUserAuthority = (String)SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return currentUserAuthority.equals("ADMIN");
    }

    private boolean isCurrentUserAdminOrEdit(){
        String currentUserAuthority = (String)SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return currentUserAuthority.equals("ADMIN") || currentUserAuthority.equals("EDIT");
    }

}
