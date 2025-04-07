import { useSelector } from "react-redux";
import { selectUser } from "@/stores/auth/authSelectors";
import { getIconById } from '@/hooks/ProfileIcons';
import { selectDominantEmotion } from "../store/dashboardSelector";




const EmotionMessages = [
  {
    emotion: "공포",
    messages: [
      "요즘 '공포'가 담긴 꿈을 자주 꾸시네요. 마음을 안정시켜 줄 음악을 추천해드릴게요.",
      "무서운 꿈이 잦은 요즘, 편안한 노래로 마음을 다독여보세요.",
      "공포를 느끼는 꿈이 많았네요. 긴장을 풀 수 있는 음악과 함께 해보세요."
    ]
  },
  {
    emotion: "분노",
    messages: [
      "요즘 '공포'가 담긴 꿈을 자주 꾸시네요. 마음을 안정시켜 줄 음악을 추천해드릴게요.",
      "무서운 꿈이 잦은 요즘, 편안한 노래로 마음을 다독여보세요.",
      "공포를 느끼는 꿈이 많았네요. 긴장을 풀 수 있는 음악과 함께 해보세요."
    ]
  },
  {
    emotion: "불안",
    messages: [
      "요즘 '불안'을 자주 느끼는 꿈을 꾸셨네요. 안정에 도움이 되는 노래를 추천해드릴게요.",
      "걱정이 담긴 꿈이 많았네요. 마음을 편하게 해줄 음악을 들어보세요.",
      "불안한 마음이 꿈에 나타났어요. 위로가 되는 음악을 추천할게요."
    ]
  },
  {
    emotion: "슬픔",
    messages: [
      "'슬픔'이 담긴 꿈이 자주 보이네요. 따뜻한 음악으로 위로받아보세요.",
      "슬픈 감정을 많이 느끼셨군요. 마음을 감싸줄 노래를 추천드릴게요.",
      "요즘 꿈에 슬픔이 많네요. 당신의 마음을 이해하는 노래를 골라봤어요."
    ]
  },
  {
    emotion: "평화",
    messages: [
      "꿈에서 '평화'를 자주 느끼셨네요. 그 기분을 이어갈 수 있는 음악을 추천할게요.",
      "평온한 꿈이 많았네요. 마음을 더 부드럽게 해줄 노래를 들어보세요.",
      "꿈속에서 느낀 평화를 현실에서도 이어가보세요. 잔잔한 음악과 함께요."
    ]
  },
  {
    emotion: "행복",
    messages: [
      "행복한 꿈을 자주 꾸셨네요! 그 기분을 유지할 수 있는 음악을 추천할게요.",
      "꿈속의 웃음이 전해지네요. 기분 좋은 노래와 함께 하루를 시작해보세요.",
      "행복이 가득한 꿈이 많았어요. 밝은 에너지의 음악을 들어보세요!"
    ]
  },
  {
    emotion: "희망",
    messages: [
      "'희망'이 담긴 꿈을 많이 꾸셨네요. 그 긍정의 기운을 이어주는 음악을 들려드릴게요.",
      "희망찬 꿈이 많았어요. 당신의 마음을 응원하는 노래를 골라봤어요.",
      "꿈에 나타난 희망, 현실에서도 느껴보세요. 활력을 주는 음악과 함께요."
    ]
  },
      
]


const UserInfoData = ()=>{

    const user = useSelector(selectUser)
    const dominantEmotion = useSelector(selectDominantEmotion)
    console.log("지배감정:", dominantEmotion);

    let emotionMessage = "";

    if (dominantEmotion) {
      // 해당 감정의 메시지 배열 찾기
      const emotionData = EmotionMessages.find(
        item => item.emotion.toLowerCase() === dominantEmotion.toLowerCase()
      );

      console.log("일치하는 감정 데이터:", emotionData);
      
      if (emotionData) {
        // 메시지 배열에서 무작위로 하나 선택
        const randomIndex = Math.floor(Math.random() * emotionData.messages.length);
        emotionMessage = emotionData.messages[randomIndex];
        console.log("랜덤 메세지!!!",emotionMessage)
      }
    }
    

    return(
    <div className="flex items-center mt-7.5">
    <img
      src={getIconById((user as any).iconSeq)}
      alt="프로필 사진"
      className="w-13 h-14"
    />
        <h1 className="text-white/85 text-[23px] ml-3">{user?.nickname}님</h1>
        <div className="p-3 bg-gray-800 rounded-lg text-[23px] text-white/85">{emotionMessage}</div>
  </div>
);
}
export default UserInfoData