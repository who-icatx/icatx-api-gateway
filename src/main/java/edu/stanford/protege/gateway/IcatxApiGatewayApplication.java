package edu.stanford.protege.gateway;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import edu.stanford.protege.webprotege.ipc.impl.RabbitMqConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
@Import({WebProtegeIpcApplication.class})
public class IcatxApiGatewayApplication {

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(IcatxApiGatewayApplication.class, args);
	}



}
