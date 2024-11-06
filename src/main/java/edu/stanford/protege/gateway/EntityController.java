package edu.stanford.protege.gateway;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/entity")
public class EntityController {

    private final OwlEntityService owlEntityService;

    public EntityController(OwlEntityService owlEntityService) {
        this.owlEntityService = owlEntityService;
    }

    @GetMapping(value = "/{url}")
    public ResponseEntity<OWLEntityDto> getEntity(@PathVariable String url){
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        OWLEntityDto dto = owlEntityService.getEntityInfo(decodedUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLastModified(dto.lastChangeDate().toInstant());
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .eTag(Hashing.sha256().hashString(dto.lastChangeDate().toString(), StandardCharsets.UTF_8).toString())
                .body(dto);
    }
}
