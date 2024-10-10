package PibuStory.Demo_ver.makeup;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class MakeupController {

    private static final Logger logger = Logger.getLogger(MakeupController.class.getName());

    @GetMapping("/makeup")
    public ModelAndView showPage() {
        return new ModelAndView("makeup"); // makeup.html을 반환
    }
}
