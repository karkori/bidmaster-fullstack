package dev.mostapha.bidmaster.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("¡Hola desde BidMaster Backend!");
    }
}
