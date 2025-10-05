# MyTestWeb

스프링부트를 사용한 웹 개발 테스트 프로젝트입니다.

## 기술 스택

- **Backend**: Spring Boot 3.2.0, Java 17
- **Template Engine**: Thymeleaf
- **Database**: H2 (인메모리)
- **Build Tool**: Gradle
- **IDE**: IntelliJ IDEA, Cursor

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/mytestweb/
│   │   ├── MyTestWebApplication.java
│   │   └── controller/
│   │       └── HomeController.java
│   └── resources/
│       ├── application.yml
│       ├── static/css/
│       │   └── style.css
│       └── templates/
│           ├── index.html
│           ├── hello.html
│           └── about.html
├── test/
build.gradle
settings.gradle
gradlew
gradlew.bat
```

## 실행 방법

1. 프로젝트 클론
```bash
git clone https://github.com/jglee2772/MyTestWeb.git
cd MyTestWeb
```

2. 애플리케이션 실행
```bash
./gradlew bootRun
```

또는 Windows에서:
```bash
gradlew.bat bootRun
```

3. 브라우저에서 접속
- 메인 페이지: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console

## 주요 기능

- 홈페이지 (/) - 메인 페이지
- 인사 페이지 (/hello) - 파라미터를 통한 동적 인사
- 소개 페이지 (/about) - 프로젝트 소개

## 개발 환경 설정

- Java 17 이상
- Gradle 8.5 이상 (또는 Gradle Wrapper 사용)
- IntelliJ IDEA 또는 Cursor

## 추가할 수 있는 기능들

- [ ] 사용자 인증/인가
- [ ] 데이터베이스 연동 (MySQL, PostgreSQL)
- [ ] REST API 개발
- [ ] 파일 업로드/다운로드
- [ ] 이메일 발송
- [ ] 실시간 채팅
- [ ] 게시판 기능
- [ ] 관리자 페이지
