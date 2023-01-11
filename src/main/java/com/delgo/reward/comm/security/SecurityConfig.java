package com.delgo.reward.comm.security;


import com.delgo.reward.comm.security.jwt.filter.JwtAuthenticationFilter;
import com.delgo.reward.comm.security.jwt.filter.JwtAuthorizationFilter;
import com.delgo.reward.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity // 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	private final UserRepository userRepository;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization_Access, Authorization_Refresh");
//		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.formLogin().disable()
				.httpBasic().disable()
				.cors().configurationSource(corsConfigurationSource())
				.and()

				.addFilter(new JwtAuthenticationFilter(authenticationManager()))
				.addFilter(new JwtAuthorizationFilter(authenticationManager(),userRepository))
				.authorizeRequests()
				.antMatchers("/oauth/**").permitAll()
				.antMatchers("/auth/**").permitAll()
				.antMatchers("/user/**").permitAll()
				.antMatchers("/code/**").permitAll()
				.antMatchers("/api/fcm/**").permitAll()
				.antMatchers("/token/reissue").permitAll()

				// delgo-map
				.antMatchers("/map/mungple").permitAll()
				.antMatchers("/survey/**").permitAll()
				.antMatchers("/photo/webp").permitAll()
				.antMatchers("/photo/upload/mungplenote/*").permitAll()
				.antMatchers("/photo/upload/mungple/*").permitAll()
				.antMatchers("/mungple").permitAll()

				.antMatchers("/**").authenticated();
//				.antMatchers("/**").permitAll();
	}
}
