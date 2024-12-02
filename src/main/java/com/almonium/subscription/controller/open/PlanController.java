package com.almonium.subscription.controller.open;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.subscription.dto.PlanDto;
import com.almonium.subscription.service.PlanSubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/plans")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PlanController {
    PlanSubscriptionService planSubscriptionService;

    @GetMapping
    public ResponseEntity<List<PlanDto>> getPlans() {
        return ResponseEntity.ok(planSubscriptionService.getAllPlans());
    }
}