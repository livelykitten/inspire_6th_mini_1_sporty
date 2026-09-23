import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AISearchBox from '../components/AISearchBox';
import AIConditionSummary from '../components/AIConditionSummary';
import MatchListSection from '../components/MatchListSection';
import { previewConditions, previewMatches } from '../data/previewData';
import api from '../../../api/axios';
import { toConditionSummary, toMatchCard } from '../api/aiMatchApi';
import { prepareAiMatchDraft } from '../../match/utils/aiMatchDraft';
import headerSearch from '../assets/header-search.svg';
import arrow from '../assets/arrow.svg';
import '../css/main.css';

/**
 * [메인 화면 연결 지점]
 * onSearch(query): 선택 사항. 기본 AI-02 대신 사용할 때 { conditions, matches } 화면 모델을 반환한다.
 * onGenerate(query): 생성 조건 해석 API를 연결할 자리. { initialValues, facility? }를 반환해야 한다.
 *   main/api에 해석 API 함수를 만든 뒤 routes/AppRoutes.jsx에서 <MainPage onGenerate={함수} />로 연결한다.
 * onEdit(): 조건 편집 UI를 여는 콜백. 편집 결과의 재검색·상태 반영 로직은 별도 구현이 필요하다.
 * onJoin(match): 카드의 화면 모델(id 포함)을 받는다. 참가 API 호출 및 오류/결과 표시를 구현해 연결한다.
 * 예시 제거 시 preview={false}만으로는 부족하다. matches={[]} conditions={[]}도 함께 전달한다.
 * 화면 스타일은 main/css/main.css, 예시 값은 main/data/previewData.js에서 수정한다.
 */
