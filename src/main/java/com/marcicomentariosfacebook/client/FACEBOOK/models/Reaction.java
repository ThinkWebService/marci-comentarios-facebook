package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

@Data
public class Reaction {
    private String id;
    private String name;
    private String type;  // LIKE, LOVE, etc.
}
