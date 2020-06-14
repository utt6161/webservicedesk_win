package com.wsd.ska.messages;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Data
@Component
public class RequestMessage {

    private String requestid;
    private String fullname;
    private String userpost;
    private String message_data;
    private String login;
    private String timestamps;
    private String paths;

    public RequestMessage() {
    }


    public RequestMessage(String requestid, String fullname, String userpost, String message_data, String login) {
        setRequestid(requestid);
        setFullname(fullname);
        setLogin(login);
        setMessage_data(message_data);
        setUserpost(userpost);
    }

    public RequestMessage(String requestid, String fullname, String userpost, String message_data, String login, String paths, String timestamps) {
        setRequestid(requestid);
        setFullname(fullname);
        setLogin(login);
        setMessage_data(message_data);
        setUserpost(userpost);
        setPaths(paths);
        setTimestamps(timestamps);
    }

}
