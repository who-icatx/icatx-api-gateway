package edu.stanford.protege.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KeycloakRoleConverter.class);

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        var authorities = authoritiesConverter.convert(jwt);

        // Extract roles from resource_access.webprotege.roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null || !resourceAccess.containsKey("webprotege")) {
            return authorities;
        }
        Map<String, Object> webprotegeAccess = (Map<String, Object>) resourceAccess.get("webprotege");
        if (webprotegeAccess != null && webprotegeAccess.containsKey("roles")) {
            List<String> webprotegeRoles = (List<String>) webprotegeAccess.get("roles");
            authorities.addAll(webprotegeRoles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }
        return authorities;
    }
}