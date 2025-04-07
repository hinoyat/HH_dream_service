import { BrowserRouter } from 'react-router-dom';
import AppRouter from './routers/Router';
import { Provider } from 'react-redux';
import { store } from './stores/store';
import { useEffect } from 'react';
import useSSE from './hooks/useSSE';
import api from './apis/apiClient';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const SSEProvider = () => {
  useSSE('/api/notifications/subscribe'); // SSE 연결은 인증된 사용자에게만 적용됨

  return null; // UI를 렌더링하지 않음
};

// 뒤로가기 감지 및 새로고침을 위한 컴포넌트
const BackHandler = () => {
  useEffect(() => {
    const handlePopState = () => {
      window.location.reload();
    };

    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, []);

  return null;
};

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <SSEProvider />
        <BackHandler />
        <AppRouter />
      </BrowserRouter>
      <ToastContainer
        position="bottom-right"
        autoClose={5000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="dark"
        icon={({ type }) => {
          switch (type) {
            case 'success':
              return '🚀';
            case 'error':
              return '❌';
            case 'info':
              return '♥️';
            default:
              return '📢';
          }
        }}
      />
    </Provider>
  );
}

export default App;
