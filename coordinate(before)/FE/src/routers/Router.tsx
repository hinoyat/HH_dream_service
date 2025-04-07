import { Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from '../common/Layout'; // Layout 컴포넌트 import
import OtherMainPage from '@/domains/mainpage/pages/OtherMainPage';
import ProtectedRoute from '@/common/ProtectedRoute';
import PublicRoute from '@/common/PublicRoute';
import SlashPage from '@/domains/intro/pages/SlashPage';

const Main = lazy(() => import('@/domains/intro/pages/SlashPage'));
const Loading = lazy(() => import('../domains/Loading/Loading'));
const ErrorPage = lazy(() => import('../domains/error/ErrorPage'));
const MainPage = lazy(() => import('../domains/mainpage/pages/MainPage'));
const Login = lazy(() => import('../domains/login/pages/Login'));
const Signup = lazy(() => import('../domains/signup/pages/Signup'));
const GuestBook = lazy(() => import('../domains/guestbook/GuestBook'));
const DiaryComponent = lazy(
  () => import('../domains/diary/modals/DiaryComponent')
);
const DiaryDetail = lazy(() => import('../domains/diary/modals/DiaryDetail'));
const SpaceShip = lazy(() => import('../domains/spaceship/pages/SpaceShip'));
const PasswordCheck = lazy(
  () => import('../domains/spaceship/pages/PasswordCheck')
);
const MyInformation = lazy(
  () => import('../domains/myinformation/pages/MyInformation')
);
const SuccessfulEdit = lazy(
  () => import('../domains/myinformation/pages/SuccessfulEdit')
);
const FailEdit = lazy(() => import('../domains/myinformation/pages/FailEdit'));
const LuckyNumber = lazy(
  () => import('../domains/luckyNumber/pages/LuckyNumber')
);
const TodayFortune = lazy(
  () => import('../domains/todayFortune/pages/TodayFortune')
);
const Ggumplaylist = lazy(
  () => import('../domains/dashboard/pages/Ggumplaylist')
);
const Intro = lazy(() => import('../domains/intro/pages/Intro'));

const AppRouter = () => {
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        {/* Layout이 필요한 라우트 */}
        <Route element={<Layout />}>
          {/* 로그인 보호 필요 */}
          <Route element={<ProtectedRoute />}>
            <Route
              path="/:username"
              element={<MainPage />}
            />
            {/* <Route
              path="/othermain"
              element={<OtherMainPage />}
            /> */}
            <Route
              path="/spaceship"
              element={<SpaceShip />}
            />
            <Route
              path="/luckynumber"
              element={<LuckyNumber />}
            />
            <Route
              path="/todayfortune"
              element={<TodayFortune />}
            />
            <Route
              path="/ggumplaylist"
              element={<Ggumplaylist />}
            />
            <Route
              path="/diary/create"
              element={<DiaryComponent />}
            />
            <Route
              path="/diary/edit/:id"
              element={<DiaryComponent isEditing={true} />}
            />
            <Route
              path="/diary/:id"
              element={<DiaryDetail />}
            />
          </Route>
        </Route>

        {/* Navbar 없이 로그인 보호받아야 할 곳*/}
        <Route element={<ProtectedRoute />}>
          <Route
            path="/myinformation"
            element={<MyInformation />}
          />
          <Route
            path="/passwordcheck"
            element={<PasswordCheck />}
          />
          <Route
            path="/successedit"
            element={<SuccessfulEdit />}
          />
          <Route
            path="/failedit"
            element={<FailEdit />}
          />
        </Route>

        {/* 비로그인 유저, navbar 없이 */}
        <Route element={<PublicRoute />}>
          <Route
            path="/login"
            element={<Login />}
          />
          <Route
            path="/"
            element={<SlashPage />}
          />

          <Route
            path="/intro"
            element={<Intro />}
          />
          <Route
            path="/signup"
            element={<Signup />}
          />
          <Route
            path="*"
            element={<ErrorPage />}
          />
        </Route>
      </Routes>
    </Suspense>
  );
};

export default AppRouter;
