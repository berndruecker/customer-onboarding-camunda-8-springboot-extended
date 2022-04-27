package io.berndruecker.onboarding.customer.rest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;

@RestController
public class CustomerOnboardingRestController {

  private Logger logger = LoggerFactory.getLogger(CustomerOnboardingRestController.class);

  @Autowired
  private ZeebeClient client;

  @PutMapping("/customer")
  public ResponseEntity<CustomerOnboardingResponse> onboardCustomer(ServerWebExchange exchange) {
    String paymentType = exchange.getRequest().getQueryParams().getFirst("paymentType");
    if (paymentType==null) {paymentType = "prepaid";}
    String monthlyPayment = exchange.getRequest().getQueryParams().getFirst("monthlyPayment");
    if (monthlyPayment==null) {monthlyPayment = "45";}
    String customerRegionScore = exchange.getRequest().getQueryParams().getFirst("customerRegionScore");
    if (customerRegionScore==null) {customerRegionScore = "40";}

    onboardCustomer(paymentType, Integer.parseInt(monthlyPayment), Integer.parseInt(customerRegionScore));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  public long onboardCustomer(String paymentType, int monthlyPayment, int customerRegionScore) {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("paymentType", paymentType);
    variables.put("monthlyPayment", monthlyPayment);
    variables.put("customerRegionScore", customerRegionScore);

    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand() //
            .bpmnProcessId("customer-onboarding-extended") //
            .latestVersion() //
            .variables(variables) //
            .send().join();
    return processInstance.getProcessInstanceKey();
  }

  public static class CustomerOnboardingResponse {
  }
}
