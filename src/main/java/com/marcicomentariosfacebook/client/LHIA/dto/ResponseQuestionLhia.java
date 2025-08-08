package com.marcicomentariosfacebook.client.LHIA.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResponseQuestionLhia {
    private String original;
    private List<String> suggestions;
}
