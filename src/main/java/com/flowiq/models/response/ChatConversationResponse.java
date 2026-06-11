package com.flowiq.models.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationResponse {

    private String id;
    private String title;
    private List<ChatMessageResponse> messages;
    private String createdAt;
    private String updatedAt;

}
