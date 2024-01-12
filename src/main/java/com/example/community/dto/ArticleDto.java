package com.example.community.dto;

import com.example.community.entity.Article;
import com.example.community.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private String password;
    private String boardName;
    private final List<CommentDto> comments = new ArrayList<>();

    public ArticleDto(String title, String content, String password) {
        this.title = title;
        this.content = content;
        this.password = password;
    }

    public static ArticleDto fromEntity(Article entity) {
        ArticleDto dto = new ArticleDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.content = entity.getContent().replace("\n", "<br>");
        dto.boardName = entity.getBoard().getName();
        for (Comment comment: entity.getComments()) {
            dto.comments.add(CommentDto.fromEntity(comment));
        }
        return dto;
    }
}
