package org.example.demoweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SaleToCSKHController {
@Autowired
private SaleToCSKHService service;
  @GetMapping("/id/{id}")
  public String showMessage(@PathVariable("id") String id, Model model) throws Exception {
    System.out.println("Nhận được ID: " + id);
    model.addAttribute("id", id);
//    service.getDataFormSale();
    service.run(id);
    return "getID";
  }
}
