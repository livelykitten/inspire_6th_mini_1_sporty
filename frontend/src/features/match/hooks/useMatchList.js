import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { fetchMatchList, fetchRecommendedMatches, requestMatchParticipation, matchRequestError } from '../api/matchListApi';

const EMPTY_FILTERS = { sportType: '', status: '' };

export default function useMatchList() {
  const navigate = useNavigate();
  const location = useLocation();
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [applied, setApplied] = useState(EMPTY_FILTERS);
  const [matches, setMatches] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [status, setStatus] = useState('loading');
  const [recommendationStatus, setRecommendationStatus] = useState('idle');
  const [listError, setListError] = useState('');
  const [recommendationError, setRecommendationError] = useState('');
  const [joinMessage, setJoinMessage] = useState('');
  const [joiningId, setJoiningId] = useState(null);
  const [sort, setSort] = useState('default');
  const [recommendationSort, setRecommendationSort] = useState('default');
  const [limit, setLimit] = useState(6);
  const listRequest = useRef(null);
  const recommendationRequest = useRef(null);
  const joining = useRef(false);
  const mounted = useRef(true);
  const appliedRef = useRef(applied);
  appliedRef.current = applied;

  const loadList = useCallback(async conditions => {
    listRequest.current?.abort();
    const request = new AbortController();
    listRequest.current = request;
    setStatus('loading'); setListError('');
    try {
      const rows = await fetchMatchList(conditions, request.signal);
      if (request.signal.aborted) return;
      setMatches(rows); setStatus('success');
    } catch (error) {
      if (request.signal.aborted) return;
      setListError(matchRequestError(error)); setStatus('error');
    }
  }, []);

  const loadRecommendations = useCallback(async () => {
    recommendationRequest.current?.abort();
    if (!localStorage.getItem('at')) {
      setRecommendations([]); setRecommendationStatus('idle');
      setRecommendationError('로그인하면 맞춤 추천을 확인할 수 있습니다.');
      return;
    }
    const request = new AbortController();
    recommendationRequest.current = request;
    setRecommendationStatus('loading'); setRecommendationError('');
    try {
      const rows = await fetchRecommendedMatches(request.signal);
      if (request.signal.aborted) return;
      setRecommendations(rows); setRecommendationStatus('success');
    } catch (error) {
      if (request.signal.aborted) return;
      setRecommendationError(matchRequestError(error, 'recommendation')); setRecommendationStatus('error');
    }
  }, []);

  useEffect(() => {
    mounted.current = true;
    loadRecommendations();
    return () => { mounted.current = false; listRequest.current?.abort(); recommendationRequest.current?.abort(); };
  }, [loadRecommendations]);
  useEffect(() => { loadList(applied); }, [applied, loadList]);

  const ordered = useMemo(() => sortMatches(matches.filter(match => !applied.status || match.statusCode === applied.status), sort), [matches, applied.status, sort]);

  const onJoin = async matchId => {
    if (joining.current) return;
    if (!localStorage.getItem('at')) {
      const from = location.pathname + location.search;
      navigate(`/login?redirect=${encodeURIComponent(from)}`, { state: { from } });
      return;
    }
    joining.current = true; setJoiningId(matchId); setJoinMessage('');
    try {
      await requestMatchParticipation(matchId);
      if (!mounted.current) return;
      setJoinMessage('참가 신청이 완료되었습니다.');
      await Promise.all([loadList(appliedRef.current), loadRecommendations()]);
    } catch (error) {
      if (!mounted.current) return;
      setJoinMessage(matchRequestError(error, 'join'));
      if (error.response?.status === 401) {
        localStorage.removeItem('at');
        const from = location.pathname + location.search;
        navigate(`/login?redirect=${encodeURIComponent(from)}`, { state: { from } });
      }
    } finally {
      joining.current = false;
      if (mounted.current) setJoiningId(null);
    }
  };

  return {
    preview: false, matches: ordered.slice(0, limit), totalCount: ordered.length,
    recommendations: sortMatches(recommendations, recommendationSort),
    filters, sort, recommendationSort, status, recommendationStatus, listError, recommendationError,
    joiningId, joinMessage, hasMore: limit < ordered.length,
    onFilterChange: (name, value) => setFilters(previous => ({ ...previous, [name]: value })),
    onApply: () => { setLimit(6); setApplied({ ...filters }); },
    onReset: () => { setFilters(EMPTY_FILTERS); setApplied({ ...EMPTY_FILTERS }); setSort('default'); setLimit(6); },
    onSort: value => { setSort(value); setLimit(6); },
    onRecommendationSort: setRecommendationSort,
    onJoin, onLoadMore: () => setLimit(previous => previous + 6),
    onRetry: () => loadList(appliedRef.current), onRecommendationRetry: loadRecommendations,
    onFacilitySearch: query => navigate(query.trim() ? `/facilities?${new URLSearchParams({ query: query.trim() })}` : '/facilities'),
  };
}

function sortMatches(matches, sort) {
  if (sort !== 'startAt') return [...matches];
  const time = match => { const value = Date.parse(match.startAt); return Number.isNaN(value) ? Infinity : value; };
  return [...matches].sort((a, b) => time(a) - time(b));
}
