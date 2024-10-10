package PibuStory.skin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class SkinAnalysisController {

    // Python 스크립트 상대 경로
    private static final String SKIN_PYTHON_SCRIPT_PATH = "Demo_ver/Demo_ver/python-script/skin-analysis.py";

    // 결과 이미지 저장 디렉토리 (상대 경로)
    private static final String SKIN_OUTPUT_DIR = "Demo_ver/Demo_ver/output_images/";

    // Python 인터프리터 절대 경로
    private static final String PYTHON_INTERPRETER = "python";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 이미지 업로드 폼을 제공하는 GET 요청 매핑
     */
    @GetMapping("/skin-type")
    public String showUploadForm() {
        return "skin-type";
    }

    /**
     * 이미지 업로드 및 Python 스크립트 실행을 처리하는 POST 요청 매핑
     */
    @PostMapping("/run-skin_model")
    public String runSkinAnalysis(
            @RequestParam("image") MultipartFile image,
            Model model,
            HttpServletResponse response
    ) {
        try {
            // 1. 업로드된 이미지를 고유한 이름으로 저장
            String originalFilename = image.getOriginalFilename();
            String uniqueFilename = "skin_" + UUID.randomUUID().toString() + "_" + originalFilename;
            Path imagePath = Paths.get(SKIN_OUTPUT_DIR + uniqueFilename);
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, image.getBytes());

            // 2. 출력 이미지 경로 설정
            String outputFilename = "result_skin_" + uniqueFilename;
            Path outputPath = Paths.get(SKIN_OUTPUT_DIR + outputFilename);

            // 3. Python 스크립트 실행을 위한 ProcessBuilder 설정
            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_INTERPRETER,
                    new File(SKIN_PYTHON_SCRIPT_PATH).getAbsolutePath(),
                    imagePath.toString(),
                    outputPath.toString()
            );

            // 4. 작업 디렉토리를 설정하지 않음 (기본 경로 사용)
            processBuilder.directory(new File(System.getProperty("user.dir")));

            // 5. 에러 스트림과 출력 스트림을 합침
            processBuilder.redirectErrorStream(true);

            // 6. 스크립트 실행
            Process process = processBuilder.start();

            // 7. 스크립트 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder scriptOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptOutput.append(line).append("\n");
            }

            // 8. 스크립트 종료 대기
            int exitCode = process.waitFor();
            System.out.println("Python 스크립트 종료 코드: " + exitCode);
            System.out.println("Python 스크립트 출력: " + scriptOutput.toString());

            if (exitCode == 0) {
                // 9. 피부 타입 추출
                String skinType = extractSkinType(scriptOutput.toString());

                // 10. 추천 화장품 검색 및 모델에 추가
                List<Cosmetic> recommendations = recommendCosmetics(skinType);
                model.addAttribute("recommendations", recommendations);

                // 11. 결과 이미지 경로 (웹 접근 가능하도록)
                String resultImagePath = "/output_images/" + outputFilename;

                // 12. 모델에 결과 추가
                model.addAttribute("skinType", skinType);
                model.addAttribute("resultImage", resultImagePath);

                // 13. 캐시 비활성화
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);

                return "skin-result";
            } else {
                throw new RuntimeException("Python 스크립트 실행 중 오류가 발생했습니다: " + scriptOutput.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "피부 분석 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Python 스크립트의 출력에서 피부 타입을 추출하는 메서드
     *
     * @param scriptOutput Python 스크립트의 콘솔 출력
     * @return 추출된 피부 타입 문자열
     */
    private String extractSkinType(String scriptOutput) {
        String skinType = "UNKNOWN";
        String[] lines = scriptOutput.split("\n");
        for (String line : lines) {
            if (line.startsWith("Skin Type:")) {
                skinType = line.replace("Skin Type:", "").trim();
                break;
            }
        }
        return skinType;
    }

    /**
     * 피부 타입에 따라 추천 화장품을 검색하는 메서드
     *
     * @param skinType 피부 타입 문자열
     * @return 추천 화장품 리스트
     */
    private List<Cosmetic> recommendCosmetics(String skinType) {
        if ("dry".equalsIgnoreCase(skinType)) {
            List<String> goodIngredients = List.of(
                    "히아루론산", "글리세린", "프로필렌 글라이콜", "소디움PCA", "1,3-부틸렌 글라이콘", "비타민E", "비타민A",
                    "비타민C", "콜라겐", "엘라스틴", "아보카도 오일", "이브닝 프라임 로즈 오일", "오트밀 단백질", "콩 추출물",
                    "카모마일", "오이", "복숭아", "해조 추출물", "상백피 추출물", "코직산", "알부틴", "포토씨 추출물",
                    "베타카로틴", "시어버터", "파일워트 추출물", "비타민B 복합체", "판테놀"
            );

            // 좋은 성분 목록을 "|"로 연결하여 정규식 생성
            String regex = goodIngredients.stream()
                    .map(Pattern::quote)  // 특수문자를 이스케이프하여 정규식에서 안전하게 사용
                    .collect(Collectors.joining("|"));

            Criteria criteria = Criteria.where("ingredient").regex(regex, "i").and("dry").gt(0.5);

            // 결과 제한 (예: 5개 화장품)
            Query query = new Query(criteria);
            query.limit(5).with(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Order.desc("review_count"),
                    org.springframework.data.domain.Sort.Order.desc("rating_average")
            ));

            return mongoTemplate.find(query, Cosmetic.class, "cosmetic");
        }
        return List.of();
    }
}
