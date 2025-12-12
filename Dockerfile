# Base Image: AWS에서 제공하는 OpenJDK 17 (Amazon Corretto)
FROM amazoncorretto:17

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 컨테이너로 복사
# (build/libs/*.jar 경로는 프로젝트 빌드 설정에 따라 다를 수 있으니 확인 필요)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Oracle Wallet을 저장할 디렉토리 생성 (선택 사항, 볼륨 마운트 시 자동 생성됨)
RUN mkdir -p /app/wallet

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]