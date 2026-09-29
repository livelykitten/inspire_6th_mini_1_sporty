import { Link } from 'react-router-dom';
import MatchListFilters from '../components/MatchListFilters';
import MatchBrowseCard from '../components/MatchBrowseCard';
import { previewMatches, previewProfile, previewRecommendations } from '../data/matchListPreview';
import logo from '../assets/logo.png';
import searchIcon from '../../../components/layout/assets/header-search.svg';
import '../css/match.css';
import '../css/matchList.css';
import useMatchList from '../hooks/useMatchList';

const SPORTS = [['', '전체 매치'], ['FUTSAL', '⚽ 풋살'], ['TENNIS', '🎾 테니스'], ['SOCCER', '⚽ 축구'], ['BADMINTON', '배드민턴'], ['BASKETBALL', '농구'], ['RUNNING', '러닝 크루']];

// 실제 라우트는 조회/이벤트 훅을 사용하고, 표시 컴포넌트는 독립적으로 테스트한다.
export default function MatchListPage() {
  const model = useMatchList();
  return <MatchListView {...model} />;
}

export function MatchListView({
  preview = false, matches = [], recommendations = [], profile, profileStatus = 'idle', totalCount,
  filters = {}, sort = 'default', recommendationSort = 'default', status = 'idle', recommendationStatus = 'idle',
  hasMore = false, listError = '', recommendationError = '', joinMessage = '', joiningId = null,
  onSportChange, onFilterChange, onApply, onReset, onSort, onRecommendationSort,
  onJoin, onLoadMore, onRetry, onRecommendationRetry, onFacilitySearch,
}) {
  const visibleMatches = preview ? previewMatches : matches;
  const visibleRecommendations = preview ? previewRecommendations : recommendations;
  const visibleProfile = preview ? previewProfile : profile;

  return <div className="ml-page">
    <header className="ml-header"><div className="ml-shell">
      <div className="ml-header-top"><Link to="/" aria-label="SPORTY 홈"><img src={logo} width="107" height="32" alt="SPORTY" /></Link>
        {/* FC-01: 검색어 → /facilities?query=... 이동 콜백. 실제 검색 처리는 시설 화면에서 구현. */}
        <div className="ml-global-search"><img src={searchIcon} width="16" height="16" alt="" /><input aria-label="체육시설 검색" placeholder="지역명, 체육시설, 서비스명 통합 검색..." disabled={!onFacilitySearch} onKeyDown={event => { if (event.key === 'Enter') onFacilitySearch?.(event.currentTarget.value); }} /><span>시설 검색</span></div>
        <Link className="ml-account" to="/mypage">{visibleProfile?.avatar && <img className="ml-avatar" src={visibleProfile.avatar} alt="" />}<span><strong>{visibleProfile?.nickname || '마이페이지'}</strong><small>{visibleProfile?.region || '내 프로필 확인'}</small></span></Link>
      </div>
      <nav className="ml-nav" aria-label="운동 종목">{SPORTS.map(([value, label]) => <button key={value} type="button" aria-pressed={(filters.sportType || '') === value} disabled={!onSportChange} onClick={() => onSportChange?.(value)}>{label}</button>)}<span>{visibleProfile?.region || '지역을 선택해주세요'}</span></nav>
    </div></header>
    <main className="ml-shell ml-layout">
      <MatchListFilters profile={visibleProfile} profileStatus={profileStatus} filters={filters} onFilterChange={onFilterChange} onApply={onApply} onReset={onReset} />
      <div className="ml-content">
        {joinMessage && <p role="status">{joinMessage}</p>}
        {preview && <p className="ml-preview" role="note">화면 미리보기용 예시입니다. 실제 모집 정보가 아니며 참가 신청은 제공하지 않습니다.</p>}
        <section className="ml-recommendations" aria-labelledby="ml-recommendation-title" aria-busy={recommendationStatus === 'loading'}>
          <div className="ml-radar">✧ PROFILE MATCH RADAR</div>
          <div className="ml-section-heading"><h1 id="ml-recommendation-title">회원 맞춤 추천 매치</h1><label>정렬 <select aria-label="추천 매치 정렬" value={recommendationSort} disabled={!onRecommendationSort} onChange={event => onRecommendationSort?.(event.target.value)}><option value="default">기본순</option><option value="startAt">시간순</option></select></label></div>
          <p className="ml-subtitle">선호 종목과 활동 지역을 바탕으로 나에게 맞는 매치를 찾아보세요.</p>
          {recommendationStatus === 'loading' ? <p className="ml-empty" role="status">추천 매치를 찾고 있습니다.</p> : recommendationStatus === 'error' ? <div className="ml-empty" role="alert">{recommendationError || '추천 매치를 불러오지 못했습니다.'} <button type="button" disabled={!onRecommendationRetry} onClick={onRecommendationRetry}>다시 시도</button></div> : visibleRecommendations.length ? <div className="ml-grid">{visibleRecommendations.map(match => <MatchBrowseCard key={match.id} match={match} recommended preview={preview} onJoin={onJoin} joiningId={joiningId} />)}</div> : <p className="ml-empty">{recommendationError || '추천 매치가 없습니다.'}</p>}
        </section>
        <section className="ml-list" aria-labelledby="ml-list-title" aria-busy={status === 'loading'}>
          <div className="ml-section-heading"><h2 id="ml-list-title">⚡ 실시간 생활체육 매치 목록 <small>{totalCount ?? visibleMatches.length}개 매치</small></h2><div className="ml-sort" role="group" aria-label="목록 정렬">{[['default', '기본순'], ['startAt', '경기 시작순']].map(([value, label]) => <button type="button" key={value} aria-pressed={sort === value} disabled={!onSort} onClick={() => onSort?.(value)}>{label}</button>)}</div></div>
          {status === 'loading' ? <p className="ml-empty" role="status">매치 목록을 불러오고 있습니다.</p> : status === 'error' ? <div className="ml-empty" role="alert">{listError || '매치 목록을 불러오지 못했습니다.'} <button type="button" disabled={!onRetry} onClick={onRetry}>다시 시도</button></div> : visibleMatches.length ? <div className="ml-grid">{visibleMatches.map(match => <MatchBrowseCard key={match.id} match={match} preview={preview} onJoin={onJoin} joiningId={joiningId} />)}</div> : <p className="ml-empty">조건에 맞는 매치가 없습니다. 검색 조건을 변경해주세요.</p>}
          {(preview || hasMore) && <div className="ml-more"><button type="button" disabled={preview || !onLoadMore || status === 'loading' || status === 'error'} onClick={onLoadMore}>더 많은 생활체육 매치 탐색하기 ⌄</button></div>}
        </section>
      </div>
    </main>
    <footer className="ml-footer"><div className="ml-shell"><div><img src={logo} width="80" height="24" alt="SPORTY" /><span>© 2026 SPORTY. 함께 즐기는 생활체육 매칭 플랫폼</span></div><div><span>이용약관</span><span>개인정보처리방침</span><span>공공체육시설 API 연동 안내</span></div></div></footer>
  </div>;
}
