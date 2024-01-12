# 익명 커뮤니티

자신의 정보를 공개할 필요 없이 자유롭게 의견을 교환할 수 있는 웹 페이지를 만들어 보자.
단, 작성한 사람은 자신의 글을 수정 / 삭제가 가능해야 한다.

## 스택

- Spring Boot 3.2.1
- Spring Boot Data JPA
- SQLite
- Thymeleaf

## 실행

1. 본 Repository를 clone 받는다.
2. Intellij IDEA를 이용해 clone 받은 폴더를 연다.
3. [CommunityApplication.java](src/main/java/com/example/community/CommunityApplication.java)의 `main`을 실행한다.

SQLite를 사용하며, 테스트 데이터를 넣어주기 위한 `data.sql`가 첨부되어 있기 때문에
실행하면 바로 기능을 확인할 수 있다.

### 테스트 데이터를 사용하지 않는 경우

1. 한번 프로젝트를 실행하고 종료 (`ddl-auto` 옵션으로 테이블 자동 생성)
2. `spring.data.jpa.defer-datasource-initialization`와 `spring.sql.init.mode` 설정 제거
3. `spring.data.jpa.hibernate.ddl-auto`를 `none` 또는 `update`로 수정
4. 이후 정상적으로 프로젝트 실행

### JAR로 실행하는 경우 (Macos, Linux)

Java 17 버전 필요

터미널에서,
1. `./gradlew clean`
2. `./gradlew bootJar`
3. `cd build/lib` (폴더 이동)
4. `java -jar community-0.0.1-SNAPSHOT.jar`

### JAR로 실행하는 경우 (Macos, Linux)

Java 17 버전 필요

CMD에서,
1. `gradlew.bat clean`
2. `gradlew.bat bootJar`
3. `cd build/lib` (폴더 이동)
4. `java -jar community-0.0.1-SNAPSHOT.jar`

## 필수 기능

- [데이터 다루기](md/data.md)
- [게시판 기능](md/board.md)
- [게시글 기능](md/article.md)
- [댓글 기능](md/comment.md)


