## 📁 폴더 구조 (`src/` 기준)

```
src/

  ├── api/        : Axios를 활용한 API 요청 함수들을 모아둔 폴더입니다.
  
  ├── component/  : 페이지에서 분리한 공통 컴포넌트 및 재사용 가능한 UI 요소들이 위치해 있습니다.
  
  ├── context/    : 토스트 메시지 전역 관리와 관련된 기능을 제공하는 컨텍스트와 훅이 포함돼 있습니다.
  
  ├── img/        : 프로젝트에서 사용하는 이미지 파일들을 보관합니다.
  
  ├── page/       : 실제 화면에 렌더링되는 주요 페이지 컴포넌트들이 위치한 폴더입니다.

  ├── redux/      : 사용자 인증 정보를 관리하는 리덕스 상태 및 액션, 스토어 설정 파일입니다.
  
  ├── style/      : styled-components로 작성한 스타일 정의 파일들이 있으며, 각 페이지와 연결됩니다.

  ├── types/      : 애플리케이션 전역에서 재사용되는 TypeScript 타입 정의들이 위치한 폴더입니다.
  
  └── util/       : 유틸 함수 및 공통 사용 데이터(지역 코드 등)들을 모아둔 폴더입니다.
```

<br/>

## 📦 패키지 설치 및 실행 방법

```bash
yarn install
yarn start
```

단, `.env` 파일이 있어야 정상적으로 작동합니다.

해당 파일은 보안사항으로 공개되지 않습니다.

<br/>

## 🧱 개발 환경

- **React** `19.0.0`

- **Styled-components** `6.1.15`

- **React Chartjs 2** `5.3.0`

- **Firebase** `11.3.1`

- **Kakao Maps SDK** `1.1.27`

- **React Quill** `2.0.0`

- **Quill Image Resize Module** `3.0.3`  

- **Swiper** `11.2.4`  

- **Uuid** `11.1.0`

- **Jwt Decode** `4.0.0`

- **React Datepicker** `8.1.0`
