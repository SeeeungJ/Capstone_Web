package PibuStory.personal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonalColorController {

    @GetMapping("/personal-color")
    public String showUploadForm() {
        return "personal-color"; // personal-color.html 템플릿으로 이동
    }

}
