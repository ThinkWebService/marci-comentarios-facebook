package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("reaction")
public class Reaction {
    @Id
    private Long id;
    private String user_id;
    private String user_name;
    private String type;
    private String verb;
    private String comment_id;
    private String post_id;
}