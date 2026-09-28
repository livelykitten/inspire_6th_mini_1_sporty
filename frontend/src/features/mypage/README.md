# 마이페이지

`/mypage`는 실제 API를 사용한다. 로그인하지 않으면 `/login`으로 이동하고 로그인 후 돌아온다. `/mypage/preview`는 개발 환경 전용 예시 화면이며 탈퇴/로그아웃은 비활성이다.

## 연결된 기능

- [USR-06] `api/myPageApi.js`의 loadMyPage: GET /api/users/me. 평면 응답을 user(이메일·성별)와 profile(닉네임·자치구·선호 종목)로 나눈다. 응답에 없는 가입일/상태는 표시하지 않는다.
- [PR-01] saveProfile: PUT /api/profiles/me. `{ nickname, district, sportTypes }` 전송, 응답의 preferenceSports를 폼에 반영한다. 사진 업로드/기본 이미지 버튼/이미지 요청 필드는 제거했다.
- [USR-03] 기존 auth/components/LogoutButton 재사용. POST /api/auth/logout 후 세션 정리.
- [USR-04] components/WithdrawalButton: 비밀번호 확인 dialog에서 DELETE /api/users/me에 `{ password }` 전송. 204 성공 후에만 at/rt/userId를 지우고 로그인 화면으로 이동. 실패 시 세션 유지. 비밀번호는 모달 종료 시 초기화한다.

## 내 정보 / 내 매치 목록

MyPage의 activeTab이 왼쪽 메뉴를 관리한다. 탭을 바꿔도 수정 중인 폼은 유지한다. 취소는 마지막 저장/조회 값으로 복구한다.

내 매치 목록은 생성·참여 모두 표시하도록 components/MyMatches.jsx에 준비했다. 아직 전용 API가 없어 현재는 조회 준비 중 안내가 나온다. 전체 공개 목록으로 대체하거나 임의 경로를 호출하지 않는다.

API 구현 후 loadMyMatches({ signal }) 함수를 만들어 MyPage props로 전달한다. 반환 형식:

```js
[{ id: 1, title: '주말 축구', startAt: '2026-10-01T18:00:00', location: '성동구 운동장', role: 'OWNER' }]
```

role은 OWNER(생성) 또는 PARTICIPANT(참여)이며, 목록 항목은 /matches/:id 상세로 이동한다. 실제 응답 필드가 다르면 연결 함수에서 변환한다. AbortSignal을 axios에 전달하고 실패 시 reject한다. 빈 배열은 참여/생성한 매치가 없다는 안내로 표시한다.

## 실행 시 주의

현재 프론트 브랜치의 백엔드가 오래된 경우 내 정보/탈퇴/수정 API가 없을 수 있다. 해당 API가 포함된 백엔드를 실행해야 실제 연동이 동작한다. 이번 작업에서는 백엔드 브랜치를 병합하거나 서버를 재시작하지 않았다.
