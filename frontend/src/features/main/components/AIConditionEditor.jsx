import { useState } from 'react';
import { districts, sports } from '../../auth/data/signUpOptions';
import '../css/main.css';

// [AI-02] AI 매치 검색 — 맞춤 조건 직접 수정
// 초기값은 AI가 추출한 조건(toCriteria 결과)이고, AI 검색 전에는 모두 빈 값이다.
// 적용하면 onApply(수정한 조건)를 호출하고 재검색은 MainPage가 한다.
const GENDERS = [['MALE', '남성'], ['FEMALE', '여성'], ['MIXED', '혼성']];
const SKILLS = [['BEGINNER', '초급'], ['INTERMEDIATE', '중급'], ['ADVANCED', '고급']];

const AIConditionEditor = ({ initialCriteria, onApply, onCancel, pending = false, error = '' }) => {
  const [criteria, setCriteria] = useState(initialCriteria);
  const [invalid, setInvalid] = useState('');
  const change = event => setCriteria(prev => ({ ...prev, [event.target.name]: event.target.value }));

  const submitHandler = event => {
    event.preventDefault();
    if (criteria.startDate && criteria.endDate && criteria.endDate < criteria.startDate) {
      setInvalid('종료일은 시작일과 같거나 이후여야 합니다.');
      return;
    }
    setInvalid('');
    onApply(criteria);
  };

  const select = (name, label, options) => (
    <div className="ms-condition">
      <label htmlFor={`ms-edit-${name}`}>{label}</label>
      <select id={`ms-edit-${name}`} name={name} value={criteria[name]} onChange={change}>
        <option value="">미지정</option>
        {options.map(([value, text]) => <option key={value} value={value}>{text}</option>)}
      </select>
    </div>
  );
  const date = (name, label) => (
    <div className="ms-condition">
      <label htmlFor={`ms-edit-${name}`}>{label}</label>
      <input id={`ms-edit-${name}`} type="date" name={name} value={criteria[name]} onChange={change} />
    </div>
  );

  return (
    <form className="ms-summary ms-panel ms-editor" aria-label="맞춤 조건 직접 수정" onSubmit={submitHandler}>
      <h2>맞춤 조건 직접 수정</h2>
      <div className="ms-conditions">
        {select('sportType', '종목', sports)}
        {select('genderGroup', '성별', GENDERS)}
        {date('startDate', '시작일')}
        {date('endDate', '종료일')}
        {select('region', '자치구', districts.map(([, name]) => [name, name]))}
        {select('skillLevel', '실력 수준', SKILLS)}
      </div>
      {(invalid || error) && <p role="alert">{invalid || error}</p>}
      {/* 버튼 모양은 AI 검색 버튼(ms-search-actions, ms-primary)과 맞춘다. */}
      <div className="ms-search-actions ms-editor-actions">
        <button className="ms-primary" type="button" onClick={onCancel} disabled={pending}>취소</button>
        <button className="ms-primary" type="submit" disabled={pending}>{pending ? '찾는 중…' : '이 조건으로 다시 찾기'}</button>
      </div>
    </form>
  );
};
export default AIConditionEditor;
