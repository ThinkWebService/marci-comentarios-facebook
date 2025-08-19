package com.marcicomentariosfacebook.client.FACEBOOK.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Attachments attachments;
    private String id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachments {
        private List<AttachmentData> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentData {
        private String media_type;
        private Media media;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Media {
        private String source;
    }
}