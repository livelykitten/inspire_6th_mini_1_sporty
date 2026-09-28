import { Link } from 'react-router-dom';

// 표시 전용. DTO → 이 화면 모델 변환은 api/matchListApi.js에서 직접 구현하세요.
export default function MatchBrowseCard({ match, recommended = false, preview = false, onJoin, joiningId = null }) {
  return <article className={`ml-card ${recommended ? 'ml-card-recommended' : ''} ${match.closed ? 'ml-card-closed' : ''}`}>
    <div className="ml-card-tags"><span className="ml-tag">{match.sport || '종목 정보 없음'}</span><span className={`ml-status ${match.closed ? 'is-closed' : ''}`}>{match.status || '상태 정보 없음'}</span></div>
    <h3>{match.title}</h3>
    <p className="ml-location">⌖ {match.location || '장소 정보 없음'}</p>
    <div className="ml-card-meta"><span>{match.level || '실력 정보 없음'} · {match.gender || '성별 구성 정보 없음'}</span><span>{match.distance || '거리 정보 없음'}</span></div>
    <p className="ml-description">{match.description || '상세 안내가 없습니다.'}</p>
    <dl className="ml-card-facts"><div><dt>매치 일시</dt><dd>{match.schedule || '일정 정보 없음'}</dd></div><div><dt>취소 마감</dt><dd>{match.deadline || '정보 없음'}</dd></div></dl>
    <div className="ml-owner"><span><small>방장</small> {match.owner || '정보 없음'}</span><strong>{match.fee || '요금 정보 없음'}</strong></div>
    <div className="ml-card-bottom"><div className="ml-card-meta"><span>참가자 {match.participants || '정보 없음'}</span><span>{match.remaining}</span></div>
      {match.occupancy != null && <progress max="100" value={match.occupancy} aria-label="참가 인원 모집률" />}
      <div className="ml-card-actions"><button type="button" disabled={preview || match.closed || !onJoin || joiningId !== null} onClick={() => onJoin?.(match.id)}>{joiningId === match.id ? '참가 신청 중…' : match.closed ? '모집 마감' : '참가 신청하기'}</button>
        {!preview && <Link to={`/matches/${match.id}`} aria-label={`${match.title} 상세 보기`}>↗</Link>}
      </div>
    </div>
  </article>;
}
