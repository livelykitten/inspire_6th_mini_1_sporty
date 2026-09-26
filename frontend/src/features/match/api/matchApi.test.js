import api from '../../../api/axios';
import { createMatch, searchMatchFacilities, getMatchDetail } from './matchApi';

jest.mock('../../../api/axios', () => ({ post: jest.fn(), get: jest.fn() }));
afterEach(() => jest.clearAllMocks());

test('loads the detail endpoint and forwards cancellation', async () => {
  const signal = new AbortController().signal;
  const match = { matchId: 101, title: '풋살', isOwner: true };
  api.get.mockResolvedValue({ data: match });
  await expect(getMatchDetail('101', signal)).resolves.toEqual(match);
  expect(api.get).toHaveBeenCalledWith('/api/matches/101', { signal });
});

test.each(['<html>frontend fallback</html>', { matchId: 2 }, null])('rejects unexpected detail responses', async data => {
  api.get.mockResolvedValue({ data });
  await expect(getMatchDetail('101')).rejects.toThrow('응답 형식');
});

test.each([17, { matchId: 17 }])('reads the created match ID from %p', async data => {
  api.post.mockResolvedValue({ data });
  await expect(createMatch({ title: '풋살' })).resolves.toBe(17);
  expect(api.post).toHaveBeenCalledWith('/api/matches', { title: '풋살' });
});

test('does not navigate to an invalid ID after an unexpected success response', async () => {
  api.post.mockResolvedValue({ data: {} });
  await expect(createMatch({})).rejects.toThrow('매치 번호');
});

test('sends documented facility filters and normalizes the response', async () => {
  api.get.mockResolvedValue({ data: [{ serviceId: 1, serviceName: '서초 풋살장', region: '서초구', locationName: '서초종합체육관' }] });
  await expect(searchMatchFacilities({ query: '풋살', region: '서초구' })).resolves.toEqual([{ serviceId: 1, name: '서초 풋살장', region: '서초구', locationName: '서초종합체육관' }]);
  expect(api.get).toHaveBeenCalledWith('/api/services', { params: { serviceName: '풋살', region: '서초구' } });
});
