import cv2
import dlib
import numpy as np
import sys
from collections import OrderedDict

# Dlib 얼굴 검출기와 랜드마크 예측기 로드
face_detector = dlib.get_frontal_face_detector()
landmark_predictor = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")  # 랜드마크 모델

# 퍼스널 컬러 색상 컬렉션 (BGR 순서)
lip_colors = OrderedDict({
    1: (121, 131, 248),  # 봄: 코랄 핑크 (BGR: 121, 131, 248)
    2: (40, 28, 78),  # 여름: 딥 로즈 (BGR: 40, 28, 78)
    3: (0, 69, 255),   # 가을: 오렌지 레드 (BGR: 0, 69, 255)
    4: (30, 17, 155)     # 겨울: 루비 레드 (BGR: 30, 17, 155)
})

# 입술 랜드마크 인덱스 (48~67)
lip_indices = list(range(48, 68))

# 파이썬 스크립트 인자로 받은 값 처리
image_path = sys.argv[1]
color_key = int(sys.argv[2])

# 이미지 읽기
image = cv2.imread(image_path)

# 얼굴 검출
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
faces = face_detector(gray)

for face in faces:
    # 랜드마크 추정
    landmarks = landmark_predictor(gray, face)

    # 입술 부분의 랜드마크 좌표 추출2
    lip_points = [(landmarks.part(i).x, landmarks.part(i).y) for i in lip_indices]

    # 입술에 색상 적용
    mask = np.zeros_like(image, dtype=np.uint8)
    cv2.fillPoly(mask, [np.array(lip_points, dtype=np.int32)], (255, 255, 255))

    # 마스크의 가장자리 부드럽게 처리
    blurred_mask = cv2.GaussianBlur(mask, (15, 15), 0)  # 커널 크기를 조절할 수 있습니다

    lip_color = lip_colors[color_key]

    # BGR에서 HSV로 변환
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    hsv_color = cv2.cvtColor(np.uint8([[lip_color]]), cv2.COLOR_BGR2HSV)[0][0]

    # 입술 색상 적용
    lip_hue = hsv_color[0]
    lip_saturation = hsv_color[1]

    # 기존 밝기(V 채널)를 유지하고, Hue와 Saturation만 변경
    original_value = hsv_image[:, :, 2].copy()  # 원래 밝기 값 복사

    hsv_image[:, :, 0] = np.where(blurred_mask[:, :, 0] == 255, lip_hue, hsv_image[:, :, 0])  # 색조 적용
    hsv_image[:, :, 1] = np.where(blurred_mask[:, :, 0] == 255, lip_saturation, hsv_image[:, :, 1])  # 채도 적용
    hsv_image[:, :, 2] = original_value  # 밝기 원상 복구

    # 다시 BGR로 변환
    colored_frame = cv2.cvtColor(hsv_image, cv2.COLOR_HSV2BGR)

    # 원본 이미지와 색상을 적용한 이미지를 부드럽게 혼합
    combined_frame = cv2.addWeighted(image, 0.8, colored_frame, 0.2, 0)  # 원본 비율을 0.8로 설정

    # 이미지 저장 경로 설정
    output_path = f"src/main/resources/static/output_images/image_{color_key}.jpg"
    cv2.imwrite(output_path, combined_frame)

# 경로 출력 (Java에서 처리할 수 있게)
print(f"Image saved at {output_path}")