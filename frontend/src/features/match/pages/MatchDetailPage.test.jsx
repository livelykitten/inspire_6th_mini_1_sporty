import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { Link, MemoryRouter, Route, Routes } from 'react-router-dom';
import MatchDetailPage from './MatchDetailPage';
import { getMatchDetail, joinMatch, deleteMatch } from '../api/matchApi';

jest.mock('../api/matchApi', () => ({ getMatchDetail: jest.fn(), joinMatch: jest.fn(), deleteMatch: jest.fn() }));

const match = {
  matchId: 101, title: '주말 풋살 모집', description: '함께 운동해요.\n운동화를 준비해 주세요.',
  startAt: '2026-09-26T23:00:00', endAt: '2026-09-27T01:00:00',
  maxParticipant: 10, currentParticipantCount: 2, status: 'RECRUITING', skillLevel: 'BEGINNER', sportType: 'FUTSAL',
  serviceId: 7, serviceName: null, locationName: null, region: null,
  participants: [
    { profileId: 11, nickname: '생성자', imageUrl: null, role: 'OWNER' },
    { profileId: 12, nickname: '참가자이름', imageUrl: 'https://example.com/avatar.png', role: 'PARTICIPANT' },
  ],
  isOwner: false, isParticipant: false,
};

afterEach(() => { jest.resetAllMocks(); jest.restoreAllMocks(); localStorage.clear(); });

function showPage(path = '/matches/101') {
  return render(
    <MemoryRouter initialEntries={[path]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Link to="/matches/102">다른 매치</Link>
      <Routes><Route path="/matches/:matchId" element={<MatchDetailPage />} /><Route path="/" element={<p>홈 화면</p>} /><Route path="/login" element={<p>로그인 화면</p>} /></Routes>
    </MemoryRouter>
  );
}

test('loads publicly and renders API data, mandatory navigation and missing facility values', async () => {
  getMatchDetail.mockResolvedValue(match);
  showPage();
  expect(screen.getByRole('status')).toHaveTextContent('불러오는 중');
  expect(await screen.findByRole('heading', { name: match.title })).toBeInTheDocument();
  const nav = screen.getByRole('navigation', { name: '주 메뉴' });
  expect(within(nav).getByRole('link', { name: '매치 찾기' })).toHaveAttribute('href', '/');
  expect(within(nav).getByRole('link', { name: '시설 예약' })).toHaveAttribute('href', '/facilities');
  expect(within(nav).getByRole('link', { name: '내 매치 내역' })).toHaveAttribute('href', '/mypage');
  expect(screen.getByText('2026.09.26 23:00 — 2026.09.27 01:00')).toBeInTheDocument();
  expect(screen.getByText('2시간')).toBeInTheDocument();
  expect(screen.getByText('등록된 장소 정보가 아직 없습니다.')).toBeInTheDocument();
  expect(screen.getByRole('link', { name: '시설 정보 →' })).toHaveAttribute('href', '/facilities/7');
  expect(screen.getByRole('button', { name: '생성자' })).toHaveAttribute('aria-haspopup', 'dialog');
  expect(screen.getByText('8명이 더 함께할 수 있어요.')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: '참가 신청하기' })).toBeEnabled();
  expect(getMatchDetail).toHaveBeenCalledWith('101', expect.any(AbortSignal));
});

test.each([
  [{ isOwner: true, isParticipant: true }, '내가 개설한 매치'],
  [{ isOwner: false, isParticipant: true }, '참가 취소하기'],
  [{ status: 'CLOSED' }, '모집이 마감되었습니다'],
  [{ currentParticipantCount: 10 }, '모집 정원이 찼습니다'],
])('uses server membership and recruitment state without enabling pending actions', async (state, label) => {
  getMatchDetail.mockResolvedValue({ ...match, ...state });
  showPage();
  expect(await screen.findByRole('button', { name: label })).toBeDisabled();
});

test('shows a specific 404 message without inventing match data', async () => {
  getMatchDetail.mockRejectedValue({ response: { status: 404 } });
  showPage();
  expect(await screen.findByRole('alert')).toHaveTextContent('매치를 찾을 수 없습니다');
  expect(screen.queryByText(match.title)).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
});

