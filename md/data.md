# 데이터 다루기

## `Board` Entity

Spring Boot Data JPA를 활용하는 만큼, 사용할 테이블에 맞는 Entity 클래스를 만들었다.
게시판 자체는 종류가 고정되어 있지만, 미래의 게시판이 늘어날 것을 고려해, JPA `Entity`로 만들었다.

```java
@Getter
@Entity
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Setter
    private String name;
    
    @OneToMany(mappedBy = "board")
    private final List<Article> articles = new ArrayList<>();
}
```

### Lombok

`@Data`대신, 전체 속성은 접근 가능하도록 클래스에는 `@Getter`를, 개발중에 수정하면 안되는
`id`나 `List`와 같은 `Collection`으로 활용하는 `@OneToMany` 속성을 제외하곤
`@Setter`를 사용했다. 그리고 상황에 따라 생성자를 따로 만드는 경우,
[`@Entity` 클래스에는 매개변수 없는 기본 생성자가 필요](https://stackoverflow.com/questions/2808747/why-does-jpa-require-a-no-arg-constructor-for-domain-objects)하기 때문에 `@NoArgsConstructor`를 덧붙였다.

## DTO

`Entity` 클래스의 데이터를 실제로 주고받을 DTO 클래스도 따로 만들어서 사용한다.
`Entity`는 `@ManyToOne` 쪽의 데이터가 필요하지만, 데이터를 사용하는 입장에서는
오히려 `@OneToMany`가 필요한 상황이 많았으며, 이를 반영하여 작성했다.
반대로 상황에 따라 필요한 데이터(게시글이 작성된 게시판 등)가 있다면 추가해 사용했다.

```java
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
    // ...
}
```

이 DTO 클래스는 한번 만들고 함부로 데이터를 조정하지 못하게 `@Setter`가 적용 안되어 있으며,
대신 각 속성에 필요한 데이터를 할당할 수 있도록 개별 생성자와 [Static Factory Method](https://stackoverflow.com/questions/929021/what-are-static-factory-methods)가 만들어져 있다.
만든 Static Factory Method를 활용하면, JPA Entity를 DTO로 변환하는 과정을 간소화 할 수 있다.

```java
@Getter
@ToString
@NoArgsConstructor
public class ArticleDto {
    // ...
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
```

## `Optional` 활용

기본 `JpaRepository`의 `findById()` 메서드는 `Optional`을 반환한다.
만약 조회한 결과가 없을 경우를 대비해 `Optional`을 주는 것이다.
여기서는 `Optional.orElseThrow()` 메서드를 활용해, 데이터가 없을 경우
예외를 발생하도록 했다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {
    // ...
    
    public ArticleDto create(Long boardId, ArticleDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow();
        Article article = new Article();
        article.setBoard(board);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setPassword(dto.getPassword());
        return ArticleDto.fromEntity(articleRepository.save(article));
    }
    
    // ...
}
```
