package edu.stanford.protege.gateway;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WebProtegeIpcApplication.class)
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class IcatxApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcatxApiGatewayApplication.class, args);
	}

}
