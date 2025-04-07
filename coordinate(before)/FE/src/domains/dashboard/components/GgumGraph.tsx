import { useSelector } from "react-redux";
import { selectUser } from "@/stores/auth/authSelectors";
import { useEffect, useRef } from "react";
import Highcharts from 'highcharts';


interface GgumGrapProps {
    periodName?: number;
    height?: number | string;
    data: { emotion: string; count: number }[]
}

const GgumGraph = ({periodName=30, height = "50%", data }: GgumGrapProps)=>{
    const user = useSelector(selectUser)
    const chartRef = useRef(null);
    const chartId = useRef(`chart-${Math.random().toString(36).substr(2, 9)}`);

        // 샤이닝 효과를 위한 스타일 추가
        useEffect(() => {
            const style = document.createElement('style');
            style.innerHTML = `
            .chart-title-shining {
                text-shadow: 0 0 10px rgba(16, 154, 109, 0.8), 
                            0 0 15px rgba(16, 154, 109, 0.6), 
                            0 0 20px rgba(16, 154, 109, 0.4);
            }
            `;
            document.head.appendChild(style);
            
            return () => {
                document.head.removeChild(style);
            };
        }, []);
    

    useEffect(()=>{
        if (!chartRef.current) 
            return;
        
        const emotions = data.map(item => item.emotion)
        const emotionsCounts = data.map(item=>item.count)
        console.log('그래프 데이터:', emotions, emotionsCounts);

        const periodMapping = {
            "14": "이주일",
            "30": "한 달"
          };

        // 숫자 값을 한글로 변환하는 함수
        const getPeriodNameInKorean = (periodName: number) => {
            return periodMapping[periodName] || `${periodName}`;
        };
           
        Highcharts.chart(chartRef.current, {
            chart: {
                backgroundColor: '#000000',  // 검은색 배경 설정
                height: height,
                spacingTop: 1,
                events: {
                    load: function() {
                        // 차트 로드 후에 타이틀 요소를 찾아서 클래스 추가
                        setTimeout(() => {
                            const titleEl = document.querySelector(`#${chartRef.current.id} .highcharts-title`);
                            if (titleEl) {
                                titleEl.classList.add('chart-title-shining');
                            }
                        }, 100);
                    }
                }
            },

            title: {
                text: `꿈결 따라 흐른 감정들 – 최근 ${getPeriodNameInKorean(periodName)}간의 꿈 그래프`,
                style: {
                    color: '#109a6d',
                    fontFamily: 'DungGeunMo',
                }
            },
        
            accessibility: {
                point: {
                    valueDescriptionFormat:
                        '{xDescription}{separator}{value} million(s)'
                }
            },
        
            xAxis: {
                categories: emotions,
                labels: {
                    style: {
                        color: '#FFFFFF'  // 흰색 x축 레이블
                    }
                },
                lineColor: '#FFFFFF'  // 흰색 x축 선
            },
        
            yAxis: {
                title:{
                    text:''
                },
                labels: {
                    style: {
                        color: '#FFFFFF'  // 흰색 y축 레이블
                    }
                },
                gridLineColor: 0  // 그리드 라인 제거
            },
        
            tooltip: {
                formatter: function() {
                    return `<b>${this.category} 감정의 꿈</b><br/>${this.y}개`;
                },
                style: {
                    textAlign: 'center',
                    fontWeight: 'bold'
                },
                useHTML: true 
            },
        
            series: [{
                name: '꿈 기록 수',
                data: emotionsCounts,
                color: '#FFFFFF',
                marker: {
                    enabled: true, // 포인트를 표시하도록 설정
                    radius: 5, // 포인트의 크기
                    fillColor: '#ffffff', // 포인트 색상을 흰색으로 설정
                    lineWidth: 2, // 포인트의 테두리 두께
                    lineColor: '#ffffff', // 포인트 테두리 색상
                    states: {
                        hover: {
                            enabled: true,
                            fillColor: '#ffffff', // 호버 시 포인트 색상
                            lineColor: '#ffffff', // 호버 시 테두리 색상
                            radius: 7 // 호버 시 포인트 크기 증가
                        }
                    },
                    shadow: {
                        color: 'rgba(255, 255, 255, 0.5)', // 그림자 색상
                        offsetX: 0,
                        offsetY: 0,
                        opacity: 1,
                        width: 10 // 그림자 크기
                    }
                }
            }],
            legend: {
                enabled: false  // 범례 완전히 제거
            },
            credits: {
                enabled: false  
            }
        });
        })
    
    
    
    return(
        <div id={chartId.current} ref={chartRef}>

        </div>
    )
}
export default GgumGraph
