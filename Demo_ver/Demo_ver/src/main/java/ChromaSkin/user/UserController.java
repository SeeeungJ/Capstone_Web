package ChromaSkin.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.security.SecureRandom;

@Controller
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 페이지 렌더링
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // login.html 템플릿 반환
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userService.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 로그인 성공 시 알림 메시지
            MessageDto message = new MessageDto("로그인 성공!", "/home", RequestMethod.GET, null);
            return showMessageAndRedirect(message, model);
        } else {
            // 로그인 실패 시 알림 메시지
            MessageDto message = new MessageDto("로그인 실패: 아이디 또는 비밀번호가 일치하지 않습니다.", "/login", RequestMethod.GET, null);
            return showMessageAndRedirect(message, model);
        }
    }

    // 회원가입 페이지 렌더링
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html 템플릿 반환
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";  // home.html 반환
    }

    // 아이디 중복 체크 API
    @GetMapping("/api/users/check-username")
    @ResponseBody
    public ResponseEntity<Boolean> checkUsername(@RequestParam("username") String username) {
        try {
            boolean isAvailable = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    // 닉네임 중복 체크 API
    @GetMapping("/api/users/check-nickname")
    @ResponseBody
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nickname") String nickname) {
        try {
            boolean isAvailable = userService.isNicknameAvailable(nickname);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    // 이메일 중복 체크 API
    @GetMapping("/api/users/check-email")
    @ResponseBody
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        try {
            boolean isAvailable = userService.isEmailAvailable(email);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


    // 회원가입 처리
    @PostMapping("/api/users/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        // 아이디, 닉네임, 이메일 중복 체크
        if (!userService.isUsernameAvailable(user.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 존재하는 아이디입니다.");
            return "redirect:/register";
        }
        if (!userService.isNicknameAvailable(user.getNickname())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 존재하는 닉네임입니다.");
            return "redirect:/register";
        }
        if (!userService.isEmailAvailable(user.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 존재하는 이메일입니다.");
            return "redirect:/register";
        }

        // 비밀번호 암호화 후 사용자 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);

        // 회원가입 성공 시 로그인 페이지로 리디렉트 및 성공 메시지 전달
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다!");
        return "redirect:/login";
    }

    // 아이디 찾기 페이지 렌더링
    @GetMapping("/find-id")
    public String showFindIdPage() {
        return "find-id"; // find-id.html 템플릿 반환
    }

    // 아이디 찾기 처리
    @PostMapping("/find-id")
    public String findIdByEmail(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("errorMessage", "해당 이메일로 등록된 아이디가 없습니다.");
            return "find-id"; // 다시 아이디 찾기 페이지로 리다이렉트
        }

        model.addAttribute("foundUsername", user.getUsername());
        return "found-id"; // 아이디를 찾은 후 결과 페이지로 이동
    }

    // 비밀번호 찾기 페이지 렌더링
    @GetMapping("/find-password")
    public String showFindPasswordPage() {
        return "find-password"; // find-password.html 템플릿 반환
    }

    // 비밀번호 찾기 처리
    @PostMapping("/find-password")
    public String resetPassword(@RequestParam("username") String username, @RequestParam("email") String email, Model model) {
        User user = userService.findByUsernameAndEmail(username, email);
        if (user == null) {
            model.addAttribute("errorMessage", "아이디와 이메일이 일치하지 않습니다.");
            return "find-password"; // 다시 비밀번호 찾기 페이지로 리다이렉트
        }

        // 새로운 임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword)); // 비밀번호 암호화 후 저장
        userService.saveUser(user); // 사용자 정보 저장

        model.addAttribute("tempPassword", tempPassword); // 임시 비밀번호를 모델에 추가
        return "reset-password"; // 비밀번호 재설정 완료 페이지로 이동
    }

    // 마이페이지 렌더링
    @GetMapping("/my-page")
    public String showMyPage(Model model, Principal principal) {
        // 로그인된 사용자 정보를 principal로 가져옴
        String username = principal.getName();
        User user = userService.findByUsername(username);

        // user 객체가 null이면 오류 발생, 예외 처리 필요
        if (user == null) {
            model.addAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/login"; // 사용자 정보가 없을 경우 로그인 페이지로 리다이렉트
        }

        // user 객체를 모델에 추가하여 템플릿에서 사용할 수 있도록 함
        model.addAttribute("user", user);

        return "my-page"; // my-page.html 템플릿 반환
    }

    // 비밀번호 재설정 처리 (마이페이지에서)
    @PostMapping("/my-page/reset-password")
    public String resetPasswordInMyPage(@RequestParam("newPassword") String newPassword,
                                        @RequestParam("oldPassword") String oldPassword,
                                        Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            MessageDto message = new MessageDto("비밀번호 재설정 실패: 현재 비밀번호가 일치하지 않습니다.", "/my-page", RequestMethod.GET, null);
            return showMessageAndRedirect(message, model);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);

        // 비밀번호 변경 성공 시 알림 메시지
        MessageDto message = new MessageDto("비밀번호가 성공적으로 변경되었습니다. 다시 로그인 해주세요.", "/login", RequestMethod.GET, null);
        return showMessageAndRedirect(message, model);
    }

    // 사용자에게 알림 메시지를 띄우고 리다이렉트하는 메서드
    private String showMessageAndRedirect(final MessageDto params, Model model) {
        model.addAttribute("params", params);
        return "common/messageRedirect"; // messageRedirect.html을 반환
    }

    // 임시 비밀번호 생성 메서드
    private String generateTemporaryPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialCharacters = "!@#$%^&*()-_+=<>?";

        String combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;
        SecureRandom random = new SecureRandom();
        int passwordLength = 10;

        StringBuilder password = new StringBuilder(passwordLength);

        // 최소 한 글자씩 추가
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // 나머지 비밀번호 글자 추가
        for (int i = 4; i < passwordLength; i++) {
            password.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        return password.toString();
    }
}
