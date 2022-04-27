package io.berndruecker.onboarding.customer;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources =  {"classpath*:*.bpmn", "classpath*:*.dmn"})
public class CustomerOnboardingSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerOnboardingSpringbootApplication.class, args);
	}

	@Bean
	@ConditionalOnMissingBean(RestTemplateBuilder.class)
	public RestTemplateBuilder restTemplateBuilder() {
		return new RestTemplateBuilder();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
}
