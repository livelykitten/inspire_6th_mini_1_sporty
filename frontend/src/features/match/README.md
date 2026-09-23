# 매치 생성 폼 담당자 안내

전체 기능 흐름은 [메인 기능별 안내](../main/README.md)에 있습니다. 기존 기능 ID 중 AI-03은 AI 해석/초안 준비를 뜻하며, 실제 매치 등록 API의 기능 ID는 아직 지정되지 않았습니다.

## [AI-03] AI 초안 수신 및 자동 입력

1. MainPage.generateHandler가 해석 결과를 prepareAiMatchDraft에 넣고 `/matches/new`의 `state.aiDraft`로 넘깁니다. MatchCreatePage는 해당 값을 읽어 MatchForm에 initialValues와 selectedFacility props로 전달합니다.
2. 초기 값은 문자열 sportType/skillLevel/startAt/endAt/title/description, 양의 정수 maxParticipant입니다. 시설은 양의 정수 serviceId와 문자열 name이 있어야 합니다. AI 입력이 없으면 일반 생성 화면으로 동작합니다.
3. MatchForm의 values(object)가 편집할 전체 값을 보관합니다. initialMatchValues가 startAt/endAt 문자열을 date/startTime/endDate/endTime으로 나눕니다. 폼 기본값은 풋살, 초급, 12명이고 미제공 텍스트/일정은 비어 있습니다.
4. update(name, value)는 해당 값을 수정하고 기존 필드 오류를 지웁니다. changeSport는 종목만 변경합니다. AI 초안은 최초 마운트 시 적용하므로 새 초안을 전달할 때 부모 key 변경이 필요합니다. 현재 MatchCreatePage는 location.key를 사용합니다.
5. 자동 입력 항목을 늘릴 담당자는 aiMatchDraft.js의 허용 목록 → matchValidation.js의 초기값/검증/요청 변환 → MatchForm 입력창 순서로 함께 수정합니다. 현재 성별/지역구 독립 입력은 없습니다.
6. RequireMatchAuth가 로그인 이동 시 from과 fromState를 보존합니다. 로그인 성공 후 내부 경로로 복귀하면서 fromState도 전달해야 초안이 유지됩니다.

## [FC-01] 생성 폼의 시설 검색 및 선택

1. query(문자열)는 시설명, region(문자열)은 지역 필터입니다. search()가 searchFacilities({ query, region })를 호출합니다. 기본 함수는 matchApi.js의 searchMatchFacilities이며 GET /api/services를 사용합니다.
2. 서버 시설 배열은 serviceId(정수), serviceName(문자열), region/locationName(선택 문자열)을 가정합니다. toFacilityOption이 화면용 name으로 변환합니다. 최종 DTO가 다르면 이 함수에서 수정합니다.
3. results(배열)는 후보 목록, facility(객체/null)는 선택한 시설입니다. 선택 시 values.serviceId에 실제 ID를 저장합니다. 선택 해제 시 facility와 serviceId를 비웁니다. 종목은 유지합니다.
4. searching(boolean)은 검색 진행 표시, searchMessage(문자열)는 빈 결과/실패 안내입니다. searchVersion(ref 숫자)은 늦게 온 이전 응답이 최신 목록을 덮지 않도록 합니다.

## [AI-03 후속 / 등록 기능 ID 미확정] 최종 매치 등록

1. 사용자가 완료 버튼을 누르면 MatchForm.submit(event)이 validateMatch(values)를 실행합니다. 오류는 errors(object)에 필드명별 문자열로 저장합니다. 제목 1~50자, 설명 1~500자, 시설 ID와 모집 인원은 양의 정수, 필수 일정/종료가 시작보다 늦은지, 실력 enum을 검사합니다.
2. 검증 성공 시 toMatchPayload가 아래 본문을 만듭니다. 모집 인원 입력값은 폼에서 문자열로 바뀔 수 있어 전송 전에 숫자로 변환합니다. 종료 날짜가 비어 있으면 시작 날짜를 사용합니다.

| 요청 필드 | 타입 | 내용 |
| --- | --- | --- |
| serviceId | 양의 정수 | 실제 시설 ID |
| title | 문자열 | 매치 제목 |
| description | 문자열 | 상세 안내 |
| startAt | 날짜·시간 문자열 | YYYY-MM-DDTHH:mm |
| endAt | 날짜·시간 문자열 | YYYY-MM-DDTHH:mm |
| maxParticipant | 양의 정수 | 방장을 포함한 최대 인원 |
| skillLevel | enum 문자열 | BEGINNER / INTERMEDIATE / ADVANCED |
| sportType | enum 문자열 | FUTSAL / TENNIS 등 종목 코드 |

3. onSubmit(payload) → MatchCreatePage.submitHandler → matchApi.createMatch 순서로 POST /api/matches를 호출합니다. 응답은 양의 정수 또는 `{ matchId: 양의 정수 }`를 지원합니다. 성공 시 `/matches/{matchId}`로 이동합니다.
4. pending(boolean)은 폼/버튼 잠금, submitting(ref boolean)은 중복 제출 차단, submitError(문자열)는 등록 실패 안내입니다. formRef는 검증 실패 시 첫 오류 입력칸으로 포커스를 이동할 때 사용합니다.
5. 401이면 토큰을 지우고 로그인으로 이동합니다. from/fromState에는 진입 당시 라우트 상태를 전달합니다. 사용자가 이후 수정한 values까지 임시 저장하는 기능은 현재 없습니다. 해당 보존이 필요하면 별도 구현해야 합니다.
6. 담당자는 등록 DTO/응답 ID 형식을 matchApi.js에서 확인하고 성공/검증 실패/401/중복 클릭을 확인합니다. AI 해석 완료 시 submitHandler를 자동 호출하지 않습니다.
