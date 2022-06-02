package io.berndruecker.onboarding.customer;

import io.berndruecker.onboarding.customer.rest.CustomerOnboardingRestController;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({ "test" })
@ZeebeSpringTest
//@RestClientTest(CustomerOnboardingGlueCode.class)
@AutoConfigureWebClient
//@AutoConfigureMockRestServiceServer
@ContextConfiguration(initializers = TestCustomerOnboardingProcess.RabbitMqTestcontainersInitializer.class)
public class TestCustomerOnboardingProcess {

    private static GenericContainer rabbitmqContainer = new GenericContainer("rabbitmq").withExposedPorts(5672);

    @Autowired
    private CustomerOnboardingRestController customerOnboardingRestController;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockRestServer;

    static class RabbitMqTestcontainersInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        // Because of issues with @Container (close to https://stackoverflow.com/questions/61357116/exception-mapped-port-can-only-be-obtained-after-the-container-is-started-when)
        // reverting to "the old way" like with https://spring.io/blog/2020/03/27/dynamicpropertysource-in-spring-framework-5-2-5-and-spring-boot-2-2-6
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            rabbitmqContainer = new GenericContainer("rabbitmq").withExposedPorts(5672); // 15672
            rabbitmqContainer.start();
            TestPropertyValues.of("spring.rabbitmq.port=" + rabbitmqContainer.getMappedPort(5672))
                    .applyTo(context.getEnvironment());
        }
    }

    @BeforeEach
    public void setupMocks() {
        // Can't simply use the autowired MockRestServiceServer as I have to set "ignoreExpectOrder" for the parallel pathes
        mockRestServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    public void testAutomaticOnboarding() throws Exception {
        // Define expectations on the REST calls
        // 1. http://localhost:8080/crm/customer
        mockRestServer
                .expect(requestTo("http://localhost:8080/crm/customer")) //
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"customerId\": \"12345\"}", MediaType.APPLICATION_JSON));
        // 2. http://localhost:8080/crm/customer
        mockRestServer
                .expect(requestTo("http://localhost:8080/billing/customer")) //
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"customerId\": \"4711\"}", MediaType.APPLICATION_JSON));

        // given a REST call with those parameters
        long processInstanceKey = customerOnboardingRestController.onboardCustomer("prepaid", 45, 50);

        // Retrieve process instances started because of the above call
        //InspectedProcessInstance processInstance = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
        InspectedProcessInstance processInstance = new InspectedProcessInstance(processInstanceKey);

        // Now the process should run to the end
        waitForProcessInstanceCompleted(processInstance, Duration.ofSeconds(10));

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .hasPassedElement("EndEventProcessedAutomatically")
                .isCompleted();

        // And verify it caused the right side effects on the REST endpoints
        mockRestServer.verify();
    }



    @Test
    public void testManualOnboarding() throws Exception {
        // given a REST call with those parameters
        long processInstanceKey = customerOnboardingRestController.onboardCustomer("invoice", 25, 30);

        // Retrieve process instances started because of the above call
        //InspectedProcessInstance processInstance = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
        InspectedProcessInstance processInstance = new InspectedProcessInstance(processInstanceKey);

        // wait for asynchronous handling around messaging to finish
        waitForProcessInstanceHasPassedElement(processInstance, "GatewayCanBeProcessesAutomatically");
        // We expect to have a user task
        waitForUserTaskAndComplete("TaskProcessApplicationManually");

        // Now the process should run to the end
        waitForProcessInstanceCompleted(processInstance, Duration.ofSeconds(10));

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .hasPassedElement("EndEventProcessesManually")
                .isCompleted();

        // And verify no REST endpoint was called
        mockRestServer.verify();
    }

    @Autowired
    private ZeebeTestEngine zeebeTestEngine;
    @Autowired
    private ZeebeClient zeebeClient;

    public void waitForUserTaskAndComplete(String userTaskId) throws InterruptedException, TimeoutException {
        waitForUserTaskAndComplete(userTaskId, new HashMap<>());
    }

    public void waitForUserTaskAndComplete(String userTaskId, Map<String, Object> variables) throws InterruptedException, TimeoutException {
        // Let the workflow engine do whatever it needs to do
        zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));

        // Now get all user tasks
        List<ActivatedJob> jobs = zeebeClient.newActivateJobsCommand().jobType(USER_TASK_JOB_TYPE).maxJobsToActivate(1).workerName("waitForUserTaskAndComplete").send().join().getJobs();

        // Should be only one
        assertTrue(jobs.size()>0, "Job for user task '" + userTaskId + "' does not exist");
        ActivatedJob userTaskJob = jobs.get(0);
        // Make sure it is the right one
        if (userTaskId!=null) {
            assertEquals(userTaskId, userTaskJob.getElementId());
        }

        // And complete it passing the variables
        if (variables!=null && variables.size()>0) {
            zeebeClient.newCompleteCommand(userTaskJob.getKey()).variables(variables).send().join();
        } else {
            zeebeClient.newCompleteCommand(userTaskJob.getKey()).send().join();
        }
    }


}
