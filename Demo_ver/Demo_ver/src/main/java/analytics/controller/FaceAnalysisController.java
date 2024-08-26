package analytics.controller;

import com.example.demo.service.FaceAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class FaceAnalysisController {

    @Autowired
    private FaceAnalysisService faceAnalysisService;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyzeFace(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String result = faceAnalysisService.analyzeFace(file);
            model.addAttribute("result", result);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("result", "Error processing file.");
        }
        return "result";
    }
}
