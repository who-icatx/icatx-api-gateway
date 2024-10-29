package edu.stanford.protege.gateway;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entity")
public class EntityController {


    @GetMapping()
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("YOU PASSED");
    }
}
