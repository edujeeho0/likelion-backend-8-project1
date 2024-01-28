# 게시글 추가 기능 (이미지)

이미지는 데이터베이스에 직접 저장하지 않고, 파일의 형태로 서버가 실행된 곳에 저장해 두었다가
그 위치를 기억하여 게시글이 조회될때 같이 전달해 주어야 한다.

## HTML

이미지를 비롯한 파일을 전달하는 경우도 마찬가지로 `form`을 사용하며, `<input type="file">`을 사용한다.
또한 이미지는 다른 단순한 데이터를 전송하는 것과 다른 방식으로 데이터를 전송해야 한다. 자세한건 추후
다룰 예정. `enctype="multipart/form-data`를 추가해야 한다.

```html
<form class="d-flex align-items-center mb-3 w-50" th:action="@{/article/{id}/image(id=${article.id})}" method="post" enctype="multipart/form-data">
  <input type="file" name="image" class="form-control me-2">
  <input type="password" name="password" class="form-control me-2" placeholder="비밀번호">
  <input type="submit" class="btn btn-info" value="이미지 추가">
</form>
```

컨트롤러에서 이를 받을 때는 `MultipartFile`이라는 클래스로 받는다. 여기에 `@RequestParam`을 적용한다.
추가로 `enctype`이 바뀌었기 때문에, `@PostMapping`에도 `consumes = MediaType.MULTIPART_FORM_DATA_VALUE`를
추가해줘야 한다.

```java
@PostMapping(
        value = "article/{id}/image",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
public String addImage(
        @PathVariable("id")
        Long id,
        @RequestParam(value = "board", required = false)
        Long board,
        @RequestParam("password")
        String password,
        @RequestParam("image")
        MultipartFile image
) {
    articleService.addImage(id, image, password);
    return String.format("redirect:/article/%d", id);
}
```

## `ArticleImage`

게시글이 여러 이미지를 받을 수 있기 때문에 `1:N` 관계의 Entity를 따로 만들어준다.
이를 쉽게 활용하기 위해 `Article`에도 `images`를 추가해 `@OneToMany` 속성을 생성한다.

```java
@Getter
@Entity
@NoArgsConstructor
public class ArticleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String link;
    @Setter
    private String path;

    @Setter
    @ManyToOne
    private Article article;
}
```

이때, `link`는 HTML 상에서 이미지를 찾을 수 있는 경로(`http://localhost:8080/static/**` 등)를 의미하며,
`path`는 물리적 서버 상에 실제로 이미지가 저장된 경로(`C:\Users\` 등)를 의미한다. 

## `ArticleService`

이미지를 서버에 저장하는 과정은 여러 방법이 있지만, `java.io`와 `java.nio`의 기능들을 활용한다.
먼저 `MultipartFile` 객체의 기능을 활용해 이미지가 저장되는 경로와 이름을 작성하고, 
만약 이미지를 저장하는 경로가 존재하지 않으면 만들어준다.

```java
// 확장자 분리
String extension = image.getOriginalFilename().split("\\.")[1];
// 이미지가 저장될 경로를 게시글 단위로 분리한다.
String imageDir = String.format("./media/article/%d", id);
// 이미지가 서버 상에 저장될 이름을 임의로 만들어준다.
String imageFileName = UUID.randomUUID() + "." + extension;

// 만약 이미지를 저장할 경로가 서버에 없다면, 만들어준다.
try {
    Files.createDirectories(Paths.get(imageDir));
} catch (IOException e) {
    log.error(e.getMessage());
    throw new RuntimeException("Internal Server Error");
}
```

이후 `MultipartFile`이 가지고 있는 이미지의 실제 `byte[]` 데이터를
파일에 저장한다.

```java
Path filePath = Path.of(imageDir, imageFileName);
File file = new File((filePath.toUri()));
try (OutputStream outputStream = new FileOutputStream(file)){
    outputStream.write(image.getBytes());
} catch (IOException e) {
    log.error(e.getMessage());
    throw new RuntimeException("Internal Server Error");
}
```

여기까지 성공적으로 진행된다면, 이미지의 정보를 데이터베이스에 저장한다.

```java
String imageUrl = String.format("/static/article/%d/%s", id, imageFileName);
String filePathStr = filePath.toString();
ArticleImage articleImage = new ArticleImage();
articleImage.setLink(imageUrl);
articleImage.setPath(filePathStr);
articleImage.setArticle(article);
articleImageRepository.save(articleImage);
```

전체 메서드는 다음과 같다. 대상 게시글이 존재하는지, 비밀번호가 일치하는지에 대한 로직이 함께
구현되어 있다.

```java
public void addImage(Long id, MultipartFile image, String password) {
    Article article = articleRepository.findById(id).orElseThrow();
    if (!article.getPassword().equals(password))
        throw new RuntimeException("Bad Request");
    String extension = image.getOriginalFilename().split("\\.")[1];
    String imageDir = String.format("./media/article/%d", id);
    String imageFileName = UUID.randomUUID() + "." + extension;

    try {
        Files.createDirectories(Paths.get(imageDir));
    } catch (IOException e) {
        log.error(e.getMessage());
        throw new RuntimeException("Internal Server Error");
    }

    Path filePath = Path.of(imageDir, imageFileName);
    File file = new File((filePath.toUri()));
    try (OutputStream outputStream = new FileOutputStream(file)){
        outputStream.write(image.getBytes());
    } catch (IOException e) {
        log.error(e.getMessage());
        throw new RuntimeException("Internal Server Error");
    }

    String imageUrl = String.format("/static/article/%d/%s", id, imageFileName);
    String filePathStr = filePath.toString();
    ArticleImage articleImage = new ArticleImage();
    articleImage.setLink(imageUrl);
    articleImage.setPath(filePathStr);
    articleImage.setArticle(article);
    articleImageRepository.save(articleImage);
}
```

## 삭제

삭제의 경우 `articleId`, `imageId`, `password`를 받는다.
`imageId`로 이미지 Entity를 조회하고, 연결된 `Article`의 `ID`와 일치하는지 확인,
`Article`의 비밀번호와 전달된 비밀번호가 일치하는지를 확인한 후 삭제한다.

```java
public void deleteImage(Long articleId, Long imageId, String password) {
    ArticleImage articleImage = articleImageRepository.findById(imageId)
            .orElseThrow();

    if (!articleImage.getArticle().getId().equals(articleId))
        throw new RuntimeException("Bad Request");
    if (articleImage.getArticle().getPassword().equals(password)) {
        articleImageRepository.delete(articleImage);
    }
    // TODO else에서 throw
}
```

이미지를 서버에서 지워야 하는 조건은 요구사항에 명시되어 있지 않다. 만약 구현하고자 한다면
`ArticleImage`의 `path` 속성을 활용할 수 있다.

## 설정

이미지는 정적 자원으로 사용자에게 전달된다. 이를 위해서 `application.yaml`의 설정을 변경해준다.

```yaml
spring:
  mvc:
    static-path-pattern: /static/**
  web:
    resources:
      static-locations: file:media/,classpath:/static
```

- `spring.mvc.static-path-pattern`은 사용자가 어떤 URL로 요청을 해야 정적 자원을 돌려주는지에 대한 설정이다.
- `spring.web.resources.static-locations`는 어떤 폴더의 데이터를 정적 자원으로 전달할지에 대한 설정이다.

