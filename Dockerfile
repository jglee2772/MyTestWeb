# 멀티 스테이지 빌드
FROM gradle:8.5-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# 모든 파일 복사
COPY . .

# Gradle로 직접 빌드 (Wrapper 사용하지 않음)
RUN gradle build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
