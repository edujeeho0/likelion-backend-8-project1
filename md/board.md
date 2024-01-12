# 게시판 기능

## 기본 게시판 생성하기

요구사항에 따르면 기본으로 생성해야 하는 게시판은 총 네개이다.
- 자유 게시판
- 개발 게시판
- 일상 게시판
- 사건사고 게시판

서비스에 필요한 게시판을 만드는 방법은 여럿 있겠지만, 개별적인 서버가 실행될 가능성을 고려해,
`BoardService`가 실행될 때 존재하는 게시판을 확인 후 없는 게시판을 생성하는 방식을 선택했다.
우선 서비스상 반드시 필요로 하는 네개의 게시판을 `static final` 속성으로 클래스에 정의한다.

```java
@Slf4j
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private static final String[] basicBoardNames = {
            "자유 게시판",
            "개발 게시판",
            "일상 게시판",
            "사건사고 게시판"
    };

    // ...
}
```

이후 `BoardService`의 생성자에서 존재하는 게시판을 확인한 후
이때, `boardRepository.findAll()`을 사용할 수도 있겠지만, 어떤 이름을 가진 게시판의 존재를 확인하는
간단한 Query Method를 작성했다.

```java
public interface BoardRepository extends JpaRepository<Board, Long> {
    boolean existsByName(String name);
}

@Slf4j
@Service
public class BoardService {
    // ...
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
        for (String boardName : basicBoardNames) {
            if (!this.boardRepository.existsByName(boardName)) {
                Board board = new Board();
                board.setName(boardName);
                this.boardRepository.save(board);
            }
        }
    }
}
```

## 게시판별 보기

간단하게 `@GetMapping`에서 대상 게시판을 지정할 수 있도록 `@PathVariable`로
`boardId`를 받을 수 있도록 해주었다. 여기서 두가지를 고려했는데,

1. 전체 글 보기와 게시판 별 보기를 같은 템플릿(html)로 출력하고 싶어서 `selected`라는 값을
   `model`에 추가해 주었다. Thymeleaf를 가지고 `selected == null`에 따라서 출력되는 형식을 바꿔주는 방법이다.
2. `Collections.reverse`를 이용해 게시글의 순서를 `id`의 역순으로 전달한다.
   `id`가 클수록 나중에 작성된 글이다.

기술적으로는 `BoardDto.articles`에 데이터가 있고, `articles`를 `Collections.reverse`로 역순 정렬할수도 있지만,
`model`에 전달되는 게시글의 정보를 명확히 해주기 위해 따로 변수로 전달했다.

```java
@Controller
@RequestMapping("board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    // ...
    @GetMapping("{boardId}")
    public String listOneBoard(
            @PathVariable("boardId")
            Long boardId,
            Model model
    ) {
        BoardDto boardDto = boardService.read(boardId);
        model.addAttribute("boards", boardService.readAll());
        model.addAttribute("selected", boardDto);
        List<ArticleDto> articles = boardDto.getArticles();
        Collections.reverse(articles);
        model.addAttribute("articles", articles);
        return "board";
    }
}
```

전체를 볼때는 `@PathVariable`이 없으며, `model.addAttribute("selected", boardDto)`가 생략된다.
또한 게시판 구분 없이 전체 게시글 조회를 위해 `ArticleService`의 기능을 활용한다.

```java
@Controller
@RequestMapping("board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @GetMapping
    public String listAllBoards(
            Model model
    ) {
        model.addAttribute("boards", boardService.readAll());
        model.addAttribute("selected", null);
        List<ArticleDto> articles = articleService.readAll();
        Collections.reverse(articles);
        model.addAttribute("articles", articles);
        return "board";
    }
    // ...
}
```

