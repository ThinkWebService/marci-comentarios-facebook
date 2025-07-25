package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

import java.util.List;

// ======= ATTACHMENTS =======
@Data
public class AttachmentsWrapper {
    private List<Attachment> data;
}
