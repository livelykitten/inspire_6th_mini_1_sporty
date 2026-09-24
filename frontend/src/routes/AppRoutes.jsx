import { Route, Routes } from 'react-router-dom';
import MatchCreatePage, { RequireMatchAuth } from '../features/match/pages/MatchCreatePage';
import LoginPage from '../features/auth/pages/LoginPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<div>메인 페이지</div>} />

      {/* [USR-02] 로그인 성공 시 원래 요청한 내부 페이지 또는 메인으로 이동한다. */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<div>회원가입</div>} />
      <Route path="/mypage" element={<div>마이페이지</div>} />

      <Route path="/matches/new" element={<RequireMatchAuth><MatchCreatePage /></RequireMatchAuth>} />
      <Route path="/matches/:matchId" element={<div>매치 상세</div>} />
      <Route path="/matches/:matchId/edit" element={<div>매치 수정</div>} />

      <Route path="/facilities" element={<div>시설 검색</div>} />
      <Route path="/facilities/:serviceId" element={<div>시설 상세</div>} />

      <Route path="/profiles/:userId" element={<div>프로필 상세</div>} />

      <Route path="*" element={<div>페이지를 찾을 수 없습니다.</div>} />
    </Routes>
  );
}
