package com.example.wallet.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    protected void configure(HttpSecurity http) throws Exception{
        http.csrf().disable()
                .httpBasic()
                .and()
                .authorizeHttpRequests()
                .antMatchers(HttpMethod.POST, "/user/**").permitAll() //user signup
                .antMatchers("/user/**").hasAuthority(UserConstants.USER_AUTHORITY)          //user driven actions
                .antMatchers("/service/**").hasAnyAuthority(UserConstants.SERVICE_AUTHORITY, UserConstants.ADMIN_AUTHORITY)
                .antMatchers("/**").hasAuthority(UserConstants.ADMIN_AUTHORITY)               //admin driven actions
                .and()
                .formLogin();
    }
}
