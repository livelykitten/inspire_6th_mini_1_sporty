import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

// [내 매치 목록] 전용 API가 준비되면 loadMyMatches({ signal })를 MyPage에 전달한다.
// [{ id, title, startAt, location, role: 'OWNER' | 'PARTICIPANT' }]로 응답을 변환한다.
// 전체 공개 목록을 임의로 내 매치로 표시하지 않는다.
export default function MyMatches({ loadMyMatches, onUnauthorized }) {
  // 내 목록의 조회 결과/진행 상태/재시도 횟수. 수정 폼의 state와 분리한다.
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(Boolean(loadMyMatches));
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    if (!loadMyMatches) return;
    const controller = new AbortController();
    setLoading(true); setError('');
    Promise.resolve().then(() => loadMyMatches({ signal: controller.signal })).then(data => {
      if (!controller.signal.aborted) setMatches(data);
    }).catch(requestError => {
      if (controller.signal.aborted) return;
      if (requestError.response?.status === 401) onUnauthorized();
      else setError('내 매치 목록을 불러오지 못했습니다.');
    }).finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [loadMyMatches, onUnauthorized, retry]);

  return <section className="mypage-panel mypage-section" aria-labelledby="my-matches-title">
    <h1 id="my-matches-title">내 매치 목록</h1><p className="mypage-list-description">내가 만들거나 참여한 매치를 확인하세요.</p>
    {!loadMyMatches ? <p className="mypage-empty">내 매치 목록 조회 기능을 준비 중입니다.</p>
      : loading ? <p role="status">매치 목록을 불러오는 중입니다.</p>
      : error ? <><p role="alert" className="mypage-error">{error}</p><button type="button" onClick={() => setRetry(value => value + 1)}>다시 시도</button></>
      : matches.length === 0 ? <p className="mypage-empty">아직 만들거나 참여한 매치가 없습니다.</p>
      : <ul className="mypage-match-list">{matches.map(match => <li key={match.id}>
        <span className="mypage-status">{match.role === 'OWNER' ? '내가 만든 매치' : '참여한 매치'}</span>
        <h2><Link to={`/matches/${match.id}`}>{match.title}</Link></h2>
        <p>{match.startAt ? new Date(match.startAt).toLocaleString('ko-KR') : '일정 미정'}</p><p>{match.location || '장소 미정'}</p>
      </li>)}</ul>}
  </section>;
}
