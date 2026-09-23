import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import MatchForm from '../components/MatchForm';
import { createMatch, searchMatchFacilities } from '../api/matchApi';
import logo from '../assets/logo.png';
import '../css/match.css';
export function RequireMatchAuth({
  children
}) {
  // 로그인 페이지 담당자: 성공 후 location.state의 from/fromState를 읽고
  // navigate(from, { state: fromState, replace: true })로 돌아와야 AI 초안이 보존된다.
  // 현재 /login은 안내 문구만 있으므로 로그인 후 복귀 동작은 아직 연결되지 않았다.
  const location = useLocation();
  if (!localStorage.getItem('at')) {
    const from = location.pathname + location.search;
    // 로그인 완료 시 fromState도 복원하면 AI 초안을 유지한 채 생성 화면으로 돌아갈 수 있다.
    return <Navigate to={`/login?redirect=${encodeURIComponent(from)}`} state={{
      from,
      fromState: location.state
    }} replace />;
  }
  return children;
}
const MatchCreatePage = ({
  searchFacilities = searchMatchFacilities
}) => {
  const moveUrl = useNavigate();
  const location = useLocation();
  // /matches/new 직접 접속에는 aiDraft가 없으며 기존 빈 폼/기본값으로 동작한다.
  // 라우터 state는 공유 URL에 포함되지 않는다. 공유 가능한 초안이 필요하면 별도 저장/조회 기능을 구현한다.
  const aiDraft = location.state?.aiDraft;
  const submitHandler = payload => {
    // 실제 등록 API 호출 지점. AI 해석 완료/페이지 진입 시 호출하면 사용자 확인 없이 매치가 생성되므로
    // MatchForm의 검증을 통과한 수동 제출에서만 실행한다.
    return createMatch(payload).then(matchId => {
      moveUrl(`/matches/${matchId}`, {
        replace: true
      });
    }).catch(error => {
      if (error.response?.status === 401) {
        localStorage.removeItem('at');
        const from = location.pathname + location.search;
        moveUrl(`/login?redirect=${encodeURIComponent(from)}`, {
          state: {
            from,
            fromState: location.state
          },
          replace: true
        });
      }
      throw error;
    });
  };
  return (
    <div className="match-page">
        <header className="match-header">
            <div className="match-header-inner">
                <Link to="/" className="match-brand" aria-label="SPORTY 홈">
                    <img src={logo} alt="" width="107" height="32" />
                    <span>
                        <strong>SPORTY</strong>
                        <small>ATHLETIC MATCH PLATFORM</small>
                    </span>
                </Link>
                <Link to="/mypage" className="match-my-link">내 매치</Link>
            </div>
        </header>
        <main className="match-main">
            <nav className="match-breadcrumb" aria-label="현재 위치">
                <Link to="/">홈</Link>
                <span>/</span>
                <Link to="/mypage">매치 관리</Link>
                <span>/</span>
                <span aria-current="page">새 매치 개설</span>
            </nav>
            <h1>새로운 매치 개설하기</h1>
            <p className="match-intro">원하는 종목과 장소, 일정을 등록하고 함께 경기할 신뢰성 높은 스포츠 메이트를 매칭하세요.</p>
            {aiDraft && <aside className="match-ai-draft" aria-label="AI 매치 생성 요청">
                <strong>{aiDraft.interpreted ? 'AI가 입력한 조건을 확인해주세요.' : '입력한 요청을 가져왔습니다. 조건을 직접 입력해주세요.'}</strong>
                <p>{aiDraft.prompt}</p>
            </aside>}
            {/* 새 라우트 진입마다 key로 폼을 다시 생성해 새 초안을 적용한다. 일반 재렌더에서는 사용자 수정값을 유지한다.
                시설 상세에서 명시적으로 전달한 facility가 AI 추천 시설보다 우선한다. */}
            <MatchForm key={location.key} initialValues={aiDraft?.initialValues} onSubmit={submitHandler} selectedFacility={location.state?.facility || aiDraft?.facility} searchFacilities={searchFacilities} />
        </main>
        <footer className="match-footer">
            <img src={logo} alt="SPORTY" width="80" height="24" />
            <strong>SPORTY</strong>
            <span>© {new Date().getFullYear()} SPORTY. All rights reserved.</span>
        </footer>
    </div>
  );
};
export default MatchCreatePage;
