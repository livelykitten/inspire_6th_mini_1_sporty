import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';

beforeEach(() => {
  window.history.replaceState({}, '', '/');
});

test('renders the main page at the root route', () => {
  render(<App />);
  expect(screen.getByRole('heading', { level: 1, name: /자연어로 말하듯 검색하면/ })).toBeInTheDocument();
});

test('opens facility search with the entered query', () => {
  render(<App />);
  userEvent.type(screen.getByRole('searchbox'), '강남 & 풋살');
  userEvent.click(screen.getByRole('button', { name: '검색', exact: true }));
  expect(screen.getByText('시설 검색')).toBeInTheDocument();
  expect(window.location.pathname).toBe('/facilities');
  expect(new URLSearchParams(window.location.search).get('query')).toBe('강남 & 풋살');
});

test('opens facility search without a query when submitted with Enter', () => {
  render(<App />);
  userEvent.type(screen.getByRole('searchbox'), '{enter}');
  expect(screen.getByText('시설 검색')).toBeInTheDocument();
  expect(window.location.pathname).toBe('/facilities');
  expect(window.location.search).toBe('');
});

test('opens the match search page from the floating link', () => {
  render(<App />);
  userEvent.click(screen.getByRole('link', { name: '전체 3건 매치 목록 보러가기' }));
  expect(screen.getByText('매치 검색')).toBeInTheDocument();
  expect(window.location.pathname).toBe('/matches/search');
});
