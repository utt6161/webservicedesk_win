package com.wsd.ska.messages;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.wsd.ska.attachments.Attachments;
import com.wsd.ska.attachments.AttachmentsRepository;
import com.wsd.ska.user.Role;
import com.wsd.ska.user.User;
import com.wsd.ska.user.UserRepository;
import com.wsd.ska.user_requests.UserRequests;
import com.wsd.ska.user_requests.UserRequestsRepository;
import com.wsd.ska.utils.UrlMatch;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;


@Singleton
@Controller
@Transactional
public class MessagesController {

    private static boolean savePermitted = true;

    @Autowired
    public UserRequestsRepository userRequestsRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public AttachmentsRepository attachmentsRepository;

    @Autowired
    public MessagesRepository messagesRepository;

    @Autowired
    public SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send.{id}")
    @SendTo("/topic/chat/{id}")
    public RequestMessage saveRequestMessages(Principal principal, @NotNull @Payload RequestMessage requestMessage, @DestinationVariable("id") String id) throws InterruptedException{

        try {
            UserRequests userRequest = userRequestsRepository.findById(Long.parseLong(id));
            if (!(isAllowedByRequest(userRequest, principal) || isAdmin(principal))) {
                return null;
            }
        } catch (NumberFormatException e){
            return null;
        }
        System.out.println("message was received: " + requestMessage.toString());
        String login = requestMessage.getLogin().trim();
        String messageText = requestMessage.getMessage_data().trim();
        User user = userRepository.findByUsername(login);
        UserRequests userRequest = userRequestsRepository.findById(Long.parseLong(id));
        System.out.println("message content: " + messageText);
//        messageText = messageText.replaceAll(UrlMatch.REGEX_URL,"<a href='"+
//                $0.replaceAll("(?:(?:"+ UrlMatch.REGEX_USERINFO + UrlMatch.REGEX_HOST + UrlMatch.REGEX_PORT +
//                        UrlMatch.REGEX_RESOURCE_PATH + ) +"' target='_blank' ref='nofollow noopener'>"+"$0"+"</a>");


        String[] mess_tokens = messageText.split(" ");
        for(int i = 0;i<mess_tokens.length;i++){
            System.out.println(mess_tokens[i]);
            if(mess_tokens[i].matches(UrlMatch.REGEX_COMPILED.toString()) && !mess_tokens[i].matches(UrlMatch.NO_PROTOCOL)){
                mess_tokens[i] = "<a href='"+mess_tokens[i]+"' target='_blank' ref='nofollow noopener'>"+mess_tokens[i]+"</a>";
            }
        }
        messageText = String.join(" ", mess_tokens);
        System.out.println("replaced message content: " + messageText);
        Message message = new Message(messageText, user, userRequest);
        messagesRepository.save(message);

        System.out.println(message.getDate().toString());
        requestMessage.setMessage_data(messageText);
        requestMessage.setTimestamps(message.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")) + " (utc+5)");
        System.out.println(requestMessage.toString());
        System.out.println("Сообщение ушло");
        return requestMessage;
    }

    @ResponseBody
    @PostMapping("/request_close/{requestid}")
    public ResponseEntity close(Principal principal,
            @PathVariable String requestid){

        if (!isAdmin(principal)) {
            return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
        }

//        ModelAndView modelAndView = new ModelAndView();
        UserRequests request = userRequestsRepository.findById(Long.parseLong(requestid));
        request.setStatus(1);
        System.out.println(request.getTitle());
        userRequestsRepository.save(request);

        requestToggle("/topic/closed/"+Integer.parseInt(requestid),new ClosedRequest("1"));
//        modelAndView.setViewName("redirect:/requests/id/"+Integer.parseInt(requestid));
//        return new ResponseEntity("request closed", HttpStatus.OK);
        return new ResponseEntity("OK",HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/request_work/{requestid}")
    public ResponseEntity inprogress(Principal principal,
                                @PathVariable String requestid){

        if (!isAdmin(principal)) {
            return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
        }

//        ModelAndView modelAndView = new ModelAndView();
        UserRequests request = userRequestsRepository.findById(Long.parseLong(requestid));
        request.setStatus(2);
        System.out.println(request.getTitle());
        userRequestsRepository.save(request);

        requestToggle("/topic/closed/"+Integer.parseInt(requestid),new ClosedRequest("2"));
//        modelAndView.setViewName("redirect:/requests/id/"+Integer.parseInt(requestid));
//        return new ResponseEntity("request closed", HttpStatus.OK);
        return new ResponseEntity("OK",HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/request_open/{requestid}")
    public ResponseEntity open(
            @PathVariable String requestid, Principal principal){

        if (!isAdmin(principal)) {
            return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
        }

//      ModelAndView modelAndView = new ModelAndView();
        UserRequests request = userRequestsRepository.findById(Long.parseLong(requestid));
        System.out.println(request.getTitle());
        request.setStatus(0);
        userRequestsRepository.save(request);

        requestToggle("/topic/closed/"+Integer.parseInt(requestid),new ClosedRequest("0"));
//        modelAndView.setViewName("redirect:/requests/id/"+Integer.parseInt(requestid));
//        return new ResponseEntity("request opened", HttpStatus.OK);
        return new ResponseEntity("OK",HttpStatus.OK);
    }

//    @ResponseBody
//    @GetMapping("/request_closed")
//    public ClosedRequest isClosed(
//            @RequestParam("requestid") String requestid){
//        UserRequests request = userRequestsRepository.findById(Long.parseLong(requestid));
//        if(request.getStatus()==1){
//            return new ClosedRequest(true);
//        }
//        return new ClosedRequest(false);
//    }

    @ResponseBody
    @GetMapping("/get_messages")
    public Object handleList(
            @RequestParam("requestid") String requestid,
            @RequestParam("token") String token,
            Principal principal) {

        try {
            UserRequests userRequest = userRequestsRepository.findById(Long.parseLong(requestid));
            if (!(isAllowedByRequest(userRequest, principal) || isAdmin(principal))) {
                return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e){
            return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
        }

        System.out.println(requestid);
        System.out.println(token);
        System.out.println("ajax to get messages is allowed");
        List<Message> messages= messagesRepository.findByRequestId(Long.parseLong(requestid));
        List<RequestMessage> requestMessages = new ArrayList<>();
        for(Message mes : messages){
            List<Attachments> attachments = attachmentsRepository.findByMessageId(mes.getId());
            RequestMessage requestMessage = new RequestMessage();
            if(attachments.isEmpty()){
                System.out.println("There is no attachments???");
                requestMessage = new RequestMessage(
                        requestid,mes.getUser().getFullname(),
                        mes.getUser().getPost(),mes.getMessageText(),mes.getUser().getUsername());
                requestMessage.setTimestamps(mes.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")) + " (utc+5)");
            } else {
                System.out.println(mes.getAttachments().get(0).getPaths());
                System.out.println("getting attachments");
                for (Attachments att : attachments) {
                    requestMessage = new RequestMessage(
                            requestid, mes.getUser().getFullname(),
                            mes.getUser().getPost(), mes.getMessageText(), mes.getUser().getUsername(), att.getPaths(),
                            mes.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")) + " (utc+5)");
                }
            }
            requestMessages.add(requestMessage);
        }
        return requestMessages;
    }


    @ResponseBody
    @PostMapping("/send_message")
    public ResponseEntity<?> uploadFileMulti(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("requestid") String requestId,
            @RequestParam("fullname") String fullname,
            @RequestParam("userpost") String userpost,
            @RequestParam("login") String login,
            Principal principal) {

        UserRequests userRequest;
        try {
            userRequest = userRequestsRepository.findById(Long.parseLong(requestId));
            if (!(isAllowedByRequest(userRequest, principal) || isAdmin(principal))) {
                return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e){
            return new ResponseEntity("OPERATION NOT ALLOWED", HttpStatus.FORBIDDEN);
        }

//        final String PATH = ".media/" + requestId;
        final String PATH = "C:\\Users\\utt61\\Desktop\\projects\\ska\\media\\attach\\requests\\" + requestId;
        User user = userRepository.findByUsername(login);
        File directory = new File(PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if(created) {
                System.out.println("directory  was created");
            }
            else{
                System.out.println("directory was not created");
            }
        }
        String random;
        String extension = "";
        String filename;
        String editedFilename;
        String orig_filename;
        String urlpath;
        Attachments attachment = new Attachments();
        List<MultipartFile> allFiles = new ArrayList<>(Arrays.asList(files));

        List<Attachments> attachments = new ArrayList<>();
        JSONObject json = new JSONObject();
        int i = 0;
        try {
            for (MultipartFile file : allFiles) {
                if (file.isEmpty()) {
                    System.out.println("empty file skipped");
                    continue;
                }
                random = UUID.randomUUID().toString();
                orig_filename = file.getOriginalFilename();
                extension = FilenameUtils.getExtension(orig_filename);
                filename = FilenameUtils.getBaseName(orig_filename);
                editedFilename = filename.length()>25?filename.substring(0,25)+".."+extension:filename+"."+extension;
                MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
                Tika tika=new Tika();
                String mimeType = tika.detect(FilenameUtils.getName(file.getOriginalFilename()));
                Path path = Paths.get(PATH, random + "." + extension);
                urlpath = "media/attach/requests/" + requestId + "/" + random + "." + extension;
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
                attachment.setPaths(json.toString());
                attachments.add(attachment);
            }
            String strmessage = "";
            Message message = new Message(strmessage, user, userRequest, attachments);
            message = messagesRepository.save(message);
            for(Attachments att : message.getAttachments()){
                att.setMessage(message);
            }
            messagesRepository.save(message);
            RequestMessage requestMessage = new RequestMessage(requestId,
                    fullname,userpost,strmessage,login,json.toString(),
                    message.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")) + " (utc+5)");

            System.out.println(requestMessage);
            messagingTemplate.convertAndSend("/topic/chat/"+requestId,requestMessage);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity("Message was received and sent!",HttpStatus.OK);
    }

    public boolean isAdmin(Principal principal){
        User user=userRepository.findByUsername(principal.getName());
        if (user!=null){
            Role role = (Role)user.getRoles().toArray()[0];
            if (role.getRole().equals("ADMIN") || role.getRole().equals("EDIT")) {
                System.out.println("user: " + user.getUsername() + " is admin or editor, thats fine");
                return true;
            }
        }
        System.out.println("user: " + user.getUsername() + " isnt admin");
        return false;
    }

    public boolean isAllowedByRequest(UserRequests userRequests, Principal principal){
        User user=userRepository.findByUsername(principal.getName());
        if (user!=null && userRequests!=null){
            if (userRequests.getUser().getId() == user.getId()) {
                System.out.println("user: " + user.getUsername() + " have access to request");
                return true;
            }
        }
        System.out.println("user: " + user.getUsername() + " have no access to request");
        return false;
    }

    public void requestToggle(String destination, ClosedRequest status){
        System.out.println("updating request: '" + destination + "' with status: " + (status.getClosed().equals("0")?"Открыта":"Закрыта"));
        messagingTemplate.convertAndSend(destination,status);
    }

    // 3.1.3 maps html form to a Model
//    @PostMapping("/api/upload/multi/model")
//    public ResponseEntity<?> multiUploadFileModel(@ModelAttribute UploadModel model) {
//
//        logger.debug("Multiple file upload! With UploadModel");
//
//        try {
//
//            saveUploadedFiles(Arrays.asList(model.getFiles()));
//
//        } catch (IOException e) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//        return new ResponseEntity("Successfully uploaded!", HttpStatus.OK);
//
//    }

}
