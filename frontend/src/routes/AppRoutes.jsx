import { Route, Routes } from 'react-router-dom';
import MatchCreatePage, { RequireMatchAuth } from '../features/match/pages/MatchCreatePage';
import LoginPage from '../features/auth/pages/LoginPage';
import MatchEditPage from '../features/match/pages/MatchEditPage';
import MatchDetailPage from '../features/match/pages/MatchDetailPage';
import MatchDetailPreviewPage from '../features/match/pages/MatchDetailPreviewPage';
import SignUpPage from '../features/auth/pages/SignUpPage';
import MainPage from '../features/main/pages/MainPage';
import MatchListPage from '../features/match/pages/MatchListPage';
import MyPage from '../features/mypage/pages/MyPage';

export default function AppRoutes() {
  return (
    <Routes>
      {/* [AI-03] AI 매치 생성
          생성 해석 API 준비 후 main/api의 함수를 import해 <MainPage onGenerate={해석함수} />로 연결한다.
          함수 반환 형식은 features/match/utils/aiMatchDraft.js 참고. 현재는 원문을 담은 state.aiDraft 전달까지만 구현되어 있다.
          생성 담당자는 MatchCreatePage의 초안 읽기/폼 props 연결과 로그인 경유 시 초안 보존을 추가해야 한다. */}
      <Route path="/" element={<MainPage/>} />

      {/* [USR-02] 로그인 성공 시 원래 요청한 내부 페이지 또는 메인으로 이동한다. */}
      <Route path="/login" element={<LoginPage />} />
      {/* [USR-01] 회원가입 화면. 가입 성공 시 기존 /login 경로로 이동한다. */}
      <Route path="/signup" element={<SignUpPage />} />
      {/* [USR-06 / PR-01] 본인 정보 조회 및 프로필 수정 API 연결. */}
      <Route path="/mypage" element={<MyPage />} />
      {process.env.NODE_ENV === 'development' && <Route path="/mypage/preview" element={<MyPage preview />} />}

      <Route path="/matches/new" element={<RequireMatchAuth><MatchCreatePage /></RequireMatchAuth>} />
      {process.env.NODE_ENV === 'development' && (
        <Route path="/matches/preview" element={<MatchDetailPreviewPage />} />
          )}
          <Route path="/matches/:matchId" element={<MatchDetailPage />} />
      {/* [EM-02] 전체 운동 매칭 목록 조회 — 실제 목록 페이지로 교체하고 해당 페이지에서 조회 API를 호출한다. */}
      <Route path="/matches/:matchId/edit" element={<RequireMatchAuth><MatchEditPage /></RequireMatchAuth>} />
      {/* [EM-02] 목록 UI skeleton. MatchListPage에서 조회 로직 연결 후 preview=false 설정. */}
      <Route path="/matches/search" element={<MatchListPage />} />

      {/* [FC-01] 시설 검색 — 실제 시설 검색 페이지로 교체하고 URL의 query를 초기 검색어로 사용한다. */}
      <Route path="/facilities" element={<div>시설 검색</div>} />
      <Route path="/facilities/:serviceId" element={<div>시설 상세</div>} />

      <Route path="/profiles/:userId" element={<div>프로필 상세</div>} />

      <Route path="*" element={<div>페이지를 찾을 수 없습니다.</div>} />
    </Routes>
  );
}
