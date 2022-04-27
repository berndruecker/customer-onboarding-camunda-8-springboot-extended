package io.berndruecker.onboarding.customer.process;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class CustomerOnboardingGlueCode {

    Logger logger = LoggerFactory.getLogger(CustomerOnboardingGlueCode.class);

    // TODO: This should be of course injected and depends on the environment.
    // Hard coded for now
    public static String ENDPOINT_CRM = "http://localhost:8080/crm/customer";
    public static String ENDPOINT_BILLING = "http://localhost:8080/billing/customer";

    @Autowired
    private RestTemplate restTemplate;

    @ZeebeWorker(type = "addCustomerToCrm", autoComplete = true)
    public void addCustomerToCrmViaREST(final ActivatedJob job) throws IOException {
        logger.info("Add customer to CRM via REST [" + job + "]");

        // call rest API
        String request = "todo";
        restTemplate.put(ENDPOINT_CRM, request);
    }

    @ZeebeWorker(type = "addCustomerToBilling", autoComplete = true)
    public void addCustomerToBillingViaREST(final ActivatedJob job) throws IOException {
        logger.info("Add customer to Billing via REST [" + job + "]");

        // call rest API
        String request = "todo";
        restTemplate.put(ENDPOINT_BILLING, request);
    }

    @ZeebeWorker(type="provisionSIM", autoComplete = true)
    public void provisionSimCard() {
        logger.info("Provisioning SIM card...");
        //if (true) {
            //throw new RuntimeException("CANOT CONNECT TO SIM CARD SYSTEM. OH NOOOOOO.");
        //}
    }

    @ZeebeWorker(type="registerSIM", autoComplete = true)
    public void registerSimCard() {
        logger.info("Registering SIM card...");
    }
}
