package com.marcicomentariosfacebook.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketEvent<T> {
    private String type; // "comment", "reaction", "post", etc.
    private T data;
}
