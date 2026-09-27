# 마이페이지 프론트

`/mypage`는 현재 `preview` 모드다. 예시 데이터를 편집하고 저장하면 화면에만 반영되며 새로고침하면 초기화된다. 실제 회원 정보/프로필 API를 호출하지 않는다.

## 파일별 담당 지점

- `pages/MyPage.jsx`: 조회, 입력, 취소, 저장 이벤트 및 화면. 각 state/핸들러에 용도 주석이 있다.
- `data/previewData.js`: 예시 회원 정보/프로필. 실제 로그인 사용자의 데이터가 아니다.
- `css/mypage.css`: Figma 49:2 배치와 컬러 가이드 2를 적용한 반응형 스타일.
- `assets/`: Figma 원본 아이콘.
- `src/routes/AppRoutes.jsx`: 실제 API 함수 연결 및 preview 제거 지점.

## 1. 회원정보·프로필 조회 연결

API 명세 확정 후 별도 API 모듈에 `loadMyPage({ signal })` 함수를 만들고 `<MyPage loadMyPage={loadMyPage} saveProfile={saveProfile} />`로 전달한다. 공통 `src/api/axios.js`를 사용한다. 경로/HTTP 메서드는 아직 정해지지 않아 임의 요청을 만들지 않았다.

함수는 Promise로 다음 객체를 반환한다. user와 profile이 별도 API라면 함수 안에서 조회하고 합쳐 반환한다. axios response 전체가 아닌 아래 데이터를 반환해야 한다.

```js
{
  user: { email: 'sporty@example.com', gender: 'MALE', status: 'ACTIVE', createdAt: '2026-09-01T10:00:00' },
  profile: { nickname: '성수동매치', district: 'SEONGDONG', preferenceSports: ['SOCCER'], imageUrl: null }
}
```

`signal`을 axios 요청 옵션에 전달한다. 회원 정보는 읽기 전용이며 비밀번호는 조회/표시하지 않는다. 프로필 조회값으로 input/select/checkbox를 채운다. 빈 종목은 `[]`, 기본 사진은 `null`로 정규화한다. live 모드에서는 토큰이 없거나 API 401이면 로그인으로 이동한다.

## 2. 프로필 수정 연결

`keyHandler`는 닉네임/자치구, `sportHandler`는 선호 종목, `imageHandler`는 사진 파일을 수정한다. 이 단계에서는 서버 요청이 없다. `saveHandler`가 검증 후 다음 형태로 연결 함수를 호출한다.

```js
saveProfile({ nickname, district, preferenceSports, imageUrl }, imageFile)
```

`imageFile`은 새로 선택한 File 또는 null이다. 새 파일이 있으면 업로드한 후 영구 이미지 URL로 저장하거나, 백엔드 명세에 따라 FormData를 사용한다. 기존 `imageUrl`보다 새 파일이 우선이다. 새 파일 없이 `imageUrl: null`이면 기본 이미지로 변경한다. 브라우저 blob URL을 서버로 보내지 않는다.

함수는 **저장이 완료된 전체 profile 객체**를 반환한다. 수정 응답이 204이면 저장 후 재조회하여 반환한다. 저장 실패는 reject해야 한다. 그래야 화면에 성공 표시가 나오지 않고 입력값이 유지된다. `DUPLICATE_NICKNAME` 오류 코드는 현재 회원가입과 동일하게 처리하며, 명세 변경 시 분기를 수정한다. 별도 중복 검사 API는 호출하지 않는다. 닉네임 1~50자 기준은 기존 회원가입 기준이며 수정 DTO 확정 시 맞춘다.

## 3. 취소·사진·선택지

`cancelHandler`는 마지막 조회/저장 시점으로 폼, 사진, 체크박스를 복구한다. 사진 선택은 JPG/PNG 10MB 이하만 허용한다. 화면 미리보기 URL은 정리한다. 자치구/운동 선택지는 기존 `auth/data/signUpOptions.js`를 재사용한다.

## 4. 로그아웃·회원탈퇴

live 모드에서는 기존 `[USR-03] LogoutButton`을 재사용한다. preview 모드에서는 실제 세션을 종료하지 않도록 비활성 상태다. 회원탈퇴는 이번 조회/프로필 편집 범위에 포함하지 않아 준비 중으로 표시한다. 추후 비밀번호 확인 모달 및 탈퇴 API를 연결한다.

프론트의 로그인 확인은 화면 이동용이다. 실제 본인 확인 및 수정 권한은 서버에서 검증해야 한다.
