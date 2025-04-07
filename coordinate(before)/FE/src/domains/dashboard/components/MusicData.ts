// 이미지 파일
import happy1 from '@/assets/music/happy/LifeIsPiano.mp3'
import happy2 from '@/assets/music/happy/Duggy - Brilliant World.mp3'
import happy3 from '@/assets/music/happy/LikeAStrong.mp3'
import hope1 from '@/assets/music/hope/Dandelion.mp3'
import hope2 from '@/assets/music/hope/Lullaby.mp3'
import hope3 from '@/assets/music/hope/Sono.mp3'
import peace1 from '@/assets/music/peace/InLoveWithAGhost.mp3'
import peace2 from '@/assets/music/peace/Midway.mp3'
import peace3 from '@/assets/music/peace/PaleEvening.mp3'


// 이미지 파일 
import happyImg1 from '@/assets/music/happy/LifeIsPianoImg.jpg'
import happyImg2 from '@/assets/music/happy/DuggyImg.jpg'
import happyImg3 from '@/assets/music/happy/LikeImg.jpg'
import hopeImg1 from '@/assets/music/hope/DandelionImg.jpg'
import hopeImg2 from '@/assets/music/hope/CrystalImg.jpg'
import hopeImg3 from '@/assets/music/hope/SonoImg.jpg'
import peaceImg1 from '@/assets/music/peace/InLoveImg.jpg'
import peaceImg2 from '@/assets/music/peace/Midway.jpg'
import peaceImg3 from '@/assets/music/peace/PaleEvening.jpg'



export interface MusicTrack {
    id: string;
    title: string;
    audio: string;
    image: string;
    duration: string;
}

export interface MusicCategory{
    name: string;
    tracks: MusicTrack[]
}


export const MusicList : MusicCategory[]= [
    {
        name: 'happy',
        tracks: [
            {
                id: "happy1",
                title: "Life is piano",
                audio: happy1,
                image: happyImg1,
                duration: "2:09"
            },
            {
                id: "happy2",
                title: "Duggy",
                audio: happy2,
                image: happyImg2,
                duration: "3:03"

            },
            {
                id: "happy3 ",
                title: "Like a strong",
                audio: happy3,
                image: happyImg3,
                duration: "3:38"
            },
        ],        
    },
    {
        name: 'hope',
        tracks: [
            {
                id: "hope1",
                title: "Dandelion",
                audio: hope1,
                image: hopeImg1,
                duration: "2:24"
            },
            {
                id: "hope2",
                title: "Lullaby",
                audio: hope2,
                image: hopeImg2,
                duration: "3:28"
            },
            {
                id: "hope3",
                title: "Sono",
                audio: hope3,
                image: hopeImg3,
                duration: "3:46"
            }
        ]
    },
    {
        name: 'peace',
        tracks: [
            {
                id: "peace1",
                title: "In Love with a ghost",
                audio: peace1,
                image: peaceImg1,
                duration: "4:12"
            },
            {
                id: "peace2",
                title: "Midway",
                audio: peace2,
                image: peaceImg2,
                duration: "6:33"

            },
            {
                id: "peace3",
                title: "Pale evening",
                audio: peace3,
                image: peaceImg3,
                duration: "2:40"
            }
        ] 
    }
]