package controller;

import framework.annotations.Controller;
import framework.annotations.GetMapping;

@Controller
public class A {
    
    @GetMapping("/a/test")
    public String test() {
        return "Hello from A";
    }
    public String testWithParam() {
        return "Hello from A";
    }
}

