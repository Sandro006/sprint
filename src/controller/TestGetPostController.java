package controller;

import framework.annotations.Controller;
import framework.annotations.GetMapping;
import framework.annotations.PostMapping;

@Controller
public class TestGetPostController {

    @GetMapping("/test/get")
    public String getTest() {
        return "OK: GET /test/get";
    }

    @PostMapping("/test/get")
    public String postTest() {
        return "OK: POST /test/get";
    }

    @GetMapping("/test/get")
    public String onlyGet() {
        return "OK: GET /test/only-get";
    }
}

