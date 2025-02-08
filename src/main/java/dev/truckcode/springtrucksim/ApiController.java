package dev.truckcode.springtrucksim;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/message")
    public Map<String, String> getMessage() {
        return Map.of("message", "Hello from Spring Boot!");
    }
}
