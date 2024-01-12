# 게시글 기능

## 비밀번호

게시글과 댓글 요구사항에, 수정과 삭제를 위한 비밀번호 항목이 있다.
이는 실제 로그인 과정을 대체하기 위한 요소에 불과하기 때문에, `@Entity`의
속성으로 정의해주고,

```java
@Getter
@Entity
@NoArgsConstructor
public class Article {
    // ...
    
    @Setter
    private String password;
    
    // ...
}
```

수정과 삭제를 위한 `Entity`를 받았을 때 거기 기록된 값과 일치하는지만 비교하면 된다.

## 게시글 작성

기본적으로 작성을 위한 HTML은 `/article/new`에서 받아간다.
게시글을 작성하면서 게시판을 선택할 수 있어야 하기 때문에, `BoardService.readAll()`을 사용한다.
HTML에서는 `select` 요소를 사용하며, 내부에 `option`을 `th:each`를 활용하여 반복한다.

```java
@Controller
@RequiredArgsConstructor
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @GetMapping("article/new")
    public String newArticle(
            Model model
    ) {
        model.addAttribute("boards", boardService.readAll());
        return "article/new";
    }
    // ...
}
```

작성하고 난 뒤에는 바로 상세보기 페이지로 이동할 수 있도록 간단한 코드를 추가했다.

```java
@Controller
@RequiredArgsConstructor
public class ArticleController {
    // ...
    @PostMapping("article")
    public String createArticle(
            @RequestParam("title")
            String title,
            @RequestParam("content")
            String content,
            @RequestParam("password")
            String password,
            @RequestParam("board-id")
            Long boardId
    ) {
        Long newId = articleService.create(boardId, new ArticleDto(title, content, password)).getId();
        return String.format("redirect:/article/%d", newId);
    }
}
```

## 게시글 수정 & 삭제

앞서 이야기 했듯, 수정과 삭제를 위해서 받은 `id`를 기준으로 `Article`을 조회한 다음,
해당 `Article`에 담긴 `password`와 수정 및 삭제 시 전달받은 `password`를 비교한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    // ...
    public ArticleDto update(Long id, ArticleDto articleDto) {
        Article article = articleRepository.findById(id)
                .orElseThrow();
        if (article.getPassword().equals(articleDto.getPassword())) {
            article.setTitle(articleDto.getTitle());
            article.setContent(articleDto.getContent());
        }
        // TODO else에서 throw
        return ArticleDto.fromEntity(articleRepository.save(article));
    }

    public void delete(Long id, String password) {
        Article article = articleRepository.findById(id)
                .orElseThrow();
        if (article.getPassword().equals(password)) {
            articleRepository.delete(article);
        }
        // TODO else에서 throw
    }
}
```

만일 전달된 비밀번호가 일치하지 않다면, 지금은 동작만 하지 않는다.
미래에는 사용자에게 오류를 알리는 기능을 추가해줄 수 있다.


