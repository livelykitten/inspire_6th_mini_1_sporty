import { useRef, useState } from 'react';
import { initialMatchValues, SKILL_LEVELS, toMatchPayload, validateMatch } from '../utils/matchValidation';
import futsal from '../assets/futsal.svg';
import tennis from '../assets/tennis.svg';
import badminton from '../assets/badminton.svg';
import basketball from '../assets/basketball.svg';
import running from '../assets/running.svg';
import soccer from '../assets/soccer.svg';
import baseball from '../assets/baseball.svg';
import tableTennis from '../assets/table-tennis.svg';
import volleyball from '../assets/volleyball.svg';
import swimming from '../assets/swimming.svg';
import submitIcon from '../assets/submit.svg';
import '../css/match.css';

const SPORTS = [
  ['SOCCER', '축구', '필드/야외', soccer],
  ['FUTSAL', '풋살', '인기 종목', futsal],
  ['BASKETBALL', '농구', '3vs3 / 5vs5', basketball],
  ['BASEBALL', '야구', '야구장', baseball],
  ['TENNIS', '테니스', '단/복식', tennis],
  ['BADMINTON', '배드민턴', '실내 체육관', badminton],
  ['TABLE_TENNIS', '탁구', '단/복식', tableTennis],
  ['VOLLEYBALL', '배구', '실내/야외', volleyball],
  ['SWIMMING', '수영', '수영장', swimming],
  ['RUNNING', '러닝', '트랙/야외', running],
];

function Section({ number, title, description, children }) {
  return <section className="match-section" aria-labelledby={`match-section-${number}`}>
    <div className="match-section-heading"><span className="match-step">{number}</span>
      <div><h2 id={`match-section-${number}`}>{title}</h2>{description && <p>{description}</p>}</div>
      <span className="match-required">필수 항목</span>
    </div>{children}
  </section>;
}

