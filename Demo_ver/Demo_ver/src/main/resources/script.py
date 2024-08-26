import pickle
import sys
from some_image_processing_library import process_image  # 이미지 처리 라이브러리 임포트 필요

# 모델 로드
with open('face_type_model2.pkl', 'rb') as f:
    model = pickle.load(f)

# 입력 이미지 파일 경로
image_path = sys.argv[1]

# 이미지 처리 및 예측
image_data = process_image(image_path)
result = model.predict(image_data)

# 결과 출력
print(f'피부 유형: {result}')
