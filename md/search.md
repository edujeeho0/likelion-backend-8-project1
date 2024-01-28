# 검색 기능

## HTML 구성

검색은 일반적으로 URL의 Query 부분을 통해서 검색어를 전달하는 방식으로 만들어진다.
여기에 사용자가 검색어를 입력할 수 있도록 `form` 요소를 사용하되,
`method="get"`으로 데이터를 조회하는 요청을 보내도록 한다. 게시판을 볼때 사용했던 `board.html`에 추가한다.

```html
<form class="d-flex align-items-center" action="/article/search" method="get">
  <input type="text" name="q" class="form-control me-2" placeholder="검색어">
  <input type="submit" class="btn btn-primary" value="검색하기">
</form>
```

검색 기준은 "선택된 게시판" + "제목 또는 내용"이다. 
선택된 게시판은 이전에 게시판 조회 기능에서 `selected`라는 데이터로 확인할 수 있으니, 이를 `<input type="hidden">`을 활용해
사용자가 보지 못하는 곳에 숨겨둔다.

```html
<form class="d-flex align-items-center" action="/article/search" method="get">
  <input type="text" name="q" class="form-control me-2" placeholder="검색어">
  <input th:unless="${selected == null}" type="hidden" name="board-id" th:value="${selected.id}">

  <input type="submit" class="btn btn-primary" value="검색하기">
</form>
```

제목 또는 내용을 구분하기 위해, 게시글 작성시 활용한 `select`와 `option`을 활용한다.

```html
<form class="d-flex align-items-center" action="/article/search" method="get">
  <input type="text" name="q" class="form-control me-2" placeholder="검색어">
  <input th:unless="${selected == null}" type="hidden" name="board-id" th:value="${selected.id}">
  <select class="form-control me-2" name="criteria">
    <option selected value="title">제목</option>
    <option value="content">내용</option>
  </select>
  <input type="submit" class="btn btn-primary" value="검색하기">
</form>
```

이 `form`의 요청을 받는 컨트롤러의 메서드는 다음과 같이 생겼다.

```java
    @GetMapping("search")
    public String search(
            @RequestParam("q")
            String query,
            @RequestParam(value = "board-id", defaultValue = "0")
            Long boardId,
            @RequestParam(value = "criteria")
            String criteria,
            Model model
    ) {
        model.addAttribute("query", query);
        model.addAttribute("boardId", boardId);
        if (!boardId.equals(0L))
            model.addAttribute("boardName", boardService.read(boardId).getName());
        model.addAttribute("criteria", criteria.equals("title") ? "제목" : "내용");
        model.addAttribute("articles", articleService.search(boardId, criteria, query));
        return "search";
    }
```

그 외의 게시글 목록을 위한 HTML은 게시판 HTML과 유사하게 작성한다.

## `ArticleRepository`


JPA를 활용하면서 가장 간단하게 SQL 쿼리를 대신할 수 있는건 Query Method이다.
`ArticleRepository`에 총 4개의 메서드를 추가해 사용한다.

```java
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 게시판이 주어졌을 때 제목 기준
    List<Article> findByTitleContainsAndBoardId(String query, Long boardId);
    // 게시판이 주어졌을 때 내용 기준
    List<Article> findByContentContainsAndBoardId(String query, Long boardId);
    // 게시판이 없을 때 제목 기준
    List<Article> findByTitleContains(String query);
    // 게시판이 없을 때 내용 기준
    List<Article> findByContentContains(String query);
}
```

## 검색 메서드

`ArticleService`에 검색을 위한 메서드를 추가한다. 매개변수로는

1. 대상 게시판 ID
2. 검색 기준 (제목, 내용)
3. 검색어

를 받는다. 이때, 게시판 ID가 `0L`인 경우 특정 게시판이 아닌 전체 게시판을 기준으로 한다. 

```java
public List<ArticleDto> search(Long boardId, String criteria, String query){
    List<ArticleDto> results = new ArrayList<>();
    List<Article> articles;
    if (boardId == 0L) {
    articles = criteria.equals("title")
            ? articleRepository.findByTitleContains(query)
            : articleRepository.findByContentContains(query);
    } else {
    articles = criteria.equals("title")
            ? articleRepository.findByTitleContainsAndBoardId(query, boardId)
            : articleRepository.findByContentContainsAndBoardId(query, boardId);
    }

    for (Article article: articles) {
    results.add(ArticleDto.fromEntity(article));
    }

    return results;
}
```

