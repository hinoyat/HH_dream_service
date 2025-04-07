import { Outlet, useLocation } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectIsAuthenticated, selectUser } from '@/stores/auth/authSelectors';

const PublicRoute = () => {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);

  return isAuthenticated ? (
    // 메인으로 이동
    <Navigate
      to={`${user?.username}`}
      replace
    />
  ) : (
    <Outlet />
  );
};

export default PublicRoute;
