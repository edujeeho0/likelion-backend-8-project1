# 댓글 기능

댓글의 경우 작성 & 삭제만 필요하며, 따로 댓글을 확인하는 UI가 없다(게시글 내부에서 확인).
그래서 내용이 상대적으로 적다.

## `@Controller`

댓글은 무조건 게시글의 부속품으로 활용되는 만큼 전체 `@Controller`에 `@RequestMapping`을 추가했다.
여기에 작성된 Path 변수도 내부 메서드의 `@RequestMapping`에서 사용 가능하다.

```java
@Slf4j
@Controller
@RequestMapping("article/{articleId}/comment")
@RequiredArgsConstructor
public class CommentController {
    // ...
}
```

## 댓글 작성 & 삭제

댓글을 작성하든 삭제하든, 이후 이동하는 곳은 댓글이 있는 게시글 페이지이다.
이 정보를 위해 `articleId`를 활용한다.

```java
@Slf4j
@Controller
@RequestMapping("article/{articleId}/comment")
@RequiredArgsConstructor
public class CommentController {
    // ...
    @PostMapping
    public String createComment(
            @PathVariable("articleId")
            Long articleId,
            @RequestParam("content")
            String content,
            @RequestParam("password")
            String password
    ) {
        commentService.createComment(articleId, new CommentDto(content, password));
        return String.format("redirect:/article/%d", articleId);
    }

    @PostMapping("{commentId}/delete")
    public String deleteComment(
            @PathVariable("articleId")
            Long articleId,
            @PathVariable("commentId")
            Long commentId,
            @RequestParam("password")
            String password
    ) {
        commentService.deleteComment(commentId, password);
        return String.format("redirect:/article/%d", articleId);
    }
}
```

작성과 삭제는 게시글의 작성과 삭제와 유사하다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public CommentDto createComment(Long articleId, CommentDto dto) {
        Article article = articleRepository.findById(articleId).orElseThrow();

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setContent(dto.getContent());
        comment.setPassword(dto.getPassword());
        return CommentDto.fromEntity(commentRepository.save(comment));
    }

    public void deleteComment(Long id, String password) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow();
        if (comment.getPassword().equals(password)) {
            commentRepository.delete(comment);
        }
        // TODO else에서 throw
    }
}
```


