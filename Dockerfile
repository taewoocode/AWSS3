# Amazon Corretto 19을 사용하는 Docker 이미지 설정
FROM amazoncorretto:19

# 작업 디렉토리 설정
WORKDIR /app

# 호스트의 프로젝트 디렉토리에서 필요한 파일들을 컨테이너의 /app 디렉토리로 복사
# 이때, .gradle과 build 폴더는 제외하여 복사 속도를 개선하고 이미지 크기를 줄일 수 있습니다.
COPY . /app

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 설치 및 프로젝트 빌드 (테스트 제외)
RUN ./gradlew build -x test

# 애플리케이션 실행
ENTRYPOINT ["./gradlew", "bootRun"]

# 컨테이너 외부로 노출할 포트 설정
EXPOSE 9090

