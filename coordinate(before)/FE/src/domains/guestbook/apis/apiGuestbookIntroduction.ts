import api from "@/apis/apiClient";


export interface UserIntroductionResponse {
    "userSeq": number,
    "username": string,
    "nickname": string,
    "birthDate": string,
    "introduction": string,
    "iconSeq": number
}

export const putGuestbookIntroduction = async(data:{introduction:string}): Promise<UserIntroductionResponse[]|null> => {
    try{
        const response = await api.put('/users/me/intro', data)
        console.log("방명록 소개글 데이터", response.data)
        return response.data
    }catch(error){
        console.error("방명록 소개글 수정 오류", error)
        return null
    }
}