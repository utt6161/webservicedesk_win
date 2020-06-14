package com.wsd.ska.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ClosedRequest {


    private String closed;

    public ClosedRequest() {
    }


    public ClosedRequest(String closed) {
        setClosed(closed);
    }
}