test('can retry after a request failure', async () => {
  getMatchDetail.mockRejectedValueOnce(new Error('network')).mockResolvedValueOnce(match);
  showPage();
  fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));
  expect(await screen.findByRole('heading', { name: match.title })).toBeInTheDocument();
  expect(getMatchDetail).toHaveBeenCalledTimes(2);
});

test('handles empty participants and description', async () => {
  getMatchDetail.mockResolvedValue({ ...match, participants: [], currentParticipantCount: 0, description: null });
  showPage();
  expect(await screen.findByText('아직 참여한 사람이 없습니다.')).toBeInTheDocument();
  expect(screen.getByText('등록된 상세 안내가 없습니다.')).toBeInTheDocument();
});

test('does not turn unknown participation counts into zero', async () => {
  getMatchDetail.mockResolvedValue({ ...match, participants: null, currentParticipantCount: null, maxParticipant: null });
  showPage();
  expect(await screen.findByText('참가자 정보를 확인할 수 없습니다.')).toBeInTheDocument();
  expect(screen.getByText('모집 인원을 확인할 수 없습니다.')).toBeInTheDocument();
  expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
});

test('does not request an invalid match ID', async () => {
  showPage('/matches/invalid');
  expect(await screen.findByRole('alert')).toHaveTextContent('매치를 찾을 수 없습니다');
  expect(getMatchDetail).not.toHaveBeenCalled();
});

test('ignores a late response after navigation to another match', async () => {
  let finishFirst;
  getMatchDetail.mockImplementationOnce(() => new Promise(resolve => { finishFirst = resolve; }))
    .mockResolvedValueOnce({ ...match, matchId: 102, title: '두 번째 매치' });
  showPage();
  fireEvent.click(screen.getByRole('link', { name: '다른 매치' }));
  expect(await screen.findByRole('heading', { name: '두 번째 매치' })).toBeInTheDocument();
  await act(async () => finishFirst(match));
  await waitFor(() => expect(screen.queryByRole('heading', { name: match.title })).not.toBeInTheDocument());
  expect(getMatchDetail.mock.calls[0][1].aborted).toBe(true);
});

test.each([['MALE', '남성'], ['FEMALE', '여성'], ['MIXED', '혼성'], [null, '성별 정보 없음']])('renders gender %s and null facility placeholders', async (genderGroup, label) => {
  getMatchDetail.mockResolvedValue({ ...match, genderGroup });
  showPage();
  await screen.findByRole('heading', { name: match.title });
  const summary = screen.getByRole('region', { name: /경기 주요 정보 요약/ });
  expect(within(summary).getByText('인원 구성')).toBeInTheDocument();
  expect(within(summary).getByText('2 / 10명 참여')).toBeInTheDocument();
  expect(within(summary).getByText(label)).toBeInTheDocument();
  const place = screen.getByRole('region', { name: /장소 및 시설 안내/ });
  expect(within(place).getByText('시설명 정보 없음')).toBeInTheDocument();
  expect(within(place).getByText('장소 정보 없음')).toBeInTheDocument();
  expect(within(place).getByText('지역 정보 없음')).toBeInTheDocument();
});

test('preserves non-null facility data including an empty string', async () => {
  getMatchDetail.mockResolvedValue({ ...match, serviceName: '서울 체육관', locationName: '', region: '성동구' });
  showPage();
  expect(await screen.findByText('서울 체육관')).toBeInTheDocument();
  const place = screen.getByRole('region', { name: /장소 및 시설 안내/ });
  expect(within(place).getByText('성동구')).toBeInTheDocument();
  expect(within(place).queryByText('장소 정보 없음')).not.toBeInTheDocument();
});

test('supports IDs above the signed 32-bit range', async () => {
  getMatchDetail.mockResolvedValue({ ...match, matchId: 2147483648 });
  showPage('/matches/2147483648');
  expect(await screen.findByRole('heading', { name: match.title })).toBeInTheDocument();
  expect(getMatchDetail).toHaveBeenCalledWith('2147483648', expect.any(AbortSignal));
});

