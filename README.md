## 📁 폴더 구조 (`src/main/java/com/springboot/final_back` 기준)

```
src/main/java/com/springboot/final_back/

  ├── config/       : 전역 설정 파일이 위치하며, 프로젝트 전반의 설정을 담당합니다.
  
  ├── constant/     : 고정된 값을 상수로 정의하여 재사용성을 높이는 폴더입니다.
  
  ├── controller/   : 클라이언트의 요청을 처리하는 REST API 엔드포인트들이 위치합니다.
  
  ├── dto/          : 요청(Request) 및 응답(Response)에 사용되는 데이터 전송 객체들이 정의됩니다.
  
  ├── entity/       : 데이터베이스 테이블과 매핑되는 JPA 엔티티 클래스들이 정의됩니다.

  ├── exception/    : 사용자 정의 예외 클래스를 포함하며, 오류 상황에 대한 처리와 응답을 담당합니다.
  
  ├── jwt/          : JWT 토큰의 생성 및 검증, 필터 처리를 담당하는 클래스들이 위치합니다.
    
  ├── repository/   : JPA를 사용한 DB 접근 인터페이스 계층입니다.
    
  ├── schedule/     : 주기적인 작업(Cron)을 처리하는 스케줄링 클래스들이 위치합니다.
    
  ├── security/     : 인증·인가 관련 설정 및 보안 유틸 클래스들이 위치합니다.

  └── service/      : 비즈니스 로직을 처리하는 서비스 계층입니다.
```

<br/>

## 📦 의존성 설치 및 실행 방법

```bash
./gradlew build
java -jar build/libs/final_back-0.0.1-SNAPSHOT.jar
```

>Swagger UI 확인: http://localhost:8111/swagger-ui.html

<br/>

단, `.env` 파일이 있어야 정상적으로 작동합니다.

해당 파일은 보안사항으로 공개되지 않습니다.

<br/>

## 🧱 개발 환경

- **Java** `17`

- **Spring Boot** `2.7.17`

- **Lombok** `1.18.24`

- **JWT** `0.11.2`

- **JSoup** `1.17.2`

- **Elasticsearch** `7.17.12`

- **Swagger** `2.9.2`

- **Gradle** `8.12.1`
