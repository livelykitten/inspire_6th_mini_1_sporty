import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the main page at the root route', () => {
  window.history.replaceState({}, '', '/');
  render(<App />);
  expect(screen.getByText('메인 페이지')).toBeInTheDocument();
});
