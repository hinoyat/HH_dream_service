import api from "@/apis/apiClient"

export interface UserGuestbookResponse {
    "guestbookSeq": number,
    "ownerSeq":number,
    "writerSeq": number,
    "writerNickname": string,
    "writerIconSeq": number,
    "content": string,
    "createdAt": string,
    "updatedAt": string,
    "deletedAt": null,
    "isDeleted": "N"
}

// 페이지네이션 응답 타입
export interface PaginatedResponse {
    guestbooks: UserGuestbookResponse[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    last: boolean;
}


// 내 방명록 조회
export const getMyGuestbookEntries = async (page: number = 1): Promise<PaginatedResponse | null> => {
    try {
        const response = await api.get<PaginatedResponse>('/guestbook/me',{
            params:{
                page:page
            }
        })
        console.log(response.data)
        return response.data
    } catch (error) {
        console.error("내 방명록 조회 오류", error)
        return null
    }
}




