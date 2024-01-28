# 게시글 추가 기능 (이전글, 다음글)

이전글과 다음글은 자신보다 먼저 작성된, 또는 나중에 작성된 글을 기준으로 자신과 가장 가까운 글을 찾는 기능이다.
이를 위해서는 특별한 테이블의 추가는 필요하지 않으며, 대신 조회를 하는 조건을 잘 만들어 주면 된다.

## Query Methods

게시글의 작성된 시간을 기록하지 않기 때문에, ID를 기준으로 조회를 한다.

- ID가 현재 글보다 큰 게시글 중 가장 작은 게시글이 이전글이다.
- ID가 현재 글보다 작은 게시글 중 가장 큰 게시글이 다음글이다.

이와 함께 게시판의 선택 유무에 따른 조건을 더해 4가지 메서드를 추가해준다.

```java
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 주어진 id보다 id가 큰 가장 첫 게시글
    Optional<Article> findFirstByIdAfter(Long id);
    // 주어진 id보다 id가 가장 작은 게시글을 역순 정렬했을 때 첫 게시글
    Optional<Article> findFirstByIdBeforeOrderByIdDesc(Long id);

    // 주어진 boardId와 일치하며 주어진 id보다 id가 큰 가장 첫 게시글
    Optional<Article> findFirstByBoardIdAndIdAfter(Long boardId, Long id);
    // 주어진 boardId와 일치하며 주어진 id보다 id가 가장 작은 게시글을 역순 정렬했을 때 첫 게시글
    Optional<Article> findFirstByBoardIdAndIdBeforeOrderByIdDesc(Long boardId, Long id);
}
```

## `ArticleController` & `ArticleService`

제일 첫 글은 이전글이 없고, 제일 뒷 글은 다음글이 없다. 그래서 이 정보를 전달하기 위한
메서드도 두개로 분리하고, `read.html`에서 정보의 유무에 따라 링크를 만드느냐 마느냐를
출력해준다.

각각 `articleId`와 `boardId`를 매개변수로 받으며, `boardId == 0L`을 기준으로 실행하는 메서드를 분기한다.

```java
public Long getFront(Long boardId, Long articleId) {
    Optional<Article> target;
    if (boardId == 0L) {
        target = articleRepository.findFirstByIdAfter(articleId);
    } else {
        target = articleRepository.findFirstByBoardIdAndIdAfter(boardId, articleId);
    }
    return target.map(Article::getId).orElse(null);
}

public Long getBack(Long boardId, Long articleId) {
    Optional<Article> target;
    if (boardId == 0L) {
        target = articleRepository.findFirstByIdBeforeOrderByIdDesc(articleId);
    } else {
        target = articleRepository.findFirstByBoardIdAndIdBeforeOrderByIdDesc(boardId, articleId);
    }
    return target.map(Article::getId).orElse(null);
}
```

이제 게시글 조회 기능에는 게시판 정보가 필요하며, 이는 게시글을 확인할때 어떤 URL을 사용하는지가
달라짐을 의미한다 (`/article/{id}` 대신 `/article/{id}?board=1`). 그래서 다른 기능들이 게시글을 향하고 있었다면 링크를 조금씩 변경해주어야 한다.

- 이미지 추가 `form`
    ```html
    <form class="d-flex align-items-center mb-3 w-50" th:action="@{/article/{id}/image(id=${article.id})}" method="post" enctype="multipart/form-data">
      <input type="file" name="image" class="form-control me-2">
      <input type="password" name="password" class="form-control me-2" placeholder="비밀번호">
      <input th:unless="${board == 0}" type="hidden" name="board" th:value="${board}">
      <input type="submit" class="btn btn-info" value="이미지 추가">
    </form>
    ```
- 게시글 조회 `a`
    ```html
    <tr th:each="article: ${articles}">
     <th scope="col" th:text="${article.id}"></th>
     <th scope="col">
       <a th:if="${selected == null}" th:href="@{/article/{id}(id=${article.id})}" th:text="${article.title}"></a>
       <a th:unless="${selected == null}" th:href="@{/article/{id}(id=${article.id},board=${selected.id})}" th:text="${article.title}"></a>
     </th>
     <th th:if="${selected == null}" scope="col" th:text="${article.boardName}"></th>
    </tr>
    ```

- `ArticleController.addImage`
    ```java
    @PostMapping(
            value = "article/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String addImage(
            // ...
    ) {
        articleService.addImage(id, image, password);
        if (board != null)
            return String.format("redirect:/article/%d?board=%d", id, board);
        return String.format("redirect:/article/%d", id);
    }
    ```
