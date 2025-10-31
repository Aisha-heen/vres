package com.vres.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vres.dto.GenericResponse;
import com.vres.dto.UpdateBeneficiaryStatusRequest;
import com.vres.service.BeneficiaryService;

@RestController
@RequestMapping("/vres/beneficiaries")
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @PutMapping("/status")
    public ResponseEntity<GenericResponse> updateBeneficiaryStatus(@RequestBody UpdateBeneficiaryStatusRequest request) {
        int count = beneficiaryService.updateBeneficiaryStatus(request.getBeneficiaryIds(), request.getStatus());
        return ResponseEntity.ok(new GenericResponse("Successfully updated status for " + count + " beneficiaries."));
    }
}

