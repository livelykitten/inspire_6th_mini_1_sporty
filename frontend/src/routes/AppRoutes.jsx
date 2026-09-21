import { Route, Routes, useLocation, useMatch } from 'react-router-dom';
import { GuestOnly, RequireAuth } from './RouteGuards';
import { HomePage, LoginPage, SignupPage, NotFoundPage, PendingPage, isValidId } from './RoutePages';
import ProfileModal from './ProfileModal';
import './routes.css';

export default function AppRoutes() {
  const location = useLocation();
  const profileMatch = useMatch('/profiles/:userId');
  const validProfile = profileMatch && isValidId(profileMatch.params.userId);
  const candidate = location.state?.backgroundLocation;
  const backgroundLocation = validProfile && candidate &&
    typeof candidate.pathname === 'string' && candidate.pathname.startsWith('/') &&
    !candidate.pathname.startsWith('//') && !candidate.pathname.startsWith('/profiles/')
    ? candidate : null;

  return <>
    <Routes location={backgroundLocation || location}>
      <Route path="/" element={<HomePage />} />
      <Route element={<GuestOnly />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
      </Route>
      <Route element={<RequireAuth />}>
        <Route path="/mypage" element={<PendingPage title="마이페이지" />} />
        <Route path="/matches/new" element={<PendingPage title="매치 개설" />} />
        {/* Wire the shared form's edit mode after GET/update contracts are agreed.
            Never render the creation form here: it would POST a new match. */}
        <Route path="/matches/:matchId/edit" element={<PendingPage title="매치 수정" />} />
      </Route>
      <Route path="/matches/:matchId" element={<PendingPage title="매치 상세" />} />
      <Route path="/facilities" element={<PendingPage title="시설 검색" />} />
      <Route path="/facilities/:serviceId" element={<PendingPage title="시설 상세" />} />
      <Route path="/profiles/:userId" element={validProfile ? <HomePage /> : <NotFoundPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
    {validProfile && <ProfileModal userId={profileMatch.params.userId} hasBackground={Boolean(backgroundLocation)} />}
  </>;
}
