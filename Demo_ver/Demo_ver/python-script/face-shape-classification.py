import tensorflow as tf
# from tensorflow.keras.layers import SeparableConv2D
# from tensorflow.keras.models import load_model
import numpy as np
import sys
import cv2

# 얼굴형 분류 클래스 (5가지 얼굴형)
FACE_SHAPE_CLASSES = ['Egg', 'Heart', 'Long', 'Round', 'Square']

# 모델 로드
model_path = 'face_shape_final_model_1002.h5'  # 실제 경로로 수정

m = tf.keras.models.load_model(model_path)
# model = load_model(model_path)

# 이미지 전처리 함수
def preprocess_image(image_path):
    # 이미지 로드
    img = cv2.imread(image_path)

    # 이미지가 없으면 에러 처리
    if img is None:
        raise ValueError("이미지를 로드할 수 없습니다.")

    # 얼굴형 분류 모델에 맞게 이미지 크기 조정 (224x224)
    img_resized = cv2.resize(img, (224, 224))

    # 이미지를 float32로 변환 후, 255로 나누어 [0, 1] 범위로 정규화
    img_normalized = img_resized.astype(np.float32) / 255.0

    # 차원 확장 (모델은 4차원 배열 [batch, height, width, channels]로 입력받음)
    img_expanded = np.expand_dims(img_normalized, axis=0)

    return img_expanded

# 얼굴형 분류 함수
def classify_face_shape(image_path):
    # 이미지 전처리
    preprocessed_image = preprocess_image(image_path)

    # 모델 예측
    predictions = m.predict(preprocessed_image)

    # 가장 높은 확률을 가진 클래스 선택
    predicted_class = np.argmax(predictions[0])

    # 결과 리턴 (얼굴형 클래스)
    return FACE_SHAPE_CLASSES[predicted_class]

# 메인 실행 함수
if __name__ == "__main__":
    # 커맨드라인에서 입력된 이미지 경로
    image_path = sys.argv[1]

    try:
        # 얼굴형 분류
        face_shape = classify_face_shape(image_path)

        # 결과 출력 (Spring Boot에서 이 값을 사용)
        print(f"Face Shape: {face_shape}")

    except Exception as e:
        print(f"Error: {str(e)}")
