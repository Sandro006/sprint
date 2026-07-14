package controller;
import framework.annotations.Controller;
import framework.annotations.GetMapping;

@Controller
public class C {
    @GetMapping("/a/test")
    public String goodString() {
        return "Hello from C COntrller";
    }
    @GetMapping("/a/test")
    public String goodString1() {
        return "Hello from C COntrller";
    }
}
