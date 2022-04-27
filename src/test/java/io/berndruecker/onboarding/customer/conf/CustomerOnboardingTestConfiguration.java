package io.berndruecker.onboarding.customer.conf;

import io.berndruecker.onboarding.customer.CustomerOnboardingSpringbootApplication;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

@ComponentScan(basePackageClasses={CustomerOnboardingSpringbootApplication.class})
@EnableAutoConfiguration(exclude={RabbitAutoConfiguration.class})
@TestConfiguration
public class CustomerOnboardingTestConfiguration {

    @Mock
    private RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

    @Bean
    protected RabbitTemplate rabbitTemplate() {
        return rabbitTemplate;
    }


}
