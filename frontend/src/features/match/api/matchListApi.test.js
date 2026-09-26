import api from '../../../api/axios';
import { fetchMatchList, fetchRecommendedMatches, requestMatchParticipation, toMatchListItem, matchRequestError } from './matchListApi';

jest.mock('../../../api/axios', () => ({ get: jest.fn(), post: jest.fn() }));
afterEach(() => jest.clearAllMocks());
const dto = { matchId: 1, title: '풋살', sportType: 'FUTSAL', status: 'RECRUITING', startAt: '2026-09-26T19:30', maxParticipant: 12 };

test('sends only supported nonempty search fields and cancellation signal', async () => {
  api.get.mockResolvedValue({ data: [dto] });
  const signal = new AbortController().signal;
  const result = await fetchMatchList({ sportType: 'FUTSAL', titleKeyword: '', region: '성동구', status: 'CLOSED', isFree: 'Y' }, signal);
  expect(api.get).toHaveBeenCalledWith('/api/matches', { params: { sportType: 'FUTSAL' }, signal, timeout: 15000 });
  expect(result[0]).toMatchObject({ id: 1, sport: '풋살', statusCode: 'RECRUITING', closed: false });
  expect(result[0].schedule).toContain('19:30');
  expect(result[0].occupancy).toBeUndefined();
  expect(result[0].fee).toBeUndefined();
});

test.each([{}, null, { matches: [] }])('rejects malformed list response %p', async data => {
  api.get.mockResolvedValue({ data });
  await expect(fetchMatchList()).rejects.toThrow('목록 응답');
});

test.each([[dto], { matches: [dto] }])('accepts documented recommendation list %p', async data => {
  api.get.mockResolvedValue({ data });
  expect(await fetchRecommendedMatches()).toHaveLength(1);
});

test('unknown and closed statuses cannot be joined', () => {
  expect(toMatchListItem({ ...dto, status: 'CLOSED' }).closed).toBe(true);
  expect(toMatchListItem({ ...dto, status: null }).closed).toBe(true);
});

test('requires confirmed creation of participation', async () => {
  api.post.mockResolvedValue({ status: 201, data: { matchId: 1 } });
  await expect(requestMatchParticipation(1)).resolves.toEqual({ matchId: 1 });
  expect(api.post).toHaveBeenCalledWith('/api/matches/1/participants', undefined, { timeout: 15000 });
  api.post.mockRejectedValue({ response: { status: 501 } });
  await expect(requestMatchParticipation(1)).rejects.toEqual({ response: { status: 501 } });
  expect(matchRequestError({ response: { status: 501 } }, 'join')).toContain('준비 중');
});

test('preview IDs cannot reach the API', async () => {
  await expect(requestMatchParticipation('preview-1')).rejects.toThrow('매치 번호');
  expect(api.post).not.toHaveBeenCalled();
});
