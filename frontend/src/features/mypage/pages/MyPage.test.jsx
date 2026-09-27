import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import MyPage from './MyPage';
import { previewData } from '../data/previewData';

beforeEach(() => localStorage.clear());
afterEach(() => localStorage.clear());
const show = props => render(<MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}><MyPage {...props} /></MemoryRouter>);

test('조회값을 미리 채우고 취소하면 닉네임/자치구/종목을 복구한다', async () => {
  show({ preview: true });
  const nickname = await screen.findByLabelText(/활동 닉네임/);
  expect(nickname).toHaveValue('성수동매치');
  expect(screen.getByRole('checkbox', { name: /축구/ })).toBeChecked();
  fireEvent.change(nickname, { target: { value: '변경닉네임' } });
  fireEvent.change(screen.getByLabelText('주 활동 자치구'), { target: { value: 'MAPO' } });
  fireEvent.click(screen.getByRole('checkbox', { name: /축구/ }));
  fireEvent.click(screen.getByRole('button', { name: '취소' }));
  expect(nickname).toHaveValue('성수동매치');
  expect(screen.getByLabelText('주 활동 자치구')).toHaveValue('SEONGDONG');
  expect(screen.getByRole('checkbox', { name: /축구/ })).toBeChecked();
});

test('저장 버튼을 눌러야 프로필만 전송하며 저장된 값을 취소 기준으로 삼는다', async () => {
  localStorage.setItem('at', 'test');
  const saveProfile = jest.fn(data => Promise.resolve(data));
  show({ loadMyPage: () => Promise.resolve(previewData), saveProfile });
  const nickname = await screen.findByLabelText(/활동 닉네임/);
  fireEvent.change(nickname, { target: { value: ' 새닉네임 ' } });
  expect(saveProfile).not.toHaveBeenCalled();
  fireEvent.click(screen.getByRole('button', { name: '변경사항 저장' }));
  await screen.findByText('프로필 변경사항을 저장했습니다.');
  expect(saveProfile).toHaveBeenCalledWith({ ...previewData.profile, nickname: '새닉네임' }, null);
  fireEvent.change(nickname, { target: { value: '다른닉네임' } });
  fireEvent.click(screen.getByRole('button', { name: '취소' }));
  expect(nickname).toHaveValue('새닉네임');
});

test('저장 실패 시 입력을 보존하고 재시도할 수 있다', async () => {
  localStorage.setItem('at', 'test');
  show({ loadMyPage: () => Promise.resolve(previewData), saveProfile: () => Promise.reject(new Error('network')) });
  const nickname = await screen.findByLabelText(/활동 닉네임/);
  fireEvent.change(nickname, { target: { value: '유지할값' } });
  fireEvent.click(screen.getByRole('button', { name: '변경사항 저장' }));
  expect(await screen.findByRole('alert')).toHaveTextContent('저장하지 못했습니다');
  expect(nickname).toHaveValue('유지할값');
  await waitFor(() => expect(screen.getByRole('button', { name: '변경사항 저장' })).toBeEnabled());
});

test('미리보기 저장은 연결 함수를 호출하지 않고 서버 미저장을 안내한다', async () => {
  const saveProfile = jest.fn();
  show({ preview: true, saveProfile });
  await screen.findByLabelText(/활동 닉네임/);
  fireEvent.click(screen.getByRole('button', { name: '변경사항 저장' }));
  await screen.findByText('미리보기에 반영했습니다. 서버에는 저장되지 않습니다.');
  expect(saveProfile).not.toHaveBeenCalled();
});

test('허용되지 않는 사진과 빈 닉네임은 저장을 막는다', async () => {
  show({ preview: true });
  const nickname = await screen.findByLabelText(/활동 닉네임/);
  fireEvent.change(screen.getByLabelText('프로필 사진 파일'), { target: { files: [new File(['x'], 'x.svg', { type: 'image/svg+xml' })] } });
  expect(screen.getByRole('alert')).toHaveTextContent('JPG 또는 PNG');
  fireEvent.change(nickname, { target: { value: ' ' } });
  fireEvent.click(screen.getByRole('button', { name: '변경사항 저장' }));
  expect(screen.getByRole('alert')).toHaveTextContent('닉네임은 1~50자');
});
