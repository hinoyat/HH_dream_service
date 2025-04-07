import { useState, useRef, useEffect } from 'react';
import { useSelector, useDispatch } from "react-redux";
import { selectDominantEmotion } from '../store/dashboardSelector';
import { selectMusicVolume, selectPlaylistPlaying } from '@/stores/music/musicSelectors';
import { startPlaylistMusic, stopPlaylistMusic, setAsBackgroundMusic } from '@/stores/music/musicThunks';
import { MusicList } from "./MusicData"
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBackward, faPlay, faStop, faForward, faMusic } from '@fortawesome/free-solid-svg-icons';
import 'font-awesome/css/font-awesome.min.css';

interface Song {
  name: string;
  category: string;
  img: string;
  audio: string;
  duration: string;
}

const flattenedMusicList: Song[] = MusicList.flatMap(category => 
  category.tracks.map(track => ({
    name: track.title,
    category: category.name, 
    img: track.image,
    audio: track.audio,
    duration: track.duration 
  }))
);

// 지배감정에 따른 음악 카테고리 매핑
const emotionToCategoryMap: { [key: string]: string } = {
  "행복": "happy",
  "희망": "happy",
  "평화": "hope",
  "슬픔": "hope",
  "불안": "peace",
  "분노": "peace",
  "공포": "peace"
};



