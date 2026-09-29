import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { fetchFacilities } from '../api/facilityApi';
import FacilityListPage from './FacilityListPage';

jest.mock('../api/facilityApi', () => ({
  fetchFacilities: jest.fn(),
  facilityRequestError: () => '체육시설을 불러오지 못했습니다.',
}));

function renderPage(path = '/facilities') {
  return render(
    <MemoryRouter initialEntries={[path]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/facilities" element={<FacilityListPage />} />
      </Routes>
    </MemoryRouter>
  );
}

afterEach(() => jest.clearAllMocks());

test('헤더 query를 서비스명 검색 조건으로 사용한다', async () => {
  fetchFacilities.mockResolvedValue([]);
  renderPage('/facilities?query=응봉공원');
  await waitFor(() => expect(fetchFacilities).toHaveBeenCalled());
  expect(fetchFacilities.mock.calls[0][0]).toEqual({
    region: '',
    serviceName: '응봉공원',
    serviceType: '',
  });
  expect(screen.getByRole('button', { name: '시설 검색' })).toBeInTheDocument();
});

test('검색 결과를 카드로 보여주고 상세 링크로 연결한다', async () => {
  fetchFacilities.mockResolvedValue([{
    serviceId: 12,
    serviceName: '응봉공원 테니스장',
    serviceType: '테니스장',
    region: '성동구',
    locationName: '응봉공원',
    contact: '02-2293-7646',
    status: '접수중',
    startTime: '07:00',
    endTime: '19:00',
    isFree: false,
    closed: false,
  }]);
  renderPage('/facilities');
  expect(await screen.findByRole('heading', { name: '응봉공원 테니스장' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: '상세보기' })).toHaveAttribute('href', '/facilities/12');
  expect(screen.getByRole('button', { name: '매치 생성' })).toBeInTheDocument();
});

test('빈 결과와 실패 안내를 구분한다', async () => {
  fetchFacilities.mockResolvedValueOnce([]);
  renderPage('/facilities');
  expect(await screen.findByText(/조건에 맞는 체육시설이 없습니다/)).toBeInTheDocument();
});

test('조회 실패 시 다시 시도할 수 있다', async () => {
  fetchFacilities.mockRejectedValueOnce({ response: { status: 503 } });
  fetchFacilities.mockResolvedValueOnce([]);
  renderPage('/facilities');
  expect(await screen.findByRole('alert')).toHaveTextContent('체육시설을 불러오지 못했습니다.');
  fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
  expect(await screen.findByText(/조건에 맞는 체육시설이 없습니다/)).toBeInTheDocument();
});
