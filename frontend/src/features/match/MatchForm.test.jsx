import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import MatchForm from './MatchForm';
import MatchCreatePage, { RequireMatchAuth } from './MatchCreatePage';
import { createMatch } from './matchApi';
import { initialMatchValues, toMatchPayload, validateMatch } from './matchValidation';

jest.mock('./matchApi', () => ({ createMatch: jest.fn(), searchMatchFacilities: jest.fn() }));

const match = { serviceId: 1, title: '주말 풋살 모집', description: '같이 풋살하실 분', startAt: '2026-09-26T19:00', endAt: '2026-09-26T21:00', maxParticipant: 10, skillLevel: 'BEGINNER', sportType: 'FUTSAL' };
const facility = { serviceId: 1, name: '서초 풋살장', region: '서초구', locationName: '서초종합체육관', sportType: 'FUTSAL' };

afterEach(() => { localStorage.clear(); jest.clearAllMocks(); });

test('maps all eight API fields without converting local times to UTC', () => {
  expect(toMatchPayload(initialMatchValues(match))).toEqual(match);
  expect(validateMatch(initialMatchValues(match))).toEqual({});
});

test('submits the chosen sport independently from the facility', async () => {
  const submit = jest.fn().mockResolvedValue(undefined);
  render(<MatchForm initialValues={{ ...match, sportType: 'SWIMMING' }} selectedFacility={facility} onSubmit={submit} />);
  expect(screen.getByRole('button', { name: /수영/ })).toHaveAttribute('aria-pressed', 'true');
  fireEvent.click(screen.getByRole('button', { name: /탁구/ }));
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 완료하기' }));
  await waitFor(() => expect(submit).toHaveBeenCalledWith({ ...match, sportType: 'TABLE_TENNIS' }));
});

test('rejects invalid inputs and accepts matches ending the next day', () => {
  const values = initialMatchValues({ ...match, serviceId: 0, title: ' ', maxParticipant: 0, endAt: '2026-09-26T18:00' });
  expect(validateMatch(values)).toEqual(expect.objectContaining({ serviceId: expect.any(String), title: expect.any(String), maxParticipant: expect.any(String), endAt: expect.any(String) }));
  expect(validateMatch(initialMatchValues({ ...match, endAt: '2026-09-27T01:00' }))).toEqual({});
});

test('shows validation and does not call the API for an empty form', async () => {
  const submit = jest.fn();
  render(<MatchForm onSubmit={submit} />);
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 완료하기' }));
  expect(await screen.findByText('체육시설을 선택해주세요.')).toBeInTheDocument();
  expect(submit).not.toHaveBeenCalled();
});

test('submits a populated form once while the request is pending', async () => {
  let resolve;
  const submit = jest.fn(() => new Promise(done => { resolve = done; }));
  render(<MatchForm initialValues={match} selectedFacility={facility} onSubmit={submit} />);
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 완료하기' }));
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 중…' }));
  expect(submit).toHaveBeenCalledTimes(1);
  expect(submit).toHaveBeenCalledWith(match);
  resolve();
  await waitFor(() => expect(screen.getByRole('button', { name: '매치 개설 완료하기' })).toBeEnabled());
});

test.each([[400, '매치 정보를 확인해주세요.'], [404, '선택한 시설을 찾을 수 없습니다.']])('displays HTTP %s and preserves the input', async (status, message) => {
  render(<MatchForm initialValues={match} selectedFacility={facility} onSubmit={jest.fn().mockRejectedValue({ response: { status } })} />);
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 완료하기' }));
  expect(await screen.findByText(new RegExp(message))).toBeInTheDocument();
  expect(screen.getByLabelText('매치 제목')).toHaveValue(match.title);
});

test('keeps sport and facility selections independent', async () => {
  const search = jest.fn().mockResolvedValue([facility]);
  render(<MatchForm searchFacilities={search} onSubmit={jest.fn()} />);
  fireEvent.click(screen.getByRole('button', { name: /수영/ }));
  fireEvent.change(screen.getByLabelText('시설명'), { target: { value: '풋살' } });
  fireEvent.change(screen.getByLabelText('지역'), { target: { value: '서초구' } });
  fireEvent.click(screen.getByRole('button', { name: '시설 검색' }));
  fireEvent.click(await screen.findByRole('button', { name: /서초 풋살장/ }));
  expect(search).toHaveBeenCalledWith({ query: '풋살', region: '서초구' });
  expect(screen.getByText('선택한 시설')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /수영/ })).toHaveAttribute('aria-pressed', 'true');
  expect(screen.getByText('서초구 · 서초종합체육관')).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: /테니스/ }));
  expect(screen.getByText('선택한 시설')).toBeInTheDocument();
  expect(screen.getByLabelText('시설명')).toHaveValue('풋살');
  expect(screen.getByLabelText('지역')).toHaveValue('서초구');
});

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}{location.search}|{location.state?.from}</div>;
}

function renderRoute() {
  return render(<MemoryRouter initialEntries={[{ pathname: '/matches/new', state: { facility } }]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}><Routes>
    <Route path="/matches/new" element={<RequireMatchAuth><MatchCreatePage /></RequireMatchAuth>} />
    <Route path="/login" element={<LocationProbe />} />
    <Route path="/matches/:matchId" element={<LocationProbe />} />
  </Routes></MemoryRouter>);
}

test('requires authentication and keeps the return URL', () => {
  renderRoute();
  expect(screen.getByTestId('location')).toHaveTextContent('/login?redirect=%2Fmatches%2Fnew|/matches/new');
});

test.each([201, 401])('handles creation result %s with the correct redirect', async status => {
  localStorage.setItem('at', 'test-token');
  if (status === 201) createMatch.mockResolvedValue(42);
  else createMatch.mockRejectedValue({ response: { status: 401 } });
  renderRoute();
  for (const [label, value] of [['경기 날짜', '2026-09-26'], ['시작 시간', '19:00'], ['종료 시간', '21:00'], ['매치 제목', match.title], ['상세 안내 및 매너 수칙', match.description]]) {
    fireEvent.change(screen.getByLabelText(label), { target: { value } });
  }
  fireEvent.click(screen.getByRole('button', { name: '매치 개설 완료하기' }));
  expect(await screen.findByTestId('location')).toHaveTextContent(status === 201 ? '/matches/42' : '/login?redirect=%2Fmatches%2Fnew');
  if (status === 401) expect(localStorage.getItem('at')).toBeNull();
});
