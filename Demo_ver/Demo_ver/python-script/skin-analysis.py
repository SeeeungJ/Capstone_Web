import tensorflow as tf
import cv2
import numpy as np
from PIL import Image
from matplotlib import pyplot as plt
from mtcnn.mtcnn import MTCNN
import dlib
import warnings
import sys
import os

warnings.filterwarnings('ignore')

# 현재 스크립트의 디렉토리 경로를 기준으로 모델 파일 경로 설정
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, 'skin_final_model.h5')
predictor_path = os.path.join(script_dir, 'shape_predictor_68_face_landmarks.dat')

# 모델 로드
m = tf.keras.models.load_model(model_path)

# Dlib 얼굴 검출기와 랜드마크 예측기 로드
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(predictor_path)  # .dat 파일 필요

# 얼굴 이미지에서 얼굴을 감지하고 시각화하는 함수
def draw_faces(filename, result_list, output_path='out1.jpg'):
    data = plt.imread(filename)
    if len(result_list) == 0:
        print("얼굴을 감지할 수 없습니다.")
        sys.exit(1)
    # 첫 번째 얼굴만 처리
    x1, y1, width, height = result_list[0]['box']
    x2, y2 = x1 + width, y1 + height
    face_img = data[y1:y2, x1:x2]
    plt.imshow(face_img)
    plt.axis('off')
    plt.savefig(output_path)
    plt.close()

def detect_and_draw_face(filename, output_path='out1.jpg'):
    pixels = plt.imread(filename)
    detector_mtcnn = MTCNN()
    faces = detector_mtcnn.detect_faces(pixels)
    draw_faces(filename, faces, output_path)

# 얼굴 이미지에서 특정 부위를 추출하는 함수
def get_facial_features(filename):
    img = cv2.imread(filename)
    if img is None:
        print(f"이미지를 불러올 수 없습니다: {filename}")
        sys.exit(1)
    height, width = img.shape[:2]

    forehead = img[int(height * 0.05):int(height * 0.3), int(width * 0.25):int(width * 0.75)]
    forehead_path = os.path.join(os.path.dirname(filename), 'forehead.jpg')
    cv2.imwrite(forehead_path, forehead)

    cheek1 = img[int(height * 0.5):int(height * 0.7), int(width * 0.05):int(width * 0.425)]
    cheek1_path = os.path.join(os.path.dirname(filename), 'cheek1.jpg')
    cv2.imwrite(cheek1_path, cheek1)

    nose = img[int(height * 0.4):int(height * 0.65), int(width * 0.475):int(width * 0.575)]
    nose_path = os.path.join(os.path.dirname(filename), 'nose.jpg')
    cv2.imwrite(nose_path, nose)

    cheek2 = img[int(height * 0.5):int(height * 0.7), int(width * 0.6):int(width * 0.95)]
    cheek2_path = os.path.join(os.path.dirname(filename), 'cheek2.jpg')
    cv2.imwrite(cheek2_path, cheek2)

    return ['forehead.jpg', 'nose.jpg', 'cheek1.jpg', 'cheek2.jpg']

# 각 얼굴 부위의 피부 타입을 예측하는 함수
def predict_for_each_facial_feature(features):
    oily_count = 0
    dry_count = 0
    skin_types = {}

    for feature in features:
        img_path = feature
        try:
            img = Image.open(img_path).resize((100, 100))
        except IOError:
            print(f"이미지를 열 수 없습니다: {img_path}")
            continue
        x = np.array(img)
        x = np.expand_dims(x, axis=0)
        val = np.round(m.predict(x))[0][0]

        if val == 0:
            skin_types[os.path.basename(feature).split('.')[0]] = "DRY"
            dry_count += 1
        elif val == 1:
            skin_types[os.path.basename(feature).split('.')[0]] = "OILY"
            oily_count += 1

    if (skin_types.get('forehead') == "OILY" and
            skin_types.get('nose') == "OILY" and
            skin_types.get('cheek1') == "DRY" and
            skin_types.get('cheek2') == "DRY"):
        skin_type = "COMBINATION"
    elif oily_count >= 3:
        skin_type = "OILY"
    elif dry_count >= 3:
        skin_type = "DRY"
    elif oily_count > dry_count:
        skin_type = "OILY"
    else:
        skin_type = "DRY"

    return skin_type, skin_types

