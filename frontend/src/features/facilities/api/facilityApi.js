import api from '../../../api/axios';

const SEARCH_FIELDS = ['region', 'serviceName', 'serviceType', 'status'];

export function toFacilityItem(dto) {
  if (!dto || !Number.isSafeInteger(dto.serviceId) || dto.serviceId < 1 || typeof dto.serviceName !== 'string') {
    throw new Error('시설 목록 응답 형식이 올바르지 않습니다.');
  }

  const status = dto.status || '정보없음';
  return {
    serviceId: dto.serviceId,
    serviceName: dto.serviceName,
    serviceType: dto.serviceType || '정보없음',
    region: dto.region || '',
    locationName: dto.locationName || '',
    contact: dto.contact || '',
    status,
    startTime: formatClock(dto.startTime),
    endTime: formatClock(dto.endTime),
    isFree: dto.isFree,
    url: dto.url || '',
    closed: isClosedStatus(status),
  };
}

export async function fetchFacilities(filters = {}, signal) {
  const params = Object.fromEntries(
    SEARCH_FIELDS
      .filter((key) => filters[key] != null && String(filters[key]).trim() !== '')
      .map((key) => [key, String(filters[key]).trim()])
  );
  const { data } = await api.get('/api/services', { params, signal, timeout: 15000 });
  if (!Array.isArray(data)) {
    throw new Error('시설 목록 응답 형식이 올바르지 않습니다.');
  }
  return data.map(toFacilityItem);
}

export function facilityRequestError(error) {
  const status = error.response?.status;
  if (status === 400) return '검색 조건을 확인해주세요.';
  if (status === 503) return '공공데이터 연결이 원활하지 않습니다. 잠시 후 다시 검색해주세요.';
  return '체육시설을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
}

function formatClock(value) {
  if (typeof value !== 'string' || value.length < 5) return '';
  return value.slice(0, 5);
}

function isClosedStatus(status) {
  return status.includes('마감');
}
