package org.example.demoweb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LarkSendMessageController {
    @Autowired
    private LarkSendMessageService larkSendMessageService;

    @RequestMapping("/")
    public String index() {
        return "index";  // Trả về giao diện index.html
    }

    @PostMapping("/run")
    public String runMessage(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("email") String email,
            Model model) {
        System.out.println("runMessage");
        try {
            String result = larkSendMessageService.run(startDate, endDate, email);
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage());
        }
        return "index";
    }
  @GetMapping("/id/{id}")
  public String showMessage(@PathVariable("id") String id, Model model) {
    System.out.println("Nhận được ID: " + id);
    model.addAttribute("id", id);

    return "getID";
  }
}
