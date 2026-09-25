import { render, screen } from '@testing-library/react';
import MatchCard from './MatchCard';
import { toMatchCard } from '../api/aiMatchApi';

test.each([
  [0, 12, 0, 'blue'],
  [2, 12, 2 / 12 * 100, 'blue'],
  [3, 12, 25, 'green'],
  [7, 12, 7 / 12 * 100, 'green'],
  [9, 12, 75, 'green'],
  [10, 12, 10 / 12 * 100, 'red'],
  [12, 12, 100, 'red'],
  [13, 12, 100, 'red'],
])('shows %s/%s participants with matching progress and color', (currentParticipant, maxParticipant, percent, color) => {
  const match = toMatchCard({ matchId: 1, title: '풋살', currentParticipant, maxParticipant });
  render(<MatchCard match={match} />);
  expect(screen.getByText(`참여 인원 ${currentParticipant}/${maxParticipant}명`)).toBeInTheDocument();
  const progress = screen.getByRole('progressbar');
  expect(progress).toHaveAttribute('aria-valuenow', String(percent));
  expect(progress).toHaveClass(`ms-progress-${color}`);
  expect(progress.firstChild).toHaveStyle({ width: `${percent}%` });
});

test.each([[undefined, 12], [7, 0], [-1, 12], [7, undefined]])('does not invent progress for invalid counts (%s, %s)', (currentParticipant, maxParticipant) => {
  render(<MatchCard match={{ currentParticipant, maxParticipant }} />);
  expect(screen.getByText('참가 인원 정보 없음')).toBeInTheDocument();
  expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
});
