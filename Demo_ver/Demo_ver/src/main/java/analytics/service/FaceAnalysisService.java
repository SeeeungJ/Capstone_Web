package analytics.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FaceAnalysisService {

    public String analyzeFace(MultipartFile file) throws IOException {
        // 업로드된 파일을 로컬 디렉토리에 저장
        File tempFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + file.getOriginalFilename());
        FileUtils.writeByteArrayToFile(tempFile, file.getBytes());

        // Python 스크립트 실행
        ProcessBuilder processBuilder = new ProcessBuilder("python", "path/to/your_script.py", tempFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 파이썬 출력 결과 읽기
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 파일 삭제
        tempFile.delete();

        return output.toString();
    }
}
