package PibuStory.Demo_ver.personalcolor.controller;

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

        String staticDir = "src/main/resources/static/output_images/";

        // Create output_images directory if it doesn't exist
        Path outputDirPath = Paths.get(staticDir);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }

        for (int colorKey = 1; colorKey <= 4; colorKey++) {
            String uniqueFileName = "image_" + colorKey + ".jpg";
            Path outputPath = Paths.get(staticDir + uniqueFileName);

            try {
                // Log before running the Python script
                System.out.println("Running Python script for colorKey: " + colorKey);
                System.out.println("Temporary file path: " + tempFile.toString());

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python3", "python_scripts/personal_color_test.py", tempFile.toString(), String.valueOf(colorKey), outputPath.toString()
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
                    System.out.println("Python script completed successfully for colorKey: " + colorKey);
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

        return "result"; // Direct to 'result.html' page to show the results
    }

    @PostMapping("/submit-answers")
    public String submitAnswers(
            @RequestParam("q1") String q1,
            @RequestParam("q2") String q2,
            @RequestParam("q3") String q3,
            @RequestParam("q4") String q4,
            @RequestParam("q5") String q5,
            Model model) {

        // Count A, B, C, D selections
        int aCount = 0, bCount = 0, cCount = 0, dCount = 0;

        // Array of answers
        String[] answers = {q1, q2, q3, q4, q5};
        for (String answer : answers) {
            switch (answer) {
                case "A": aCount++; break;
                case "B": bCount++; break;
                case "C": cCount++; break;
                case "D": dCount++; break;
            }
        }

        // Determine final result based on counts
        String finalResult;
        if (aCount >= bCount && aCount >= cCount && aCount >= dCount) {
            finalResult = "Spring (봄)";
        } else if (bCount >= aCount && bCount >= cCount && bCount >= dCount) {
            finalResult = "Summer (여름)";
        } else if (cCount >= aCount && cCount >= bCount && cCount >= dCount) {
            finalResult = "Autumn (가을)";
        } else {
            finalResult = "Winter (겨울)";
        }

        // Add result to the model
        model.addAttribute("finalResult", finalResult);

        return "result-confirm";  // Direct to result-confirm page to display the result
    }

    public class FileUploadExceptionAdvice {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(MaxUploadSizeExceededException exc, Model model) {
            model.addAttribute("message", "파일 크기가 너무 큽니다. 최대 10MB까지 업로드할 수 있습니다.");
            return "error"; // error.html 템플릿에서 에러 메시지 표시
        }
    }

}
