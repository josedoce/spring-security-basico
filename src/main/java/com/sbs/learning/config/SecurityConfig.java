package com.sbs.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.sbs.learning.config.filters.JwtInputFilter;
import com.sbs.learning.config.filters.RestAccessDeniedHandler;
import com.sbs.learning.config.filters.RestAuthenticationEntryPoint;
import com.sbs.learning.model.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwtInputFilter jwtInputFilter;
    private RestAccessDeniedHandler restAccessDeniedHandler;
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private UserRepository userRepository;

    public SecurityConfig(JwtInputFilter jif,
            RestAccessDeniedHandler radh,
            RestAuthenticationEntryPoint raep,
            UserRepository ur) {
        this.jwtInputFilter = jif;
        this.restAccessDeniedHandler = radh;
        this.restAuthenticationEntryPoint = raep;
        this.userRepository = ur;
    }

    // com esta declaração, podemos authenticar em qualquer parte da aplicação
    // utilizando o AuthenticationManager
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // aqui criamos os usuários de forma estatica, porem, dá para usar o banco de
    // daddos.
    /*
     * @Bean
     * 
     * @Override
     * public UserDetailsService userDetailsService() {
     * UserDetails usuario = User.withUsername("jose")
     * .password("{noop}12345678").roles("USUARIO").build();
     * 
     * UserDetails gerente = User.withUsername("maria")
     * .password("{noop}01234567").roles("GERENTE").build();
     * 
     * InMemoryUserDetailsManager userDetailsManager = new
     * InMemoryUserDetailsManager();
     * 
     * userDetailsManager.createUser(usuario);
     * userDetailsManager.createUser(gerente);
     * 
     * return userDetailsManager;
     * }
     */
    
     /*
     * Produz o mesmo resultado.
     * 
     * @Override
     * protected void configure(AuthenticationManagerBuilder auth) throws Exception
     * {
     * auth.inMemoryAuthentication()
     * .withUser("sergey")
     * .password("{noop}12345678")
     * .roles("USER")
     * .and()
     * .withUser("John")
     * .password("{noop}87654321")
     * .roles("MANAGER");
     * }
     */
    /*
     * Permitirá acesso por parte de outras aplicações e uso de mtodos não
     * suportados por padrão.
     */

    @Override
    @SuppressWarnings({ "deprecation" })
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService((username) -> {
            return this.userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException(String.format("User: %s, not found", username));
                });
        })
        // use Bcrypt, NoOpPasswordEncoder is not recommended
        .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http = http.cors() // aqui desabilitamos o cors e
            .and()
            .csrf().disable(); // aqui Cross-site request forgery
        http = http.authorizeRequests() // aqui pedimos para authorizar os seguintes requests

            .antMatchers(HttpMethod.POST, "/signin").permitAll()
            .antMatchers(HttpMethod.POST, "/signup/**").permitAll()
            // the order has impact in this case.
            .antMatchers("/users/admin").hasAuthority("ADMIN")
            .antMatchers("/users/manager").hasAuthority("MANAGER")
            .antMatchers("/users/user").hasAnyAuthority("USER")
            .antMatchers(HttpMethod.GET, "/users").permitAll()
            .antMatchers("/users/**").hasAnyAuthority("ADMIN", "MANAGER", "USER")
            .anyRequest().authenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(restAccessDeniedHandler)
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
            .addFilterBefore(jwtInputFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement((session) -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            });
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