export default function MatchForm({ initialValues, selectedFacility, searchFacilities, onSubmit, submitLabel = '매치 개설 완료하기' }) {
  const [values, setValues] = useState(() => initialMatchValues({ ...initialValues, ...(selectedFacility ? { serviceId: selectedFacility.serviceId } : {}) }));
  const [facility, setFacility] = useState(selectedFacility || null);
  const [query, setQuery] = useState('');
  const [region, setRegion] = useState('');
  const [results, setResults] = useState([]);
  const [searchMessage, setSearchMessage] = useState('');
  const [searching, setSearching] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [pending, setPending] = useState(false);
  const submitting = useRef(false);
  const searchVersion = useRef(0);
  const formRef = useRef(null);

  function update(name, value) {
    setValues(previous => ({ ...previous, [name]: value }));
    setErrors(previous => ({ ...previous, [name]: undefined }));
  }

  function changeSport(sportType) {
    update('sportType', sportType);
  }

  async function search() {
    if (!searchFacilities) {
      setSearchMessage('시설 검색을 준비 중입니다. 시설 상세 화면에서 시설을 선택한 후 매치를 개설해주세요.');
      return;
    }
    const version = ++searchVersion.current;
    setSearching(true); setSearchMessage(''); setResults([]);
    try {
      const facilities = await searchFacilities({ query: query.trim(), region: region.trim() });
      if (version !== searchVersion.current) return;
      setResults(facilities);
      if (!facilities.length) setSearchMessage('검색 결과가 없습니다. 다른 시설명이나 지역으로 검색해주세요.');
    } catch (error) {
      if (version === searchVersion.current) setSearchMessage(error.response?.status === 400 ? '시설 검색 조건을 확인해주세요.' : error.response?.status === 503 ? '공공데이터 연결이 원활하지 않습니다. 잠시 후 다시 검색해주세요.' : '시설을 불러오지 못했습니다. 잠시 후 다시 검색해주세요.');
    } finally { if (version === searchVersion.current) setSearching(false); }
  }

  async function submit(event) {
    event.preventDefault();
    if (submitting.current) return;
    const nextErrors = validateMatch(values);
    setErrors(nextErrors); setSubmitError('');
    if (Object.keys(nextErrors).length) {
      requestAnimationFrame(() => formRef.current?.querySelector('[aria-invalid="true"]')?.focus());
      return;
    }
    submitting.current = true; setPending(true);
    try { await onSubmit(toMatchPayload(values)); }
    catch (error) {
      const messages = { 400: '매치 정보를 확인해주세요. 입력한 정보가 올바르지 않습니다.', 401: '로그인이 만료되었습니다. 다시 로그인해주세요.', 404: '선택한 시설을 찾을 수 없습니다. 시설을 다시 선택해주세요.' };
      setSubmitError(messages[error.response?.status] || (error.response || error.request ? '매치를 개설하지 못했습니다. 연결 상태를 확인하고 다시 시도해주세요.' : error.message || '매치를 개설하지 못했습니다. 다시 시도해주세요.'));
    } finally { submitting.current = false; setPending(false); }
  }

  const errorText = name => errors[name] && <p className="match-error" id={`error-${name}`}>{errors[name]}</p>;
  const fieldProps = (name, errorName = name) => ({ id: name, name, value: values[name], onChange: event => update(name, event.target.value), 'aria-invalid': !!errors[errorName], 'aria-describedby': errors[errorName] ? `error-${errorName}` : undefined });

  return <form className="match-form" ref={formRef} onSubmit={submit} noValidate>
    <fieldset className="match-fields" disabled={pending}>
      <legend className="match-sr-only">매치 개설 정보</legend>
      <Section number="1" title="운동 종목 선택" description="함께 즐기고 싶은 스포츠를 선택해주세요.">
        <div className="match-sports" role="group" aria-label="운동 종목">
          {SPORTS.map(([value, label, hint, icon]) => <button key={value} type="button" className={`match-sport ${values.sportType === value ? 'is-selected' : ''}`} aria-pressed={values.sportType === value} onClick={() => changeSport(value)}>
            <span className="match-sport-icon" aria-hidden="true"><img src={icon} width="22" height="22" alt="" /></span><strong>{label}</strong><small>{hint}</small>
          </button>)}
        </div>
      </Section>
      <Section number="2" title="체육시설 및 구장 선택">
        <label className="match-sr-only" htmlFor="facility-region">지역</label>
        <input id="facility-region" className="match-region" value={region} onChange={event => setRegion(event.target.value)} placeholder="지역 (선택, 예: 서초구)" onKeyDown={event => { if (event.key === 'Enter') { event.preventDefault(); search(); } }} />
        <label className="match-sr-only" htmlFor="facility-search">시설명</label>
        <div className="match-search"><input id="facility-search" value={query} placeholder="시설명으로 검색" onChange={event => setQuery(event.target.value)} onKeyDown={event => { if (event.key === 'Enter') { event.preventDefault(); search(); } }} aria-invalid={!!errors.serviceId} aria-describedby={errors.serviceId ? 'error-serviceId' : undefined} />
          <button type="button" className="match-small-button" onClick={search} disabled={searching}>{searching ? '검색 중…' : '시설 검색'}</button></div>
        <p className="match-search-message" role="status">{searchMessage}</p>
        {results.length > 0 && <ul className="match-results">{results.map(item => <li key={item.serviceId}><button type="button" onClick={() => { setFacility(item); update('serviceId', item.serviceId); setResults([]); setSearchMessage(''); }}><strong>{item.name}</strong><span>{[item.region, item.locationName].filter(Boolean).join(' · ')}</span><span>선택</span></button></li>)}</ul>}
        {facility ? <div className="match-facility"><div><strong>{facility.name}</strong><span className="match-facility-tag">선택한 시설</span><p>{[facility.region, facility.locationName].filter(Boolean).join(' · ')}</p></div><button type="button" onClick={() => { setFacility(null); update('serviceId', ''); }}>선택 해제</button></div> : <p className="match-empty">운동할 체육시설을 선택해주세요.</p>}
        {errorText('serviceId')}
      </Section>
      <Section number="3" title="일정 및 모집 인원 설정" description="경기 일정과 함께할 인원, 실력 레벨을 정해주세요.">
        <div className="match-schedule-grid"><div className="match-control"><label htmlFor="date">경기 날짜</label><input type="date" {...fieldProps('date', 'startAt')} />{errorText('startAt')}</div>
          <div className="match-control"><label htmlFor="maxParticipant">모집 최대 인원 (방장 포함)</label><div className="match-stepper"><button type="button" aria-label="모집 인원 줄이기" disabled={Number(values.maxParticipant) <= 1} onClick={() => update('maxParticipant', Math.max(1, Number(values.maxParticipant) - 1))}>−</button><input type="number" min="1" step="1" {...fieldProps('maxParticipant')} /><span>명</span><button type="button" aria-label="모집 인원 늘리기" onClick={() => update('maxParticipant', Number(values.maxParticipant) + 1)}>+</button></div>{errorText('maxParticipant')}</div>
          <div className="match-time-grid"><div className="match-control"><label htmlFor="startTime">시작 시간</label><input type="time" {...fieldProps('startTime', 'startAt')} /></div><div className="match-control"><label htmlFor="endTime">종료 시간</label><input type="time" {...fieldProps('endTime', 'endAt')} /></div><div className="match-control match-end-date"><label htmlFor="endDate">종료 날짜 (미선택 시 경기 당일)</label><input type="date" {...fieldProps('endDate', 'endAt')} />{errorText('endAt')}</div></div>
          <div className="match-control"><span id="match-level-label">경기 실력 레벨</span><div className="match-levels" role="group" aria-labelledby="match-level-label">{SKILL_LEVELS.map(level => <button type="button" key={level.value} aria-pressed={values.skillLevel === level.value} className={values.skillLevel === level.value ? 'is-selected' : ''} onClick={() => update('skillLevel', level.value)}>{level.label}</button>)}</div>{errorText('skillLevel')}</div>
        </div>
      </Section>
      <Section number="4" title="매치 상세 정보 & 수칙">
        <div className="match-control"><div className="match-label-row"><label htmlFor="title">매치 제목</label><span>{values.title.length} / 50</span></div><input {...fieldProps('title')} maxLength="50" placeholder="예) 주말 저녁 가볍게 즐기는 풋살 친선 경기" />{errorText('title')}</div>
        <div className="match-control match-description"><div className="match-label-row"><label htmlFor="description">상세 안내 및 매너 수칙</label><span>{values.description.length} / 500</span></div><textarea {...fieldProps('description')} maxLength="500" rows="6" placeholder={'함께할 분들에게 매치를 소개해주세요.\n준비물, 모임 장소, 경기 수칙 등을 안내하면 좋아요.'} />{errorText('description')}</div>
      </Section>
    </fieldset>
    <aside className="match-actions"><button className="match-submit" type="submit" disabled={pending}><img src={submitIcon} alt="" width="18" height="18" />{pending ? '매치 개설 중…' : submitLabel}</button><div role="alert">{submitError && <p className="match-error match-submit-error">{submitError}</p>}{Object.values(errors).some(Boolean) && <p className="match-error">입력 항목을 확인해주세요.</p>}</div></aside>
  </form>;
}
