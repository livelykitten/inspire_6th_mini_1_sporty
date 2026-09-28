import verified from '../assets/list/imgContainer.svg';
import sport from '../assets/list/imgContainer1.svg';
import pin from '../assets/list/imgContainer2.svg';
import role from '../assets/list/imgContainer3.svg';
import filter from '../assets/list/imgContainer4.svg';
import region from '../assets/list/imgContainer5.svg';
import search from '../assets/list/imgContainer6.svg';
import link from '../assets/list/imgContainer7.svg';
import chevron from '../assets/list/imgImage.svg';

// 상태 관리/검색은 부모가 담당. 미연결 버튼은 비활성화하여 검색 성공을 가장하지 않습니다.
export default function MatchListFilters({ profile, filters = {}, onFilterChange, onApply, onReset }) {
  return <aside className="ml-sidebar" aria-label="프로필과 매치 필터">
    <section className="ml-panel ml-profile">
      <div className="ml-profile-heading">
        {profile?.avatar && <img className="ml-avatar" src={profile.avatar} alt="" />}
        <div><strong>{profile?.nickname || '로그인 후 확인해주세요'} {profile && <img src={verified} alt="" />}</strong><p>{profile?.name || '나에게 맞는 매치를 찾아보세요'}</p></div>
        {profile && <span className="ml-status">ACTIVE</span>}
      </div>
      <dl>{[[sport, '선호 종목', profile?.sports], [pin, '활동 권역', profile?.region], [role, '참여 역할', profile?.roles]].map(([icon, label, value]) => <div key={label}><dt><img src={icon} alt="" /> {label}</dt><dd>{value || '정보 없음'}</dd></div>)}</dl>
    </section>
    <section className="ml-panel ml-filters">
      <div className="ml-filter-heading"><h2><img src={filter} alt="" /> 매치 필터</h2><button type="button" disabled={!onReset} onClick={onReset}>전체 초기화</button></div>
      <fieldset><legend>매치 상태</legend><div className="ml-segments">{[['', '전체'], ['RECRUITING', '모집중'], ['CLOSED', '마감']].map(([value, label]) => <button key={value} type="button" aria-pressed={(filters.status || '') === value} disabled={!onFilterChange} onClick={() => onFilterChange?.('status', value)}>{label}</button>)}</div></fieldset>
      <fieldset><legend><img src={region} alt="" /> 지역 <small>(준비 중)</small></legend><div className="ml-region-selects">
        <label><span className="match-sr-only">시·도</span><select disabled defaultValue="서울특별시"><option>서울특별시</option></select><img src={chevron} alt="" /></label>
        <label><span className="match-sr-only">자치구</span><select value={filters.region || ''} disabled onChange={event => onFilterChange?.('region', event.target.value)}><option value="">전체 지역</option>{['성동구', '송파구', '마포구', '강남구', '광진구'].map(value => <option key={value}>{value}</option>)}</select><img src={chevron} alt="" /></label>
      </div></fieldset>
      <fieldset><legend>체육서비스 요금 <small>(준비 중)</small></legend><div className="ml-segments ml-fees">{[['', '전체'], ['Y', '무료'], ['N', '유료 대관']].map(([value, label]) => <button type="button" key={value} aria-pressed={(filters.isFree || '') === value} disabled onClick={() => onFilterChange?.('isFree', value)}>{label}</button>)}</div></fieldset>
      <label className="ml-preference"><span>내 선호 종목만 보기<small>프로필 연동 준비 중</small></span><input type="checkbox" role="switch" checked={!!filters.preferredOnly} disabled onChange={event => onFilterChange?.('preferredOnly', event.target.checked)} /></label>
      <button className="ml-primary" type="button" disabled={!onApply} onClick={onApply}><img src={search} alt="" /> 조건 검색 적용</button>
    </section>
    <aside className="ml-notice"><p><img src={link} alt="" /> 공공체육시설 공식예약 연동</p><small>시설별 대관 정보와 예약 가능 여부는 공식 예약 페이지에서 확인해주세요.</small></aside>
  </aside>;
}
