package edu.stanford.protege.gateway.linearization.commands;

import edu.stanford.protege.gateway.dto.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class LinearizationMapper {


    public static EntityLinearizationWrapperDto mapFromResponse(WhoficEntityLinearizationSpecification whoficSpecification, Date latsRevisionDate, List<LinearizationDefinition> linDef) {
        List<EntityLinearization> linearizations = whoficSpecification.linearizationSpecifications()
                .stream()
                .map(specification -> LinearizationMapper.mapFromSpecification(specification, linDef))
                .sorted(Comparator.comparing(EntityLinearization::linearizationId))
                .toList();
        if (whoficSpecification.linearizationResiduals() != null) {
            String suppressOtherResiduals = whoficSpecification.linearizationResiduals().suppressOtherSpecifiedResiduals() == null ? null : whoficSpecification.linearizationResiduals().suppressOtherSpecifiedResiduals().toString();
            String suppressUnspecified = whoficSpecification.linearizationResiduals().suppressUnspecifiedResiduals() == null ? null : whoficSpecification.linearizationResiduals().suppressUnspecifiedResiduals().toString();
            return new EntityLinearizationWrapperDto(suppressOtherResiduals,
                    suppressUnspecified,
                    latsRevisionDate,
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().unspecifiedResidualTitle()),
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().otherSpecifiedResidualTitle()),
                    linearizations);
        }
        return new EntityLinearizationWrapperDto(null,
                null,
                latsRevisionDate,
                null,
                null,
                linearizations);
    }


    public static WhoficEntityLinearizationSpecification mapFromDto(String entityIri, EntityLinearizationWrapperDto dto, List<LinearizationDefinition> linDef) {
        LinearizationResiduals residuals = new LinearizationResiduals(LinearizationSpecificationStatus.getStatusFromString(dto.suppressOtherSpecifiedResiduals()),
                LinearizationSpecificationStatus.getStatusFromString(dto.suppressUnspecifiedResiduals()),
                dto.otherSpecifiedResidualTitle() != null ? dto.otherSpecifiedResidualTitle().label() : null,
                dto.unspecifiedResidualTitle() != null ? dto.unspecifiedResidualTitle().label() : null);

        List<LinearizationSpecification> specifications = dto.linearizations().stream().map(lin -> mapFromDto(lin, linDef)).toList();

        return new WhoficEntityLinearizationSpecification(IRI.create(entityIri), residuals, specifications);
    }


    private static LinearizationSpecification mapFromDto(EntityLinearization dto, List<LinearizationDefinition> linDef) {
        return new LinearizationSpecification(LinearizationSpecificationStatus.getStatusFromString(dto.isAuxiliaryAxisChild()),
                LinearizationSpecificationStatus.getStatusFromString(dto.isGrouping()),
                LinearizationSpecificationStatus.getStatusFromString(dto.isIncludedInLinearization()),
                dto.linearizationPathParent() == null ? null : IRI.create(dto.linearizationPathParent()),
                IRI.create(getLinView(dto, linDef)),
                dto.codingNote());
    }

    private static EntityLinearization mapFromSpecification(LinearizationSpecification specification, List<LinearizationDefinition> linDef) {

        return new EntityLinearization(specification.isAuxiliaryAxisChild().toString(),
                specification.isGrouping().toString(),
                specification.isIncludedInLinearization().toString(),
                specification.linearizationParent() == null ? "" : specification.linearizationParent().toString(),
                getLinId(specification, linDef),
                specification.codingNote());
    }

    private static String getLinId(LinearizationSpecification linSpec, List<LinearizationDefinition> linDef) {
        return linDef.stream()
                .filter(lindef -> {
                    if (linSpec.linearizationView() != null) {
                        return lindef.getLinearizationUri().equals(linSpec.linearizationView().toString());
                    }
                    return false;
                })
                .findAny()
                .map(LinearizationDefinition::getLinearizationId)
                .orElse("");
    }

    private static String getLinView(EntityLinearization lin, List<LinearizationDefinition> linDef) {
        return linDef.stream()
                .filter(lindef -> lin.linearizationId().equals(lindef.getLinearizationId()))
                .findAny()
                .map(LinearizationDefinition::getLinearizationUri)
                .orElse("");
    }
}
