package com.github.squi2rel.ssmmo;

import com.github.squi2rel.ssmmo.core.eval.CodeEval;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableAsync
public class Application {
	public CodeEval eval = new CodeEval();

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping("/run")
	public String hello(@RequestParam(value = "code", defaultValue = "1+1") String code) {
        try {
            return eval.runCode(code).get();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
