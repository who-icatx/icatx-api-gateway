package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

public record EntityLinearizationWrapperDto(String suppressOtherSpecifiedResiduals,
                                            String suppressUnspecifiedResiduals,

                                            @JsonIgnore
                                            Date lastRevisionDate,
                                            LinearizationTitle unspecifiedResidualTitle,
                                            LinearizationTitle otherSpecifiedResidualTitle,
                                            List<EntityLinearization> linearizations) {


}
