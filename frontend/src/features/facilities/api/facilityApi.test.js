import api from '../../../api/axios';
import { fetchFacilities, toFacilityItem } from './facilityApi';

jest.mock('../../../api/axios', () => ({ get: jest.fn() }));
afterEach(() => jest.clearAllMocks());

test('sends FC-01 search params and maps the list DTO', async () => {
  api.get.mockResolvedValue({
    data: [{
      serviceId: 1,
      serviceName: '응봉공원 테니스장',
      serviceType: '테니스장',
      region: '성동구',
      locationName: '응봉공원',
      contact: '02-2293-7646',
      status: '접수중',
      startTime: '07:00:00',
      endTime: '19:00:00',
      isFree: false,
    }],
  });

  await expect(fetchFacilities({
    region: '성동구',
    serviceName: '테니스',
    serviceType: '테니스장',
  })).resolves.toEqual([expect.objectContaining({
    serviceId: 1,
    serviceName: '응봉공원 테니스장',
    locationName: '응봉공원',
    startTime: '07:00',
    closed: false,
  })]);

  expect(api.get).toHaveBeenCalledWith('/api/services', {
    params: { region: '성동구', serviceName: '테니스', serviceType: '테니스장' },
    signal: undefined,
    timeout: 15000,
  });
});

test('marks closed facilities and rejects a non-array payload', async () => {
  expect(toFacilityItem({
    serviceId: 2,
    serviceName: '풋살장',
    status: '예약마감',
  }).closed).toBe(true);

  api.get.mockResolvedValue({ data: { items: [] } });
  await expect(fetchFacilities()).rejects.toThrow('응답 형식');
});
