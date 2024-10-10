package PibuStory.faceshape;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class FaceShapeController {

    // ================================
    // **환경 설정: 절대 경로로 수정 필요**
    // ================================

    // Python 스크립트 절대 경로
    private static final String FACE_PYTHON_SCRIPT_PATH = "Demo_ver/Demo_ver/python-script/face-shape-classification.py"; // 실제 경로로 수정

    // 출력 이미지 저장 디렉토리 (외부 디렉토리)
    private static final String FACE_OUTPUT_DIR = "Demo_ver/Demo_ver/output_images/"; // 실제 경로로 수정

    // Python 인터프리터 절대 경로
    private static final String PYTHON_INTERPRETER = "python"; // 실제 설치 경로로 수정

    /**
     * 얼굴형 분석 페이지를 제공하는 GET 요청 매핑
     */
    @GetMapping("/face-shape")
    public String showUploadForm() {
        return "face-shape"; // face-shape.html 템플릿으로 이동
    }

    /**
     * 얼굴형 분석을 처리하는 POST 요청 매핑
     */
    @PostMapping("/run-face-model")
    public String runFaceShapeAnalysis(
            @RequestParam("image") MultipartFile image,
            Model model,
            HttpServletResponse response
    ) {
        try {
            // 1. 업로드된 이미지를 고유한 이름으로 저장
            String originalFilename = image.getOriginalFilename();
            String uniqueFilename = "face_" + UUID.randomUUID().toString() + "_" + originalFilename;
            Path imagePath = Paths.get(FACE_OUTPUT_DIR + uniqueFilename);
            Files.createDirectories(imagePath.getParent()); // 디렉토리 생성
            Files.write(imagePath, image.getBytes());

            // 2. Python 스크립트 실행을 위한 ProcessBuilder 설정
            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_INTERPRETER,  // Python 인터프리터의 전체 경로
                    FACE_PYTHON_SCRIPT_PATH,
                    imagePath.toString() // 저장된 이미지 경로 전달
            );

            // 3. 작업 디렉토리를 Python 스크립트가 있는 위치로 설정
            processBuilder.directory(new File("Demo_ver/Demo_ver/python-script/")); // 실제 경로로 수정

            // 4. 에러 스트림과 출력 스트림을 합침
            processBuilder.redirectErrorStream(true);

            // 5. 스크립트 실행
            Process process = processBuilder.start();

            // 6. 스크립트 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder scriptOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptOutput.append(line).append("\n");
            }

            // 7. 스크립트 종료 대기
            int exitCode = process.waitFor();
            System.out.println("Python 스크립트 종료 코드: " + exitCode);
            System.out.println("Python 스크립트 출력: " + scriptOutput.toString());

            if (exitCode == 0) {
                // 8. 얼굴형 추출
                String faceShape = extractFaceShape(scriptOutput.toString());

                // 9. 모델에 결과 추가
                model.addAttribute("faceShape", faceShape);

                // 10. 캐시 비활성화
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);

                return "face-result"; // 결과 페이지로 이동
            } else {
                throw new RuntimeException("Python 스크립트 실행 중 오류가 발생했습니다: " + scriptOutput.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "얼굴형 분석 중 오류가 발생했습니다: " + e.getMessage());
            return "error"; // error.html 템플릿으로 이동
        }
    }

    /**
     * Python 스크립트의 출력에서 얼굴형을 추출하는 메서드
     *
     * @param scriptOutput Python 스크립트의 콘솔 출력
     * @return 추출된 얼굴형 문자열
     */
    private String extractFaceShape(String scriptOutput) {
        String faceShape = "UNKNOWN";
        String[] lines = scriptOutput.split("\n");
        for (String line : lines) {
            if (line.startsWith("Face Shape:")) {
                faceShape = line.replace("Face Shape:", "").trim();
                break;
            }
        }
        return faceShape;
    }

    /**
     * 파일 업로드 크기 초과 시 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, Model model) {
        model.addAttribute("message", "파일 크기가 너무 큽니다. 최대 10MB까지 업로드할 수 있습니다.");
        return "error"; // error.html 템플릿에서 에러 메시지 표시
    }
}
