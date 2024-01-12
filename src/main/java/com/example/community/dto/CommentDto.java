package com.example.community.dto;

import com.example.community.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private String password;

    public CommentDto(String content, String password) {
        this.content = content;
        this.password = password;
    }

    public static CommentDto fromEntity(Comment entity) {
        CommentDto dto = new CommentDto();
        dto.id = entity.getId();
        dto.content = entity.getContent();
        return dto;
    }
}
