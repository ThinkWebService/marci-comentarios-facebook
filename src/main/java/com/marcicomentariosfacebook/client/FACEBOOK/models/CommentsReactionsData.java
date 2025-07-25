package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

import java.util.List;

// ======= COMENTARIOS Y REACCIONES =======
@Data
public class CommentsReactionsData {
    private List<CommentsReactions> data;
}
