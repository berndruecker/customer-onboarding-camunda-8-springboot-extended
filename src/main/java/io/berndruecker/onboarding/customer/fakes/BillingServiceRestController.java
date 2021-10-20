package io.berndruecker.onboarding.customer.fakes;

import io.berndruecker.onboarding.customer.rest.CustomerOnboardingRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class BillingServiceRestController {

    private Logger logger = LoggerFactory.getLogger(BillingServiceRestController.class);

    @PutMapping("/billing/customer")
    public ResponseEntity<String> addCustomerToBillingSystem(ServerWebExchange exchange) {
        logger.info("Add customer to billing system...");
        return ResponseEntity.status(HttpStatus.OK).build(); // add body?
    }

}
