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
public class CrmServiceRestController {

    private Logger logger = LoggerFactory.getLogger(CrmServiceRestController.class);

    @PutMapping("/crm/customer")
    public ResponseEntity<String> onboardCustomer(ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.OK).build(); // add body?
    }

}
