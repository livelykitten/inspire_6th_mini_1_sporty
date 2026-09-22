import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import MatchForm from '../components/MatchForm';
import { createMatch, searchMatchFacilities } from '../api/matchApi';
import logo from '../assets/logo.png';
import '../css/match.css';

export function RequireMatchAuth({ children }) {
  const location = useLocation();
  if (!localStorage.getItem('at')) {
    const from = location.pathname + location.search;
    return <Navigate to={`/login?redirect=${encodeURIComponent(from)}`} state={{ from }} replace />;
  }
  return children;
}

export default function MatchCreatePage({ searchFacilities = searchMatchFacilities }) {
  const navigate = useNavigate();
  const location = useLocation();

  async function handleSubmit(payload) {
    try {
      const matchId = await createMatch(payload);
      navigate(`/matches/${matchId}`, { replace: true });
    } catch (error) {
      if (error.response?.status === 401) {
        localStorage.removeItem('at');
        const from = location.pathname + location.search;
        navigate(`/login?redirect=${encodeURIComponent(from)}`, { state: { from }, replace: true });
      }
      throw error;
    }
  }

  return (
    <div className="match-page">
      <header className="match-header">
        <div className="match-header-inner">
          <Link to="/" className="match-brand" aria-label="SPORTY 홈">
            <img src={logo} alt="" width="107" height="32" />
            <span><strong>SPORTY</strong><small>ATHLETIC MATCH PLATFORM</small></span>
          </Link>
          <Link to="/mypage" className="match-my-link">내 매치</Link>
        </div>
      </header>
      <main className="match-main">
        <nav className="match-breadcrumb" aria-label="현재 위치">
          <Link to="/">홈</Link><span>/</span><Link to="/mypage">매치 관리</Link><span>/</span><span aria-current="page">새 매치 개설</span>
        </nav>
        <h1>새로운 매치 개설하기</h1>
        <p className="match-intro">원하는 종목과 장소, 일정을 등록하고 함께 경기할 신뢰성 높은 스포츠 메이트를 매칭하세요.</p>
        <MatchForm onSubmit={handleSubmit} selectedFacility={location.state?.facility} searchFacilities={searchFacilities} />
      </main>
      <footer className="match-footer"><img src={logo} alt="SPORTY" width="80" height="24" /><strong>SPORTY</strong><span>© {new Date().getFullYear()} SPORTY. All rights reserved.</span></footer>
    </div>
  );
}