test('rejects IDs outside the safe integer range', async () => {
  showPage('/matches/9007199254740992');
  expect(await screen.findByRole('alert')).toHaveTextContent('매치를 찾을 수 없습니다');
  expect(getMatchDetail).not.toHaveBeenCalled();
});

test('redirects anonymous join attempts to login without posting', async () => {
  getMatchDetail.mockResolvedValue(match);
  showPage();
  fireEvent.click(await screen.findByRole('button', { name: '참가 신청하기' }));
  expect(await screen.findByText('로그인 화면')).toBeInTheDocument();
  expect(joinMatch).not.toHaveBeenCalled();
});

test('joins once while pending and refreshes membership on success', async () => {
  localStorage.setItem('at', 'token');
  getMatchDetail.mockResolvedValueOnce(match).mockResolvedValueOnce({ ...match, isParticipant: true, currentParticipantCount: 3 });
  let finish;
  joinMatch.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
  showPage();
  const button = await screen.findByRole('button', { name: '참가 신청하기' });
  fireEvent.click(button); fireEvent.click(button);
  expect(joinMatch).toHaveBeenCalledTimes(1);
  expect(joinMatch).toHaveBeenCalledWith(101);
  expect(button).toBeDisabled();
  await act(async () => finish({ matchId: 101 }));
  expect(await screen.findByRole('button', { name: '참가 취소하기' })).toBeDisabled();
  expect(screen.getByText('3명 참여 중')).toBeInTheDocument();
});

test.each([[400, '모집이 마감'], [403, '권한'], [404, '매치를 찾을'], [409, '이미 참가'], [501, '준비 중']])('shows join failure %s', async (status, message) => {
  localStorage.setItem('at', 'token');
  getMatchDetail.mockResolvedValue(match);
  joinMatch.mockRejectedValue({ response: { status } });
  showPage();
  fireEvent.click(await screen.findByRole('button', { name: '참가 신청하기' }));
  expect(await screen.findByRole('alert')).toHaveTextContent(message);
  expect(screen.getByRole('button', { name: '참가 신청하기' })).toBeEnabled();
});

test('expired authentication redirects and clears the token', async () => {
  localStorage.setItem('at', 'expired');
  getMatchDetail.mockResolvedValue(match);
  joinMatch.mockRejectedValue({ response: { status: 401 } });
  showPage();
  fireEvent.click(await screen.findByRole('button', { name: '참가 신청하기' }));
  expect(await screen.findByText('로그인 화면')).toBeInTheDocument();
  expect(localStorage.getItem('at')).toBeNull();
});

test('owner deletion requires confirmation and navigates home after success', async () => {
  localStorage.setItem('at', 'token');
  getMatchDetail.mockResolvedValue({ ...match, isOwner: true, isParticipant: true });
  const confirm = jest.spyOn(window, 'confirm').mockReturnValueOnce(false).mockReturnValueOnce(true);
  deleteMatch.mockResolvedValue(undefined);
  showPage();
  const button = await screen.findByRole('button', { name: '매치 삭제하기' });
  fireEvent.click(button);
  expect(deleteMatch).not.toHaveBeenCalled();
  fireEvent.click(button);
  expect(await screen.findByText('홈 화면')).toBeInTheDocument();
  expect(confirm).toHaveBeenCalledTimes(2);
  expect(deleteMatch).toHaveBeenCalledWith(101);
});

test('non-owners cannot see delete actions', async () => {
  getMatchDetail.mockResolvedValue(match);
  showPage();
  await screen.findByRole('heading', { name: match.title });
  expect(screen.queryByRole('button', { name: '매치 삭제하기' })).not.toBeInTheDocument();
});

test('failed deletion keeps the detail page and reports the error', async () => {
  localStorage.setItem('at', 'token');
  getMatchDetail.mockResolvedValue({ ...match, isOwner: true });
  jest.spyOn(window, 'confirm').mockReturnValue(true);
  deleteMatch.mockRejectedValue({ response: { status: 403 } });
  showPage();
  fireEvent.click(await screen.findByRole('button', { name: '매치 삭제하기' }));
  expect(await screen.findByRole('alert')).toHaveTextContent('권한');
  expect(screen.getByRole('heading', { name: match.title })).toBeInTheDocument();
});
