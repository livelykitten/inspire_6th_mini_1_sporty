# FC-01 체육시설 목록

`/facilities`에서 `GET /api/services`로 공공체육시설 목록을 조회합니다.

- 헤더 검색어는 URL `query`로 전달되고, 백엔드 `serviceName` 조건으로 사용합니다.
- 지역은 회원가입과 같은 자치구 한글명(`성동구`)을 보냅니다.
- 종목 칩은 서울시 소분류명(`테니스장`)을 `serviceType`으로 보냅니다.
- 거리 정렬/표시는 구현하지 않습니다.
- `상세보기`는 `/facilities/:serviceId`로 이동합니다. 상세 API는 FC-02입니다.
- `매치 생성`은 `/matches/new`로 시설 정보를 state로 넘깁니다.
