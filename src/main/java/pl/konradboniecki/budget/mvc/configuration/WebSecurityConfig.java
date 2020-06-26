package pl.konradboniecki.budget.mvc.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.konradboniecki.budget.mvc.service.SpringAuthenticationProvider;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Scope(scopeName = SCOPE_SINGLETON)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SpringAuthenticationProvider authProvider;

    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider).eraseCredentials(false);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/",
                        "/login",
                        "/logout",
                        "/register",
                        "/budget/family/*/addMember/**",
                        "/resources/**",
                        "/actuator/health",
                        "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage(BASE_URL + "/login")
                .loginProcessingUrl("/authenticate")
                .successForwardUrl("/")
                .permitAll()
                .usernameParameter("email").passwordParameter("password")
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl(BASE_URL + "/login?logout")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/403")
                .and()
                .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
