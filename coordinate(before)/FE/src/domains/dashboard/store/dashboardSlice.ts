import { createSlice } from "@reduxjs/toolkit";
import { DashboardGraphState } from "./dashboardTypes"; 
import { fetchFourteenDaysEmotionStats, fetchThirtyDaysEmotionStats } from "./dashboardThunks";


// 초기 상태 설정: 대시보드가 처음 로드될 때의 상태
const initialState: DashboardGraphState = {
    twoWeeksData : null,                       // 초기에는 데이터없음 
    monthlyData: null,                         // 초기에는 데이터없음 
    loading: false,                            // 초기에는 로딩 중이 아님
    error: null,                               // 초기에는 오류 없음
    dominantEmotion: null,                     // 초기에는 주 감정 없음
  };


const dashboardSlice = createSlice({

    name: 'DashboardGraphEmotion',
    initialState,
    reducers:{
    },

    extraReducers: (builder) =>{
      
      // ---- 14일 데이터 처리 로직 -----
      // 14일 데이터 로딩 시작 시

  
      builder.addCase(fetchFourteenDaysEmotionStats.pending, (state)=> {
        state.loading = true;    // 로딩 상태를 true로 설정
        state.error = null;      // 이전 오류 메시지를 초기화
        console.log("시작시 state가 뭔지 궁금해요!!!", JSON.parse(JSON.stringify(state)))
      })

      // 14일 데이터 로드 성공 시
      .addCase(fetchFourteenDaysEmotionStats.fulfilled, (state,action)=> {
        state.loading = false;   // 로딩 완료

        // API 응답에서 감정 데이터 배열을 추출
        const emotionData = action.payload.data.data;      
        console.log("로드성공시 state가 뭔지 궁금해요!!!", JSON.parse(JSON.stringify(state)))

        const formattedData = {} as Record<string, number>;
        let maxCount = 0;
        let dominantEmotionName = '';

        // 한 번의 순회로 객체 변환과 지배적 감정 찾기 동시에 수행
        emotionData.forEach(item => {
          formattedData[item.emotion] = item.count;
          
          // 현재 감정의 카운트가 지금까지의 최대값보다 크면 업데이트
          if (item.count > maxCount) {
            maxCount = item.count;
            dominantEmotionName = item.emotion;
          }
        });
        
        state.twoWeeksData = formattedData;
        state.dominantEmotion = dominantEmotionName;

      })

      // 
      .addCase(fetchFourteenDaysEmotionStats.rejected, (state, action)=>{
          state.loading = false;
          state.error = action.payload as string;   
      })


      // ---- 30일 데이터 처리 로직 -----
      // 30일 데이터 로딩 시작
      builder.addCase(fetchThirtyDaysEmotionStats.pending, (state)=>{
        state.loading = true;
        state.error = null;
      })

        // 30일 데이터 로드 성공
        .addCase(fetchThirtyDaysEmotionStats.fulfilled, (state, action)=>{

          // 로딩 중 상태 변경
          state.loading = false;

          // 데이터 저장하기
          const emotionData = action.payload.data.data;
   
          const formattedData = emotionData.reduce((acc, item) => {
              acc[item.emotion] = item.count;
              return acc;
          }, {} as Record<string, number>);
          
          state.monthlyData = formattedData;
          
        })

        // 30일 데이터 로드 실패
        builder.addCase(fetchThirtyDaysEmotionStats.rejected, (state, action)=>{
          state.loading = false;
          state.error = action.payload as string;   
        })
    }
});

export default dashboardSlice.reducer;
