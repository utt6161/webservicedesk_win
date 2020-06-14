package com.wsd.ska.user;

import com.wsd.ska.attachments.Attachments;
import com.wsd.ska.attachments.AttachmentsRepository;
import com.wsd.ska.messages.Message;
import com.wsd.ska.messages.MessagesRepository;
import com.wsd.ska.user_requests.UserRequests;
import com.wsd.ska.user_requests.UserRequestsRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class UserDataController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public AttachmentsRepository attachmentsRepository;

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private UserRequestsRepository userRequestsRepository;

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public ModelAndView getRequests(Principal principal){
        ModelAndView modelAndView = new ModelAndView();
        Pageable pageable = PageRequest.of(0,7, Sort.by("active").ascending());
        Page<User> users;
        if(isCurrentUserAdminOrEdit()){
            users = userRepository.findAll(pageable);
        } else {
            return new ModelAndView("redirect:/requests");
        }
        modelAndView.addObject("users", users);
        modelAndView.setViewName("users_list");
        return modelAndView;
    }

    @RequestMapping(value="/users/id/{strId}/activate", method = RequestMethod.GET)
    public ModelAndView activateUser(Principal principal, HttpServletRequest request, HttpServletResponse response, @PathVariable String strId){ ;
        if(isCurrentUserAdminOrEdit()) {
            if (userRepository.count() == 0) {
                return new ModelAndView("redirect:/users");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
        }

        User user = userRepository.findById(Long.parseLong(strId));
        if(user == null){
            try {
                response.sendRedirect(request.getHeader("Referer"));
                return null;
            }
            catch(Exception e){

            }
        } else {
            user.setActive(true);
            userRepository.save(user);
        }

        for(Role i : user.getRoles()){
            System.out.println(i.getRole());
        }
        try {
            response.sendRedirect(request.getHeader("Referer"));
            return null;
        }
        catch(Exception e){

        }
        return new ModelAndView("redirect:/users");
    }

    @RequestMapping(value="/users/id/{strId}/deactivate", method = RequestMethod.GET)
    public ModelAndView deactivateUser(Principal principal, HttpServletRequest request, HttpServletResponse response, @PathVariable String strId){
        if(isCurrentUserAdminOrEdit()) {
            if (userRepository.count() == 0) {
                return new ModelAndView("redirect:/users");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
        }

        User user = userRepository.findById(Long.parseLong(strId));
        if(user == null){
            try {
                response.sendRedirect(request.getHeader("Referer"));
                return null;
            }
            catch(Exception e){

            }
        } else {
            if(!user.getRoles().contains("ADMIN")) {
                user.setActive(false);
                userRepository.save(user);
            }
            else {
                try {
                    response.sendRedirect(request.getHeader("Referer"));
                    return null;
                }
                catch(Exception e){

                }
            }
        }

        try {
            response.sendRedirect(request.getHeader("Referer"));
            return null;
        }
        catch(Exception e){

        }
        return new ModelAndView("redirect:/users");
    }

    @RequestMapping(value="/users/{strPage}", method = RequestMethod.GET)
    public ModelAndView getRequestsPageable(@PathVariable String strPage, Principal principal, HttpServletRequest request){
        if(isCurrentUserAdminOrEdit()) {
            if (userRepository.count() == 0) {
                return new ModelAndView("redirect:/users");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
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
            modelAndView.setViewName("redirect:/users/1");
            return modelAndView;
        }
        Pageable pageable = PageRequest.of(page - 1, 7, Sort.by("active").ascending());
        Page<User> users = userRepository.findAll(pageable);

        if(page>users.getTotalPages() && (page>1)){
            return new ModelAndView("redirect:/users/"+users.getTotalPages());
        }
        modelAndView.addObject("users", users);
        modelAndView.setViewName("users_list");
        return modelAndView;
    }

    @RequestMapping(value = "/users/search", method = RequestMethod.POST)
    public ModelAndView getRequestsSearchQuery(Principal principal, HttpServletRequest request){
        if(isCurrentUserAdminOrEdit()) {
            if (userRepository.count() == 0) {
                return new ModelAndView("redirect:/requests");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
        }
        String searchQuery = request.getParameter("search");
        searchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        System.out.println("on first step of search: encoded query: " + searchQuery);
        request.getSession().setAttribute("usr_mode", request.getParameter("mode"));
        return new ModelAndView("redirect:/users/"+searchQuery+"/1");
    }

//    @RequestMapping(value = "/requests/search", method = RequestMethod.GET)
//    public ModelAndView getEmptyResults(HttpServletRequest request){
//        System.out.println("Requiring empty set");
//        ModelAndView modelAndView = new ModelAndView("requests_search");
//        return modelAndView;
//    }

    @RequestMapping(value = "/users/{searchQuery}/{strPage}", method = RequestMethod.GET)
    public ModelAndView getRequestsSearchResults(Principal principal,
                                                 HttpServletRequest request,
                                                 @PathVariable String searchQuery,
                                                 @PathVariable String strPage){
        if(isCurrentUserAdminOrEdit()) {
            if (userRepository.count() == 0) {
                return new ModelAndView("redirect:/users");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
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
            modelAndView.setViewName("redirect:/users/1");
            return modelAndView;
        }
        Pageable pageable = PageRequest.of(page-1, 7, Sort.by("active").ascending());
        String mode = "t";
        Page<User> users = Page.empty();

        try {
            mode = request.getSession().getAttribute("usr_mode") instanceof String ? (String)request.getSession().getAttribute("usr_mode") : request.getParameter("usr_mode");
            if(mode == null){
                return new ModelAndView("redirect:/users");
            }
            System.out.println("Режим поиска: " + mode);
            if ("n".equals(mode)) {

                users = userRepository.findByUsernameLikeIgnoreCase('%' + searchQuery + '%', pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по имени: " + searchQuery);

            } else if ("e".equals(mode)) {

                users = userRepository.findByEmailLikeIgnoreCase('%' + searchQuery + '%', pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по email: " + searchQuery);

            } else if ("p".equals(mode)) {

                users = userRepository.findByPhonenumberLikeIgnoreCase('%' + searchQuery + '%', pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по номеру телефона: " + searchQuery);

            } else if ("f".equals(mode)) {

                users = userRepository.findByFullnameLikeIgnoreCase('%' + searchQuery + '%', pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по фио: " + searchQuery);

            } else if ("d".equals(mode)) { //by post

                users = userRepository.findByPostLikeIgnoreCase('%' + searchQuery + '%', pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по должности: " + searchQuery);

            }
            else if ("s".equals(mode)){

                users = userRepository.findByActive(Boolean.parseBoolean(searchQuery), pageable);
                modelAndView.addObject("users", users);
                System.out.println("Поиск по статусу пользователя: " + searchQuery);

            }
            System.out.println(users.toString());
            System.out.println("Найдено совпадений: " + users.getTotalElements());
            modelAndView.addObject("searchQuery",searchQuery);
            modelAndView.setViewName("users_search");

        } catch (NumberFormatException e) {

            System.out.println("Был произведен нецелочисленный поиск по полю статус: " + searchQuery);
            modelAndView.addObject("status_error","Статус не является числом, повторите запрос");
            modelAndView.addObject("searchQuery",searchQuery);
            modelAndView.setViewName("empty_search");

        }

        if(page>users.getTotalPages() && (page>1)){
            return new ModelAndView("redirect:/requests/"+URLEncoder.encode(searchQuery,StandardCharsets.UTF_8)+"/"+users.getTotalPages());
        }
        modelAndView.addObject("usr_mode", mode);
        return modelAndView;
    }


//    @RequestMapping(value = "/requests/add", method = RequestMethod.GET)
//    public ModelAndView getRequestAddPage(){
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("request_add");
//        return modelAndView;
//    }

//    @RequestMapping(value = "/requests/add", method = RequestMethod.POST)
//    public ModelAndView setRequestAddPage(@RequestParam(name ="files",required = false) MultipartFile[] files,
//                                          @RequestParam("title") String title,
//                                          @RequestParam("description") String description,
//                                          Principal principal, HttpServletRequest request, RedirectAttributes redirectAttributes){
//        User user = userRepository.findByUsername(principal.getName());
//        UserRequests userRequest = new UserRequests(title,description,0, user);
//        userRequest = userRequestsRepository.save(userRequest);
//        System.out.println(files.length);
//        final String PATH = "C:\\Users\\utt61\\Desktop\\projects\\ska\\media\\attach\\requests\\" + userRequest.getId();
//        File directory = new File(PATH);
//        if (!directory.exists()) {
//            boolean created = directory.mkdirs();
//            if (created) {
//                System.out.println("directory  was created");
//            } else {
//                System.out.println("directory was not created");
//            }
//        }
//        String random;
//        String extension = "";
//        String filename;
//        String editedFilename;
//        String orig_filename;
//        String urlpath;
//        List<MultipartFile> allFiles = new ArrayList<>(Arrays.asList(files));
//
//
//        int i = 0;
//        try {
//            for (MultipartFile file : allFiles) {
//                List<Attachments> attachments = new ArrayList<>();
//                Attachments attachment = new Attachments();
//                JSONObject json = new JSONObject();
//                if (file.isEmpty()) {
//                    System.out.println("empty file skipped");
//                    continue; //next pls
//                }
//                random = UUID.randomUUID().toString();
//                orig_filename = file.getOriginalFilename();
//                extension = FilenameUtils.getExtension(orig_filename);
//                filename = FilenameUtils.getBaseName(orig_filename);
//                editedFilename = filename.length()>25?filename.substring(0,25)+".."+extension:filename+"."+extension;
//                MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
//                //String mimeType = fileTypeMap.getContentType(orig_filename);
//                Tika tika=new Tika();
//                String mimeType = tika.detect(FilenameUtils.getName(file.getOriginalFilename()));
//                Path path = Paths.get(PATH, random + "." + extension);
//                urlpath = "media/attach/requests/" + userRequest.getId() + "/" + random + "." + extension;
//                System.out.println(orig_filename + " : " + mimeType);
//                Files.write(path, file.getBytes());
//                if(mimeType.contains("image")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            true, false, false, false, false, false, false, false);
//                } else if(mimeType.contains("audio")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, true, false, false, false, false, false, false);
//                } else if(mimeType.contains("video")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, true, false, false, false, false, false);
//                } else if(mimeType.contains("pdf")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, true, false, false, false, false);
//                } else if(mimeType.contains("excel") || mimeType.contains("spreadsheetml.sheet")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, false, true, false, false, false);
//                } else if(mimeType.contains("word") || mimeType.contains("wordprocessingml.document")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, false, false, true, false, false);
//                } else if(mimeType.contains("plain")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, false, false, false, true, false);
//                } else if(mimeType.contains("zip") || mimeType.contains("7z") || mimeType.contains("rar")){
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, false, false, false, false, true);
//                } else {
//                    attachment = new Attachments(editedFilename, urlpath,
//                            false, false, false, false, false, false, false, false);
//                }
//
//                // what the fuck am i wrote, gotta make a note not to write such trash again, like really, look at this...
//
//
//                System.out.println("file \"" + orig_filename + "\" was saved");
//                JSONArray arr = new JSONArray();
//                arr.put(new JSONObject().put("url", attachment.getPaths()));
//                arr.put(new JSONObject().put("filename", attachment.getName()));
//                arr.put(new JSONObject().put("image", attachment.isImage()));
//                arr.put(new JSONObject().put("audio", attachment.isAudio()));
//                arr.put(new JSONObject().put("video", attachment.isVideo()));
//                arr.put(new JSONObject().put("pdf", attachment.isPdf()));
//                arr.put(new JSONObject().put("excel", attachment.isExcel()));
//                arr.put(new JSONObject().put("word", attachment.isWord()));
//                arr.put(new JSONObject().put("txt", attachment.isTxt()));
//                arr.put(new JSONObject().put("archive", attachment.isArchive()));
//                i++;
//                json.put("file" + i, arr);
//                String strmessage = "";
//                attachment.setPaths(json.toString());
//                System.out.println(json.toString());
//                attachments.add(attachment);
//                Message message = new Message(strmessage, user, userRequest, attachments);
//                message = messagesRepository.save(message);
//                for(Attachments att : message.getAttachments()){
//                    att.setMessage(message);
//                }
//                messagesRepository.save(message);
//            }
//
//
////            for(Attachments att: attachments){
////                att.setMessage(message);
////            }
////            attachmentsRepository.saveAll(attachments);
//        }
//        catch(Exception ex){
//            ex.printStackTrace();
//        }
//
//        ModelAndView modelAndView = new ModelAndView();
//        redirectAttributes.addFlashAttribute("successMessage", "Ваша заявка была успешно добавлена, ожидайте обновлений");
//        modelAndView.setViewName("redirect:/requests");
//        return modelAndView;
//    }

    @RequestMapping(value = "/users/id/{strId}", method = RequestMethod.GET)
    public ModelAndView getRequestView(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        long id;
        try {
            id = Long.parseLong(strId);
        } catch (NumberFormatException e){
            return new ModelAndView("redirect:/404");
        }
        User user = userRepository.findById(id);
        if(user == null){
            return new ModelAndView("redirect:/404");
        }
//        if(!user.getUsername().equals(principal.getName()) && !isCurrentUserAdmin()){
//            return new ModelAndView("redirect:/requests");
//        }
        Pageable pageable = PageRequest.of(0,7, Sort.by("status").ascending());
        Page<UserRequests> requests = userRequestsRepository.findByUser(user,pageable);
        modelAndView.setViewName("users_view");
        modelAndView.addObject("user", user);
        modelAndView.addObject("requests",requests);
        Cookie cookie = new Cookie("token",user.getToken());
        response.addCookie(cookie);
        return modelAndView;
    }

    @RequestMapping(value = "/users/id/{strId}/{strPage}", method = RequestMethod.GET)
    public ModelAndView getRequestViewPag(HttpServletRequest request, @PathVariable String strId, @PathVariable String strPage, Principal principal, HttpServletResponse response){
        if(isCurrentUserAdminOrEdit()) {
            if (userRequestsRepository.count() == 0) {
                return new ModelAndView("redirect:/users");
            }
        }
        else{
            return new ModelAndView("redirect:/requests");
        }
        ModelAndView modelAndView = new ModelAndView();
        int page;
        long id;
        try {
            page = Integer.parseInt(strPage);
            id = Long.parseLong(strId);
        }
        catch (NumberFormatException e){
            modelAndView.setViewName("redirect:/404");
            return modelAndView;
        }
        // 2147483648

        if(page<1){
            modelAndView.setViewName("redirect:/users/id/"+id);
            return modelAndView;
        }

        modelAndView.setViewName("users_list");
        User user = userRepository.findById(Long.parseLong(strId));
        Pageable pageable = PageRequest.of(page-1, 7, Sort.by("status").ascending());
        Page<UserRequests> requests = userRequestsRepository.findByUser(user,pageable);
        if(page>requests.getTotalPages() && (page>1)){
            return new ModelAndView("redirect:/users/"+requests.getTotalPages());
        }

        modelAndView.setViewName("users_view");
        modelAndView.addObject("user", user);
        modelAndView.addObject("requests",requests);
        return modelAndView;
    }

    @RequestMapping(value = "/users/id/{strId}/edit", method = RequestMethod.GET)
    public ModelAndView getEditUser(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        long id;
        try {
            id = Long.parseLong(strId);
        } catch (NumberFormatException e){
            return new ModelAndView("redirect:/404");
        }
        User user = userRepository.findById(id);
        if(user == null){
            return new ModelAndView("redirect:/404");
        }
        modelAndView.setViewName("edit_user");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @RequestMapping(value = "/users/id/{strId}/edit", method = RequestMethod.POST)
    public ModelAndView getEditUserSave(HttpServletRequest request, @PathVariable String strId, Principal principal, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        long id;
        try {
            id = Long.parseLong(strId);
        } catch (NumberFormatException e){
            return new ModelAndView("redirect:/404");
        }
        User user = userRepository.findById(id);
        if(user == null){
            return new ModelAndView("redirect:/404");
        }
        user.setFullname(request.getParameter("fio"));
        user.setPost(request.getParameter("post"));
        user.setEmail(request.getParameter("email"));
        user.setPhonenumber(request.getParameter("phone"));
        userRepository.save(user);
        modelAndView.setViewName("redirect:/users/id/"+id);
        return modelAndView;
    }

    private boolean isCurrentUserAdmin(){
        String currentUserAuthority = (String) SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return currentUserAuthority.equals("ADMIN");
    }

    private boolean isCurrentUserAdminOrEdit(){
        String currentUserAuthority = (String) SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return currentUserAuthority.equals("ADMIN") || currentUserAuthority.equals("EDIT");
    }


}
