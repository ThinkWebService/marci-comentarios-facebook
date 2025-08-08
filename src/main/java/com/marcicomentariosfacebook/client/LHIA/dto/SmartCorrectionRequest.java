package com.marcicomentariosfacebook.client.LHIA.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmartCorrectionRequest {
    private String uuidConversation;
    private String message;
}