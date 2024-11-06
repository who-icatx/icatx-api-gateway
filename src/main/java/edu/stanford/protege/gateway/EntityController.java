package edu.stanford.protege.gateway;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/entity")
public class EntityController {

    private final OwlEntityService owlEntityService;

    public EntityController(OwlEntityService owlEntityService) {
        this.owlEntityService = owlEntityService;
    }

    @GetMapping()
    public ResponseEntity<OWLEntityDto> getEntity(@RequestParam String entityIri){
//        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        OWLEntityDto dto = owlEntityService.getEntityInfo(entityIri);
        HttpHeaders httpHeaders = new HttpHeaders();
        String etag = "";
        if(dto.lastChangeDate() != null) {
            httpHeaders.setLastModified(dto.lastChangeDate().toInstant());
            etag = Hashing.sha256().hashString(dto.lastChangeDate().toString(), StandardCharsets.UTF_8).toString();
        }
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .eTag(etag)
                .body(dto);
    }
}
