import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectIsAuthenticated } from '@/stores/auth/authSelectors';

const ProtectedRoute = () => {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const location = useLocation();
  
  // 직접 URL 접근인지 확인 (location.key가 없거나 'default'인 경우 직접 접근으로 간주)
  const isDirectAccess = !location.key || location.key === 'default';
  
  if (!isAuthenticated) {
    // 직접 URL로 접근한 경우 홈('/')으로 리다이렉트
    if (isDirectAccess) {
      return <Navigate to="/" replace />;
    }
    // 일반적인 경우(앱 내 네비게이션) intro로 리다이렉트
    return <Navigate to="/intro" replace />;
  } else{
    if (isDirectAccess) {
      return <Navigate to="/" replace />;
    }
  }

  return <Outlet />;
};

export default ProtectedRoute;