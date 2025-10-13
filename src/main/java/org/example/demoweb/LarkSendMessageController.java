package org.example.demoweb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class LarkSendMessageController {
    @Autowired
    private LarkSendMessageService larkSendMessageService;

    @RequestMapping("/")
    public String index() {
        return "index";  // Trả về giao diện index.html
    }

    @PostMapping("/run")
    public String runMessage(Model model) {
        System.out.println("runMessage");
        try {
            String result = larkSendMessageService.run();
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage());
        }
        return "index";
    }
}