const MusicPlaylist: React.FC = () => {
  const dispatch = useDispatch();
  const dominantEmotion = useSelector(selectDominantEmotion)
  const volume = useSelector(selectMusicVolume);
  const isPlaylistPlaying = useSelector(selectPlaylistPlaying);

  // 지배감정에 해당하는 카테고리 가져오기
  const categoryToShow = dominantEmotion ? emotionToCategoryMap[dominantEmotion] : "happy"; // 기본값 설정


  // 카테고리에 맞는 음악 목록 필터링
  const currentEmotionMusicList = flattenedMusicList.filter(song => 
    song.category.toLowerCase() === categoryToShow
  );

  // 필터링된 목록이 비어있으면 전체 목록 사용
  const displayMusicList = currentEmotionMusicList.length > 0 ? currentEmotionMusicList : flattenedMusicList;


  const [index, setIndex] = useState<number>(0);
  const [currentTime, setCurrentTime] = useState<string>('0:00');
  const [pause, setPause] = useState<boolean>(false);
  
  const playerRef = useRef<HTMLAudioElement>(null);
  const timelineRef = useRef<HTMLDivElement>(null);
  const playheadRef = useRef<HTMLDivElement>(null);
  
  // 플레이리스트 컨테이너에 대한 ref 추가
  const playlistContainerRef = useRef<HTMLDivElement>(null);

  // 첫 렌더링 여부를 추적하는 상태 추가
  const firstRenderRef = useRef(true);

  // 컴포넌트 마운트 시 배경음악 일시정지하고 플레이리스트 음악 활성화
  useEffect(() => {
   
    // 컴포넌트 언마운트 시 배경음악 재개
    return () => {
      dispatch(stopPlaylistMusic());
    };
  }, [dispatch]); // 컴포넌트 마운트/언마운트 시에만 실행



  // 볼륨 변경 시 현재 플레이어 볼륨도 변경
  useEffect(() => {
    if (playerRef.current) {
      playerRef.current.volume = volume;
    }
  }, [volume]);


  useEffect(() => {
    if (!playerRef.current || !timelineRef.current || !playheadRef.current ) return;
    
    playerRef.current.addEventListener("timeupdate", timeUpdate);
    playerRef.current.addEventListener("ended", nextSong);
    timelineRef.current.addEventListener("click", changeCurrentTime);

    return () => {
      if (!playerRef.current || !timelineRef.current) return;
      
      playerRef.current.removeEventListener("timeupdate", timeUpdate);
      playerRef.current.removeEventListener("ended", nextSong);
      timelineRef.current.removeEventListener("click", changeCurrentTime);
    };
  }, []);

  useEffect(() => {

      // 첫 렌더링인 경우 무시
    if (firstRenderRef.current) {
      firstRenderRef.current = false;
      return;
    }

    updatePlayer();
    
    if (playlistContainerRef.current) {
      const selectedElement = playlistContainerRef.current.querySelector(`[data-index="${index}"]`);
      if (selectedElement) {
        selectedElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    }
  }, [index]);

  const updatePlayer = (): void => {
    if (!playerRef.current) return;
    playerRef.current.load();

    // 재생 중이었다면 새 곡도 재생
    if (!pause) {
      // load 이후에 재생하려면 약간의 지연이 필요할 수 있음
      setTimeout(() => {
        if (playerRef.current) {
          playerRef.current.play();
          // 현재 플레이리스트 음악 경로도 업데이트
          const actionCreator = startPlaylistMusic(displayMusicList[index].audio);
          dispatch(actionCreator);
        }
      }, 100);
    }
  };

  // 현재 음악을 배경음악으로 설정
  const setCurrentAsBackground = (): void => {
    const currentSong = displayMusicList[index];
    dispatch(setAsBackgroundMusic(currentSong.audio));
    // 설정 완료 알림 (선택사항)
    alert(`${currentSong.name}이(가) 배경음악으로 설정되었습니다.`);
  };


  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    let secs = Math.floor(seconds % 60);
    const formattedSeconds = secs >= 10 ? secs.toString() : "0" + secs.toString();
    return `${minutes}:${formattedSeconds}`;
  };

  const timeUpdate = (): void => {
    if (!playerRef.current || !playheadRef.current || !timelineRef.current) return;
    
    const duration = playerRef.current.duration;
    const playPercent = 100 * (playerRef.current.currentTime / duration);
    playheadRef.current.style.width = `${playPercent}%`;
    
    const currentTimeFormatted = formatTime(playerRef.current.currentTime);
    setCurrentTime(currentTimeFormatted);
  };

  const changeCurrentTime = (e: MouseEvent): void => {
    if (!playerRef.current || !playheadRef.current || !timelineRef.current) return;
    
    const duration = playerRef.current.duration;
    const playheadWidth = timelineRef.current.offsetWidth;
    const offsetWidth = timelineRef.current.offsetLeft;
    const userClickWidth = e.clientX - offsetWidth;
    
    const userClickWidthInPercent = (userClickWidth * 100) / playheadWidth;
    
    playheadRef.current.style.width = `${userClickWidthInPercent}%`;
    playerRef.current.currentTime = (duration * userClickWidthInPercent) / 100;
  };



  const nextSong = (): void => {
    setIndex((prevIndex) => (prevIndex + 1) % displayMusicList.length);
    
    if (!pause && playerRef.current) {
      playerRef.current.play();
    }
  };

  const prevSong = (): void => {
    setIndex((prevIndex) => (prevIndex + displayMusicList.length - 1) % displayMusicList.length);
    
    if (!pause && playerRef.current) {
      playerRef.current.play();
    }
  };

  const playOrPause = (): void => {
    if (!playerRef.current) return;
    
    if (!pause) {
      playerRef.current.pause();
    } else {
      playerRef.current.play();
    }
    
    setPause(!pause);
  };

  const clickAudio = (key: number): void => {
    setIndex(key);
    
    // 항상 재생되도록 수정
    if (playerRef.current) {
      // 곡이 바뀌었으므로 다시 로드
      playerRef.current.load(); 
      
      // 로드 후 약간의 지연을 두고 재생
      setTimeout(() => {
        if (playerRef.current) {
          playerRef.current.play();
          // 일시정지 상태였다면 재생 상태로 변경
          if (pause) {
            setPause(false);
          }
          // Redux 상태 업데이트
          dispatch(startPlaylistMusic(displayMusicList[key].audio));
        }
      }, 100);
    }
  };

  const currentSong = displayMusicList[index];

  // SVG 데이터 URL
  const svgUrl = "data:image/svg+xml,%3Csvg version='1.1' id='Capa_1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' viewBox='0 0 56 56' style='enable-background:new 0 0 56 56;' xml:space='preserve'%3E%3Cpath style='fill:%23071739;' d='M47.799,8.201c-10.935-10.935-28.663-10.935-39.598,0c-10.935,10.935-10.935,28.663,0,39.598 c10.935,10.935,28.663,10.935,39.598,0C58.734,36.864,58.734,19.136,47.799,8.201z M32.95,32.95c-2.734,2.734-7.166,2.734-9.899,0 c-2.734-2.734-2.734-7.166,0-9.899s7.166-2.734,9.899,0S35.683,30.216,32.95,32.95z'/%3E%3Cpath style='fill:%23E7ECED;' d='M35.778,20.222c-4.296-4.296-11.261-4.296-15.556,0c-4.296,4.296-4.296,11.261,0,15.556 c4.296,4.296,11.261,4.296,15.556,0C40.074,31.482,40.074,24.518,35.778,20.222z M30.121,30.121c-1.172,1.172-3.071,1.172-4.243,0 s-1.172-3.071,0-4.243s3.071-1.172,4.243,0S31.293,28.95,30.121,30.121z'/%3E%3Cg%3E%3Cpath style='fill:%23709fdc;' d='M35.778,35.778c-0.76,0.76-1.607,1.378-2.504,1.87l8.157,14.92c2.284-1.25,4.434-2.835,6.368-4.769 c1.934-1.934,3.519-4.084,4.769-6.368l-14.92-8.157C37.157,34.172,36.538,35.018,35.778,35.778z'/%3E%3Cpath style='fill:%23709fdc;' d='M20.222,20.222c0.76-0.76,1.607-1.378,2.504-1.87l-8.157-14.92c-2.284,1.25-4.434,2.835-6.368,4.769 s-3.519,4.084-4.769,6.368l14.92,8.157C18.843,21.828,19.462,20.982,20.222,20.222z'/%3E%3C/g%3E%3C/svg%3E";

  return (
    <div className="absolute w-[93%] h-[88%] top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 mt-15 pl">
      <div className="flex flex-col justify-center items-center max-w-[380px] py-0 px-[5px] mt-[0px] ml-[4vw] rounded-[20px] text-white font-light shadow-[0px_0px_70px_0px_#274684] bg-[#071739] overflow-hidden">
        <div className="flex flex-col items-center w-full py-[20px] px-0 rounded-[20px] text-[#071739] bg-white">
          <audio ref={playerRef}>
            <source src={currentSong.audio} type="audio/ogg" />
            Your browser does not support the audio element.
          </audio>
          
          <div className="relative mx-auto w-[280px] h-[210px] overflow-hidden rounded-[20px] shadow-[0px_10px_40px_0px_rgba(39,70,132,0.7)]">
            {/* 배경음악으로 설정하는 버튼을 사진 위에 절대 위치로 배치 */}
            <button 
              onClick={setCurrentAsBackground}
              className="absolute top-[8px] left-1/2 transform -translate-x-1/2 text-xs z-10 flex items-center justify-center px-[15px] py-[8px] rounded-[20px] bg-[#709fdc] text-white transition-[0.2s] cursor-pointer hover:bg-[#4d7fd8]"
            >
              <FontAwesomeIcon icon={faMusic} className="mr-[5px]" />
              배경음악으로 설정
            </button>
            
            <img src={currentSong.img} alt={currentSong.name} className="w-auto h-full" />
          </div>
          
          <span className="mt-[30px] text-[22px]">{currentSong.name}</span>
          <span className="text-[#709fdc]">{currentSong.category}</span>
          
          <div className="flex justify-between mt-[10px] w-[240px]">
            <div>{currentTime}</div>
            <div>{currentSong.duration}</div>
          </div>
          
          <div ref={timelineRef} className="relative mx-auto w-[240px] h-[5px] bg-[#709fdc] rounded-[5px] cursor-pointer hover:[&_.hover-playhead]:opacity-100">
            <div ref={playheadRef} className="relative z-[2] w-0 h-[5px] rounded-[5px] bg-[#071739]"></div>
          </div>
          
          <div className="mt-[10px]">
            <button onClick={prevSong} className="text-[#071739] rounded-full mx-[15px] text-[18px] text-center transition-[0.2s] cursor-pointer border-none bg-[transparent] focus:outline-none w-[35px] h-[35px] hover:transform hover:scale-[1.2]">
               <FontAwesomeIcon icon={faBackward} />
            </button>
            
            <button onClick={playOrPause} className="text-[#071739] rounded-full mx-[15px] text-[18px] text-center transition-[0.2s] cursor-pointer border border-[#e2e2e2] bg-[transparent] focus:outline-none w-[50px] h-[50px] hover:left-0 hover:shadow-[0px_0px_15px_0px_rgba(39,70,132,0.7)]">
              {!pause ? (
                <FontAwesomeIcon icon={faPlay} />
              ) : (
                <FontAwesomeIcon icon={faStop} />
              )}
            </button>
            
            <button onClick={nextSong} className="text-[#071739] rounded-full mx-[15px] text-[18px] text-center transition-[0.2s] cursor-pointer border-none bg-[transparent] focus:outline-none w-[35px] h-[35px] hover:transform hover:scale-[1.2]">
                <FontAwesomeIcon icon={faForward} />
            </button>
          </div>
        </div>
        
        <div
          ref={playlistContainerRef} 
          className="flex flex-col p-[10px] h-[193px] overflow-y-scroll [&::-webkit-scrollbar]:w-[5px] [&::-webkit-scrollbar-thumb]:bg-white [&::-webkit-scrollbar-thumb]:rounded-[5px] [&::-webkit-scrollbar-track]:bg-[#071739]">
          {displayMusicList.map((music, key) => (
            <div
              key={key}
              data-index={key}
              className={`
                flex items-center mb-[10px] rounded-[10px] border border-transparent transition-[0.3s] cursor-pointer
                hover:bg-[#274684] hover:border-[#274684] hover:relative
                ${index === key && !pause ? 'bg-[#274684] shadow-[0px_0px_15px_0px_#274684]' : ''}
                ${index === key && pause ? `bg-[#274684] shadow-[0px_0px_15px_0px_#274684] relative after:content-[''] after:block after:absolute after:left-[17px] after:w-[57px] after:h-[57px] after:rounded-[10px] after:text-[16px] after:animate-[play_2s_linear_infinite] after:bg-[url("${svgUrl}")]` : ''}
              `}
            >
              {/* 음악 재생 클릭 영역 */}
              <div 
              className="flex flex-1 items-center cursor-pointer"
              onClick={(e) => {
                e.stopPropagation(); // 이벤트 버블링 방지
                clickAudio(key);
              }}
              >
              <img 
                className={`w-[90px] rounded-[10px] ${index === key && pause ? 'opacity-[70%]' : ''}`} 
                src={music.img} 
                alt={music.name} 
              />
              <div className="ml-[15px] flex flex-col min-w-[190px]">
                <span className="text-[17px] mt-[8px]">{music.name}</span>
                <span className="mt-[8px] font-[300] text-[#709fdc]">{music.category}</span>
              </div>
              <span className="min-w-[40px] ml-[10px] mr-[10px] font-[500]">
                {index === key ? currentTime : music.duration}
              </span>
            </div>
          </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default MusicPlaylist;