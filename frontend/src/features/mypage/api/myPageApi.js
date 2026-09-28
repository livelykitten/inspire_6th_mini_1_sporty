import api from '../../../api/axios';

// [USR-06] GET /api/users/me의 평면 응답을 읽기 전용 회원 정보와 수정 폼으로 나눈다.
export const loadMyPage = ({ signal }) => api.get('/api/users/me', { signal }).then(({ data }) => ({
  user: { email: data.email, gender: data.gender },
  profile: { nickname: data.nickname, district: data.district, preferenceSports: data.preferenceSports || [] },
}));

// [PR-01] 요청은 sportTypes, 응답은 preferenceSports다. 이미지와 회원 정보는 전송하지 않는다.
export const saveProfile = form => api.put('/api/profiles/me', {
  nickname: form.nickname,
  district: form.district,
  sportTypes: form.preferenceSports,
}).then(({ data }) => ({
  nickname: data.nickname, district: data.district, preferenceSports: data.preferenceSports || [],
}));

// [USR-04] axios.delete의 요청 본문은 두 번째 인자의 data에 담는다.
export const withdrawUser = password => api.delete('/api/users/me', { data: { password } });
