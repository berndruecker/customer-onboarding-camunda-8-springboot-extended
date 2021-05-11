package io.berndruecker.onboarding.customer;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResources = {"customer-onboarding-extended.bpmn", "customer-scoring.bpmn" })
public class CustomerOnboardingSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerOnboardingSpringbootApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
