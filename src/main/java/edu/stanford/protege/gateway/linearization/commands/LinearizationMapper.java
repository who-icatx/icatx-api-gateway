package edu.stanford.protege.gateway.linearization.commands;

import edu.stanford.protege.gateway.dto.EntityLinearization;
import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.dto.LinearizationTitle;
import org.semanticweb.owlapi.model.IRI;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class LinearizationMapper {


    public static EntityLinearizationWrapperDto mapFromResponse(WhoficEntityLinearizationSpecification whoficSpecification, Date latsRevisionDate) {
        List<EntityLinearization> linearizations = whoficSpecification.linearizationSpecifications()
                .stream().map(LinearizationMapper::mapFromSpecification)
                .sorted(Comparator.comparing(EntityLinearization::linearizationId)).toList();
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


    public static WhoficEntityLinearizationSpecification mapFromDto(String entityIri, EntityLinearizationWrapperDto dto) {
        LinearizationResiduals residuals = new LinearizationResiduals(LinearizationSpecificationStatus.getStatusFromString(dto.suppressOtherSpecifiedResiduals()),
                LinearizationSpecificationStatus.getStatusFromString(dto.suppressUnspecifiedResiduals()),
                dto.otherSpecifiedResidualTitle() != null ? dto.otherSpecifiedResidualTitle().label() : null,
                dto.unspecifiedResidualTitle() != null ? dto.unspecifiedResidualTitle().label() : null);

        List<LinearizationSpecification> specifications = dto.linearizations().stream().map(LinearizationMapper::mapFromDto).toList();

        return new WhoficEntityLinearizationSpecification(IRI.create(entityIri), residuals, specifications);
    }


    private static LinearizationSpecification mapFromDto(EntityLinearization dto) {
        return new LinearizationSpecification(LinearizationSpecificationStatus.getStatusFromString(dto.isAuxiliaryAxisChild()),
                LinearizationSpecificationStatus.getStatusFromString(dto.isGrouping()),
                LinearizationSpecificationStatus.getStatusFromString(dto.isIncludedInLinearization()),
                dto.linearizationPathParent() == null ? null : IRI.create(dto.linearizationPathParent()),
                IRI.create(dto.linearizationId()),
                dto.codingNote());
    }

    private static EntityLinearization mapFromSpecification(LinearizationSpecification specification) {
        return new EntityLinearization(specification.isAuxiliaryAxisChild().toString(),
                specification.isGrouping().toString(),
                specification.isIncludedInLinearization().toString(),
                specification.linearizationParent() == null ? "" : specification.linearizationParent().toString(),
                specification.linearizationView() == null ? "" : specification.linearizationView().toString(),
                specification.codingNote());
    }
}
