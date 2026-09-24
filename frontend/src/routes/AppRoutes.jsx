import { Route, Routes } from 'react-router-dom';
import MatchCreatePage, { RequireMatchAuth } from '../features/match/pages/MatchCreatePage';
import SignUpPage from '../features/auth/pages/SignUpPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<div>메인 페이지</div>} />

      <Route path="/login" element={<div>로그인</div>} />
      {/* [USR-01] 회원가입 화면. 가입 성공 시 기존 /login 경로로 이동한다. */}
      <Route path="/signup" element={<SignUpPage />} />
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
