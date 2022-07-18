package com.example.demo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Autowired
  private DataSource dataSource;
  
  // ユーザIDとパスワードを取得するSQL文
	private static final String USER_SQL = "SELECT" + " user_id," + " password," + " true" + " FROM" + " user"
			+ " WHERE" + " user_id = ?";

	// ユーザのロールを取得するSQL文
	private static final String ROLE_SQL = "SELECT" + " user_id," + " role" + " FROM" + " user" + " WHERE"
			+ " user_id = ?";

  @Override
  public void configure(WebSecurity web) throws Exception {

    // 静的リソースへのアクセスには、セキュリティを適用しない
    web.ignoring().antMatchers("/webjars/**", "/css/**", "/images/**", "/js/**");

  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // ログイン不要ページの設定
    http.authorizeRequests()
        .antMatchers("/webjars/**").permitAll()
        .antMatchers("/css/**").permitAll()
        .antMatchers("/images/**").permitAll()
        .antMatchers("/js/**").permitAll()
        .antMatchers("/login").permitAll()
        .antMatchers("/signup").permitAll()
        .antMatchers("/admin").hasAuthority("ROLE_ADMIN")
        .anyRequest().authenticated();

    // ログイン処理
    http.formLogin()
        .loginProcessingUrl("/login")
        .loginPage("/login")
        .failureUrl("/login")
        .usernameParameter("userId")
        .passwordParameter("password")
        .defaultSuccessUrl("/home", true);
    
    // ログアウト処理
    http.logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login");

    // RESTのみCSRF対策を無効にする場合
    // http.csrf().disable();
  }
  
  @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		// ログイン処理時のユーザ情報を、DBから取得する
    auth.jdbcAuthentication()
        .dataSource(dataSource)
        .usersByUsernameQuery(USER_SQL)
        .authoritiesByUsernameQuery(ROLE_SQL)
        .passwordEncoder(passwordEncoder());
	}
  
}