const MainPage = ({
  matches = previewMatches,
  conditions = previewConditions,
  onSearch,
  onGenerate,
  onEdit,
  onJoin,
  pending = false,
  preview = true
}) => {
  // [FC-01] string: 상단 시설 검색어. facilitySearchHandler가 URL의 query로 전달한다.
  const [facilityQuery, setFacilityQuery] = useState('');
  // [AI-02] null | { matches: 배열, conditions: 배열 }: 변환된 검색 응답. null은 아직 검색하지 않은 상태다.
  const [searchResults, setSearchResults] = useState(null);
  // [AI-02] 'idle' | 'loading' | 'success' | 'error': 예시/로딩/결과/오류 화면과 검색 버튼 상태를 결정한다.
  const [searchStatus, setSearchStatus] = useState('idle');
  // [AI-03] boolean: 생성 조건 해석 진행 여부. true이면 입력과 버튼을 잠그고 '조건 분석 중'을 표시한다.
  const [generating, setGenerating] = useState(false);
  // [AI-02][AI-03] ref<boolean>: 렌더링 전 연속 클릭도 차단하는 공통 잠금. 화면 표시용 state가 아니다.
  const searching = useRef(false);
  const visibleMatches = searchResults?.matches ?? matches;
  const visibleConditions = searchResults?.conditions ?? conditions;
  const isPreview = preview && searchStatus === 'idle';
  const moveUrl = useNavigate();

  // [AI-02] AI 매치 검색
  // 검색할 때만 AI-02를 호출한다. 예시 3개 제한은 서버에서 받은 결과 개수에는 적용하지 않는다.
  // 새 요청 시작 시 이전 조건과 카드를 함께 비우고, 성공 시 한 응답으로 함께 교체한다.
  const searchHandler = query => {
    if (searching.current) return;
    searching.current = true;
    setSearchStatus('loading');
    setSearchResults({
      matches: [],
      conditions: []
    });
    // 수업 방식: 페이지의 핸들러에서 요청 → 응답 데이터 → 상태 변경 순서로 처리한다.
    // onSearch는 테스트/외부 연결용이며, 일반 화면에서는 아래 api.post가 실행된다.
    const request = onSearch ? Promise.resolve().then(() => onSearch(query)) : api.post('/api/ai/matches/search', {
      query: query.trim()
    }).then(response => {
      const data = response.data;
      if (!Array.isArray(data?.matches)) throw new Error('매치 목록 응답 형식을 확인해주세요.');
      return {
        matches: data.matches.map(toMatchCard),
        conditions: toConditionSummary(data.conditions)
      };
    });
    return request.then(results => {
      if (!Array.isArray(results?.matches) || !Array.isArray(results?.conditions)) throw new Error('매치 검색 응답 형식을 확인해주세요.');
      setSearchResults(results);
      setSearchStatus('success');
    }).catch(error => {
      // 실패 시 예시 카드나 이전 검색 결과를 새 추천 결과로 보여주지 않는다.
      setSearchStatus('error');
      const message = error.response?.data?.message;
      if (typeof message === 'string' && message.trim()) throw new Error(message);
      if (error.response?.status === 401) throw new Error('로그인이 필요하거나 인증이 만료되었습니다. 다시 로그인해주세요.');
      if (error.response?.status === 500) throw new Error('추천 처리 중 오류가 발생했습니다. 잠시 후 다시 검색해주세요.');
      if (error.response || error.request) throw new Error('매치를 불러오지 못했습니다. 연결 상태를 확인하고 다시 시도해주세요.');
      throw error;
    }).finally(() => {
      searching.current = false;
    });
  };

  // [AI-03] AI 매치 생성
  // 생성 해석 API는 추후 onGenerate(query)에 연결한다. 검색 API나 매치 등록 API를 대신 호출하지 않는다.
  // 연결 함수는 HTTP 응답 전체가 아니라 prepareAiMatchDraft가 받는 본문 객체를 반환해야 한다.
  // 미연결 상태에서는 원문만 전달한다. 임시 정규식으로 자연어를 해석하거나 등록 API를 호출하지 않는다.
  const generateHandler = query => {
    if (searching.current) return;
    searching.current = true;
    setGenerating(true);
    return Promise.resolve().then(() => {
      return onGenerate ? onGenerate(query) : undefined;
    }).then(result => {
      if (onGenerate && result == null) throw new Error('매치 생성 조건 응답이 없습니다.');
      const aiDraft = prepareAiMatchDraft(query, result);
      // 다른 라우트에는 props를 직접 넘길 수 없어 state를 사용한다.
      // MatchCreatePage가 state.aiDraft를 읽어 MatchForm의 initialValues/selectedFacility props로 전달한다.
      moveUrl('/matches/new', {
        state: {
          aiDraft
        }
      });
    }).finally(() => {
      searching.current = false;
      setGenerating(false);
    });
  };

  // [FC-01] 시설 검색
  // 통합 검색은 시설 검색 화면으로 위임한다. 입력값은 URL에 보존해 이동 후에도 사용할 수 있다.
  // 시설 검색 페이지 구현 시 useSearchParams().get('query')로 읽어 초기 검색 조건에 사용한다.
  const facilitySearchHandler = event => {
    event.preventDefault();
    const query = facilityQuery.trim();
    moveUrl(query ? `/facilities?${new URLSearchParams({
      query
    })}` : '/facilities');
  };


  return (
    <div className="ms-page">
        <header className="ms-header">
            <div className="ms-container ms-header-inner">
                <Link className="ms-brand" to="/">
                    <span>S</span>
                    <strong>Sporty</strong>
                </Link>
                {/* 폼 제출을 사용해 검색 버튼 클릭과 Enter 입력이 같은 경로로 이동하도록 한다. */}
                <form className="ms-header-search" role="search" aria-label="체육시설 검색" onSubmit={facilitySearchHandler}>
                    <div className="ms-global-search">
                        <img src={headerSearch} width="16" height="16" alt="" />
                        <input type="search" aria-label="체육시설 검색" placeholder="체육시설 검색" value={facilityQuery} onChange={event => setFacilityQuery(event.target.value)} />
                    </div>
                    <button type="submit" className="ms-header-search-button">검색</button>
                </form>
            </div>
        </header>
        <main className="ms-container ms-main">
            <section className="ms-hero">
                <h1>자연어로 말하듯 검색하면, AI가 딱 맞는 매치 조건을 찾아드립니다</h1>
                <p>종목, 시간, 지역, 실력을 알려주세요. 나에게 맞는 매치를 한눈에 확인하세요.</p>
            </section>
            {/* 최초에는 예시 3개를 표시하고, AI 검색 성공 후에는 서버 응답으로 교체한다. */}
            {isPreview && <p className="ms-preview-note">예시 매치 3개입니다. AI 검색을 누르면 실제 검색 결과로 바뀝니다.</p>}
            <AISearchBox onSearch={searchHandler} onGenerate={generateHandler} pending={pending || generating || searchStatus === 'loading'} generating={generating} />
            {/* 조건과 매치 목록은 같은 응답으로 함께 갱신해 서로 다른 검색 결과가 섞이지 않게 한다. */}
            {searchStatus !== 'loading' && searchStatus !== 'error' && visibleConditions.length > 0 && <AIConditionSummary conditions={visibleConditions} onEdit={onEdit} />}
            {/* 메인에서는 추천 매치를 보여주고, 전체 매치 탐색은 아래 링크로 별도 페이지에 연결한다. */}
            {searchStatus === 'loading' ? <p role="status">조건에 맞는 매치를 찾고 있습니다.</p> : searchStatus !== 'error' && <MatchListSection matches={visibleMatches} onJoin={onJoin} />}
        </main>
        <footer className="ms-footer">
            <div className="ms-container ms-footer-inner">
                <div>
                    <p><strong>Sporty</strong><span className="ms-divider">|</span>함께 즐기는 스포츠, 함께 만드는 매치</p>
                    <p className="ms-copyright">© 2026 Sporty. All rights reserved.</p>
                </div>
                <div className="ms-footer-labels">
                    <span>서비스 이용약관</span>
                    <span>개인정보 처리방침</span>
                    <span>고객지원센터</span>
                </div>
            </div>
        </footer>
        {/* [EM-02] 전체 운동 매칭 목록 조회
            여기서는 목록 화면으로 이동만 한다. 담당자는 /matches/search 페이지에서 전체 목록 API를 연결한다. */}
        <Link className="ms-floating" to="/matches/search">
            {searchStatus === 'loading' || searchStatus === 'error' ? '전체 매치 목록 보러가기' : `전체 ${visibleMatches.length}건 매치 목록 보러가기`}
            <img src={arrow} width="10" height="4" alt="" />
        </Link>
    </div>
  );
};
export default MainPage;
