import miroVideo from '@/assets/video/miro-video.mp4';
import ButtonBase from '@/domains/diary/components/details/button/ButtonBase';
interface DiaryDetailProps {
  dream_video?: string | null;
  onVideoRetry?: () => void;
  isVideoGenerating?: boolean;
}

const DetailVideo: React.FC<DiaryDetailProps> = ({
  dream_video,
  onVideoRetry,
  isVideoGenerating,
}) => {
  // const videoSrc = dream_video || miroVideo;

  return (
    // <div className="w-full h-auto flex flex-col items-center">
    //   <video
    //     src={videoSrc}
    //     controls
    //     className="w-full rounded-lg shadow-md"
    //   />
    //   {!dream_video && (
    //     <ButtonBase
    //       onClick={onVideoRetry}
    //       disabled={isVideoGenerating}
    //       className={`mt-4 ${isVideoGenerating ? 'opacity-50 cursor-not-allowed' : ''}`}>
    //       {isVideoGenerating ? '생성 중...' : '영상 다시 생성'}
    //     </ButtonBase>
    //   )}
    // </div>

    <div className="w-full h-auto flex flex-col items-center">
      {dream_video ? (
        <video
          src={dream_video}
          controls
          className="w-full rounded-lg shadow-md"
        />
      ) : (
        <ButtonBase
          onClick={onVideoRetry}
          disabled={isVideoGenerating}
          className={` ${isVideoGenerating ? 'opacity-50 cursor-not-allowed' : ''}`}>
          {isVideoGenerating ? '생성 중...' : '영상 다시 생성'}
        </ButtonBase>
      )}
    </div>
  );
};

export default DetailVideo;
