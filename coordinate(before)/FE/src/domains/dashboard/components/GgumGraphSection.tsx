import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch } from '@/stores/store'
import GgumGraph from './GgumGraph';
import { fetchFourteenDaysEmotionStats, fetchThirtyDaysEmotionStats } from '../store/dashboardThunks'; 
import { selectTwoWeeksData, selectMonthlyData, selectLoading, selectError } from '../store/dashboardSelector';



const GraphSection = () => {

  const dispatch = useDispatch<AppDispatch>();           // 타입 에러로 인해 명확한 타입 명시
  const loading = useSelector(selectLoading)
  const error = useSelector(selectError)
  const twoWeeksData = useSelector(selectTwoWeeksData)
  const monthlyData = useSelector(selectMonthlyData)
  console.log(twoWeeksData)
  console.log(monthlyData)

  useEffect(() => {
    // 컴포넌트 마운트 시 데이터 로드하기
    dispatch(fetchFourteenDaysEmotionStats())
    dispatch(fetchThirtyDaysEmotionStats())

  }, [dispatch]);

  if (loading) return <div className="flex justify-center items-center h-full">로딩 중...</div>;
  if (error) return <div className="text-red-500">{error}</div>;
  if (!twoWeeksData || !monthlyData) return <div>데이터가 없습니다.</div>;

  // 객체 형태를 배열 형태로 변환
  const twoWeeksDataArray = Object.entries(twoWeeksData).map(([emotion, count]) => ({
    emotion,
    count
  }));

  const monthlyDataArray = Object.entries(monthlyData).map(([emotion, count]) => ({
    emotion,
    count
  }));

  return (
            <div className="flex flex-col pl-30 pt-15 w-[47vw] gap-23 ">
            <div className="flex flex-col">
                <GgumGraph 
                    periodName={14}  
                    height="42%"
                    data={twoWeeksDataArray}
                     />
            </div>
            <div className="flex flex-col">
                <GgumGraph 
                    periodName={30}  
                    height="42%"
                    data={monthlyDataArray}
                     />
            </div>
          </div> 
  );
};

export default GraphSection;
