package edu.stanford.protege.gateway.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.gateway.linearization.commands.GetEntityLinearizationsRequest;
import edu.stanford.protege.gateway.linearization.commands.GetEntityLinearizationsResponse;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinitionRequest;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinitionResponse;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValueResponse;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValuesRequest;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationRequest;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationResponse;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.IriDeserializer;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@Configuration
public class ApplicationBeans  implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> connector.setEncodedSolidusHandling(
                EncodedSolidusHandling.DECODE.getValue()));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }

    @Bean
    CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> executorForEntityLinearization() {
        return new CommandExecutorImpl<>(GetEntityLinearizationsResponse.class);
    }


    @Bean
    CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> executorForCustomScales(){
        return new CommandExecutorImpl<>(GetEntityCustomScaleValueResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> executorForPostCoordination(){
        return new CommandExecutorImpl<>(GetEntityPostCoordinationResponse.class);
    }

    @Bean
    CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> executorForLinearizationDefinition(){
        return new CommandExecutorImpl<>(LinearizationDefinitionResponse.class);
    }

    @Bean
    CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> executorForClassAncestors(){
        return new CommandExecutorImpl<>(GetClassAncestorsResponse.class);
    }

    @Bean
    CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> executorForEntityForm() {
        return new CommandExecutorImpl<>(GetEntityFormAsJsonResponse.class);
    };


    @Bean
    CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> executorForLogicalDefinition(){
        return new CommandExecutorImpl<>(GetLogicalDefinitionsResponse.class);
    }

}