# 둥근 모서리 사각형을 그리는 함수
def draw_rounded_rectangle(img, x, y, w, h, radius, color, thickness=-1):
    cv2.ellipse(img, (x + radius, y + radius), (radius, radius), 180, 0, 90, color, thickness)
    cv2.ellipse(img, (x + w - radius, y + radius), (radius, radius), 270, 0, 90, color, thickness)
    cv2.ellipse(img, (x + w - radius, y + h - radius), (radius, radius), 0, 0, 90, color, thickness)
    cv2.ellipse(img, (x + radius, y + h - radius), (radius, radius), 90, 0, 90, color, thickness)

    cv2.rectangle(img, (x + radius, y), (x + w - radius, y + h), color, thickness)
    cv2.rectangle(img, (x, y + radius), (x + w, y + h - radius), color, thickness)

# 얼굴 이미지에 특정 영역 결과 값을 추가하는 함수
def apply_facial_zones_on_image_with_dlib(img, skin_types):
    overlay = img.copy()
    zones = get_facial_zones(img)

    for zone_name, (x, y, w, h) in zones.items():
        if zone_name in skin_types:
            color = (0, 255, 0) if skin_types[zone_name] == "OILY" else (0, 165, 255)
            draw_rounded_rectangle(overlay, x, y, w, h, radius=20, color=color, thickness=-1)
            cv2.putText(overlay, skin_types[zone_name], (x + 10, y + 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)

    alpha = 0.5  # 투명도 계수
    cv2.addWeighted(overlay, alpha, img, 1 - alpha, 0, img)

    # BGR 형식을 유지한 채로 반환
    return img


# dlib의 얼굴 검출기와 얼굴 랜드마크 예측기 로드
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(predictor_path)  # .dat 파일 필요

def get_facial_zones(img):
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = detector(gray)

    if len(faces) == 0:
        print("Dlib으로 얼굴을 감지할 수 없습니다.")
        sys.exit(1)

    face = faces[0]
    landmarks = predictor(gray, face)

    forehead = [landmarks.part(n).x for n in range(18, 26)]
    forehead_top = min([landmarks.part(n).y for n in range(19, 27)]) - 50  # 눈썹 위에 여유 공간 추가
    forehead_coords = (min(forehead), forehead_top, max(forehead) - min(forehead), 50)

    nose_top = landmarks.part(27).y
    nose_bottom = landmarks.part(33).y
    nose_left = landmarks.part(31).x
    nose_right = landmarks.part(35).x
    nose_coords = (nose_left, nose_top, nose_right - nose_left, nose_bottom - nose_top)

    left_cheek_coords = (landmarks.part(1).x + 20, landmarks.part(36).y + 30, 70, 70)
    right_cheek_coords = (landmarks.part(28).x + 50, landmarks.part(15).y, 70, 70)

    return {
        "forehead": forehead_coords,
        "nose": nose_coords,
        "cheek1": left_cheek_coords,
        "cheek2": right_cheek_coords
    }

def final_func(image_path, output_path):
    print(f"Running final_func with image_path={image_path} and output_path={output_path}")

    # 얼굴 감지 및 저장
    detect_and_draw_face(image_path, output_path='out1.jpg')
    print("Face detected and saved to out1.jpg")

    # 얼굴 특징 추출
    features = get_facial_features('out1.jpg')
    print(f"Extracted features: {features}")

    # 피부 타입 예측
    skin_type, skin_types = predict_for_each_facial_feature(features)
    print(f"Skin Type: {skin_type}")

    # 결과 이미지 불러오기
    img = cv2.imread('out1.jpg')
    if img is None:
        print("출력 이미지를 불러올 수 없습니다.")
        sys.exit(1)
    print("Output image loaded successfully.")

    # 피부 타입 표시
    annotated_img = apply_facial_zones_on_image_with_dlib(img, skin_types)
    print("Annotated image created.")

    # 결과 이미지 저장
    cv2.imwrite(output_path, annotated_img)
    print(f"Annotated image saved to {output_path}")

    # 피부 타입 출력
    print(f"Skin Type: {skin_type}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("사용법: python skin-analysis.py <이미지경로> <출력경로>")
        sys.exit(1)

    image_path = sys.argv[1]
    output_path = sys.argv[2]

    final_func(image_path, output_path)
