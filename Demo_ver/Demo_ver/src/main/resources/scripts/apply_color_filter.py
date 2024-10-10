import cv2
import numpy as np
import sys

def apply_color_filter(image_path, tone):
    # 이미지 로드
    image = cv2.imread(image_path)

    # 가우시안 블러를 사용해 노이즈를 줄입니다.
    image = cv2.GaussianBlur(image, (7, 7), 0)

    # HSV 색공간으로 변환
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # 입술 색상 감지를 위한 HSV 범위 설정
    lower_lip_color = np.array([0, 50, 50])
    upper_lip_color = np.array([10, 255, 255])

    # 색상 범위 내의 마스크를 생성
    mask = cv2.inRange(hsv, lower_lip_color, upper_lip_color)

    # 톤에 따라 색상 변경
    if tone == "spring":
        new_color = np.array([15, 150, 150])
    elif tone == "summer":
        new_color = np.array([20, 100, 100])
    elif tone == "fall":
        new_color = np.array([5, 200, 200])
    elif tone == "winter":
        new_color = np.array([0, 200, 200])

    # 마스크를 사용하여 입술 부분만 색상을 변경
    hsv[mask > 0] = new_color

    # BGR로 변환
    final_image = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

    # 새로운 이미지 저장 경로
    new_image_path = image_path.replace(".", f"_{tone}.")

    # 변경된 이미지 저장
    cv2.imwrite(new_image_path, final_image)

    return new_image_path

if __name__ == "__main__":
    image_path = sys.argv[1]
    tone = sys.argv[2]
    result_path = apply_color_filter(image_path, tone)
    print(result_path)
