# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# --- 의존성 레이어 (자주 안 바뀜) ---
COPY gradlew .
RUN chmod +x ./gradlew
COPY gradle gradle
COPY build.gradle .

# 2. 의존성 다운로드 및 캐시 (BuildKit 캐시 마운트 적용)
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies

# --- 소스 코드 레이어 (자주 바뀜) ---
COPY src src
RUN --mount=type=cache,target=/root/.gradle ./gradlew build -x test -x asciidoctor

# 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]