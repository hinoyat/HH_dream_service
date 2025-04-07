import { useDispatch, useSelector, TypedUseSelectorHook} from 'react-redux';
import type { AppDispatch, RootState } from '@/stores/store';

// Redux Toolkit Thunk를 지원하는 `useAppDispatch` 생성
export const useAppDispatch: () => AppDispatch = useDispatch;

// 타입이 지정된 `useAppSelector` 생성
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
