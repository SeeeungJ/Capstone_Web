package PibuStory.Demo_ver.personalcolor.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PersonalColorController {

    @GetMapping("/personalcolortest")
    public String showPersonalColorTest() {
        return "personaltest"; // personaltest.html 페이지로 연결
    }


    @PostMapping("/test-color")
    public String testColor(@RequestParam("image") MultipartFile image, Model model, HttpServletResponse response) throws Exception {
        // Upload image to temporary file
        Path tempFile = Files.createTempFile("uploaded_", ".jpg");
        Files.write(tempFile, image.getBytes());

        List<String> resultImages = new ArrayList<>();

        String staticDir = "Demo_ver/Demo_ver/src/main/resources/static/output_images/";

        // Create output_images directory if it doesn't exist
        Path outputDirPath = Paths.get(staticDir);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }

        for (int colorKey = 1; colorKey <= 4; colorKey++) {
            String uniqueFileName = "image_" + colorKey + "_" + System.currentTimeMillis() + ".jpg";
            Path outputPath = Paths.get(staticDir + uniqueFileName);

            try {
                // Log before running the Python script
                System.out.println("Running Python script for colorKey: " + colorKey);
                System.out.println("Temporary file path: " + tempFile.toString());

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python3", "Demo_ver/Demo_ver/python_scripts/personal_color_test.py", tempFile.toString(), String.valueOf(colorKey), outputPath.toString()
                );
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                int exitCode = process.waitFor();
                System.out.println("Python script exited with code: " + exitCode);
                System.out.println("Python script output: " + result.toString());

                if (exitCode == 0) {
                    // Add image path to result list
                    String imagePath = "/output_images/" + uniqueFileName;
                    resultImages.add(imagePath);

                    // Log the image path
                    System.out.println("Generated image path: " + imagePath); // 콘솔에 경로 출력
                } else {
                    throw new RuntimeException("Error in Python script: " + result.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Python script execution failed for colorKey: " + colorKey, e);
            }
        }

        // Disable cache
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        model.addAttribute("resultImages", resultImages);

        // Log the result images list for debugging purposes
        System.out.println("All generated images: " + resultImages); // 모든 이미지 경로 출력

        return "result"; // Direct to 'result.html' page to show the results
    }

    public class FileUploadExceptionAdvice {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(MaxUploadSizeExceededException exc, Model model) {
            model.addAttribute("message", "파일 크기가 너무 큽니다. 최대 10MB까지 업로드할 수 있습니다.");
            return "error"; // error.html 템플릿에서 에러 메시지 표시
        }
    }

}
