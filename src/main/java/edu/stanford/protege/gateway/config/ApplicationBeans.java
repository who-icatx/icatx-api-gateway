package edu.stanford.protege.gateway.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.gateway.history.commands.*;
import edu.stanford.protege.gateway.linearization.commands.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.postcoordination.commands.*;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.IriDeserializer;
import edu.stanford.protege.webprotege.jackson.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@Configuration
public class ApplicationBeans implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addDeserializer(LinearizationSpecificationStatus.class, new LinearizationSpecificationStatusDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> executorForEntityLinearization() {
        return new CommandExecutorImpl<>(GetEntityLinearizationsResponse.class);
    }


    @Bean
    CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> executorForCustomScales() {
        return new CommandExecutorImpl<>(GetEntityCustomScaleValueResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> executorForPostCoordination() {
        return new CommandExecutorImpl<>(GetEntityPostCoordinationResponse.class);
    }

    @Bean
    CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> executorForLinearizationDefinition() {
        return new CommandExecutorImpl<>(LinearizationDefinitionResponse.class);
    }

    @Bean
    CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> executorForClassAncestors() {
        return new CommandExecutorImpl<>(GetClassAncestorsResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> executorForEntityForm() {
        return new CommandExecutorImpl<>(GetEntityFormAsJsonResponse.class);
    }

    @Bean
    CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> executorForLogicalDefinition() {
        return new CommandExecutorImpl<>(GetLogicalDefinitionsResponse.class);
    }

    @Bean
    CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor() {
        return new CommandExecutorImpl<>(GetChangedEntitiesResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor() {
        return new CommandExecutorImpl<>(GetEntityChildrenResponse.class);
    }

    @Bean
    CommandExecutor<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> executorForSaveEntityLinearization(){
        return new CommandExecutorImpl<>(SaveEntityLinearizationResponse.class);
    }

    @Bean
    CommandExecutor<AddEntityCustomScalesRevisionRequest, AddEntityCustomScalesRevisionResponse> executorForSaveEntityCustomScales(){
        return new CommandExecutorImpl<>(AddEntityCustomScalesRevisionResponse.class);
    }

    @Bean
    CommandExecutor<AddEntitySpecificationRevisionRequest, AddEntitySpecificationRevisionResponse> executorForSaveEntityPostCoordinationSpec(){
        return new CommandExecutorImpl<>(AddEntitySpecificationRevisionResponse.class);
    }
    @Bean
    CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> executorForGetPostCoordinationTableConfiguration(){
        return new CommandExecutorImpl<>(GetTablePostCoordinationAxisResponse.class);
    }

    @Bean
    CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> executorForGetIcatxEntityType() {
        return new CommandExecutorImpl<>(GetIcatxEntityTypeResponse.class);
    }

    @Bean
    CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> executorForUpdateLogicalDefinition(){
        return new CommandExecutorImpl<>(UpdateLogicalDefinitionsResponse.class);
    }

    @Bean
    CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> executorForChangeEntityParents(){
        return new CommandExecutorImpl<>(ChangeEntityParentsResponse.class);
    }

    @Bean
    CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> executorForUpdateLanguageTerms(){
        return new CommandExecutorImpl<>(SetEntityFormDataFromJsonResponse.class);
    }
    @Bean
    CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> getIsExistingProjectExecutor() {
        return new CommandExecutorImpl<>(GetIsExistingProjectResponse.class);
    }

    @Bean
    CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor() {
        return new CommandExecutorImpl<>(FilterExistingEntitiesResponse.class);
    }

    @Bean
    CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassesExecutor() {
        return new CommandExecutorImpl<>(CreateClassesFromApiResponse.class);
    }

    @Bean
    CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getAvailableProjectsExecutor() {
        return new CommandExecutorImpl<>(GetAvailableProjectsForApiResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor() {
        return new CommandExecutorImpl<>(GetEntityCommentsResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> getEntityHistorySummaryExecutor() {
        return new CommandExecutorImpl<>(GetEntityHistorySummaryResponse.class);
    }

}
