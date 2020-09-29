package com.example.uaa.config;

import com.example.uaa.security.AuthoritiesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    /**
     * Access tokens will not expire any earlier than this.
     */
    private static final int MIN_ACCESS_TOKEN_VALIDITY_SECS = 60;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationContext applicationContext;

    private final AppProperties properties;

    public AuthorizationServerConfiguration(PasswordEncoder passwordEncoder, ApplicationContext applicationContext, AppProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authorizationManager;

    /**
     * Apply the token converter (and enhancer) for token store.
     *
     * @return the {@link JwtTokenStore} managing the tokens.
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * This bean generates an token enhancer, which manages the exchange between JWT access tokens and Authentication
     * in both directions.
     *
     * @return an access token converter configured with the authorization server's public/private keys.
     */
    @Bean
    JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource(properties.getKeyStore().getName()),
                properties.getKeyStore().getPassword().toCharArray()
        ).getKeyPair(properties.getKeyStore().getAlias());
        tokenConverter.setKeyPair(keyPair);
        return tokenConverter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        int accessTokenValidity = properties.getWebClientConfiguration().getAccessTokenValidityInSeconds();
        accessTokenValidity = Math.max(accessTokenValidity, MIN_ACCESS_TOKEN_VALIDITY_SECS);
        int refreshTokenValidity = properties.getWebClientConfiguration().getRefreshTokenValidityInSecondsForRememberMe();
        refreshTokenValidity = Math.max(refreshTokenValidity, accessTokenValidity);

        clients.inMemory()
                .withClient(properties.getWebClientConfiguration().getClientId())
                .secret(passwordEncoder.encode(properties.getWebClientConfiguration().getSecret()))
                .authorizedGrantTypes("implicit", "refresh_token", "password", "authorization_code")
                .scopes("read")
                .autoApprove(true)
                .accessTokenValiditySeconds(accessTokenValidity)
                .refreshTokenValiditySeconds(refreshTokenValidity)
               .and()
                 //For the machine calls, the machine has to authenticate as a UAA using client credentials grant
                .withClient(properties.getSecurity().getClientAuthorization().getClientId())
                .secret(passwordEncoder.encode(properties.getSecurity().getClientAuthorization().getClientSecret()))
                .authorities(AuthoritiesConstants.ADMIN)
                .autoApprove(true)
                .authorizedGrantTypes("client_credentials")
                .scopes("web-app")
                .accessTokenValiditySeconds((int) properties.getSecurity().getClientAuthorization().getTokenValidityInSeconds())
                .refreshTokenValiditySeconds((int) properties.getSecurity().getClientAuthorization().getTokenValidityInSecondsForRememberMe());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //pick up all  TokenEnhancers incl. those defined in the application
        //this avoids changes to this class if an application wants to add its own to the chain
        Collection<TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class).values();
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(new ArrayList<>(tokenEnhancers));
        endpoints.authenticationManager(authorizationManager)
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .reuseRefreshTokens(false); //don't reuse or we will run into session inactivity timeouts
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()"); //secure the oauth/check_token endpoint
    }
}
