// 파일 선택 이벤트 감지
const inputFile = document.getElementById('input-file');
const fileInfo = document.getElementById('file-info');
const fileName = document.getElementById('file-name');
const checkBtn = document.getElementById('check-btn');

inputFile.addEventListener('change', (event) => {
    const file = event.target.files[0];
    if (file) {
        fileName.textContent = `선택한 파일: ${file.name}`;
        fileInfo.style.display = 'block'; // 파일 정보 표시
        checkBtn.style.display = 'inline-block'; // 검사하기 버튼 표시
    } else {
        fileInfo.style.display = 'none';
        checkBtn.style.display = 'none'; // 검사하기 버튼 숨기기
    }
});
