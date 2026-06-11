package com.flowiq.models.request;


import lombok.Data;

@Data
public class SendChatMessageRequest {

    private Long conversationId;
    private String message;
}
