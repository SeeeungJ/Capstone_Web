const slideFrame = document.querySelector('.slide-track');
const prevBtn = document.querySelector('.prev');
const nextBtn = document.querySelector('.next');
const slides = document.querySelectorAll('.session1-img');
const totalSlides = slides.length;
let currentSlide = 0;

// 슬라이드를 이동시키는 함수
const moveSlide = (index) => {
    slideFrame.style.transform = `translateX(${-index * 100}%)`; // 슬라이드를 이동시키기
    currentSlide = index;
    console.log(`Slide moved to index: ${index}`); // 디버깅을 위한 로그
};

// 자동 슬라이드 설정 (5초마다 이동)
setInterval(() => {
    console.log("Auto-slide is running"); // 자동 슬라이드 작동 확인을 위한 로그
    if (currentSlide === totalSlides - 1) {
        moveSlide(0); // 마지막 슬라이드에서 첫 번째로 이동
    } else {
        moveSlide(currentSlide + 1); // 다음 슬라이드로 이동
    }
}, 5000);
