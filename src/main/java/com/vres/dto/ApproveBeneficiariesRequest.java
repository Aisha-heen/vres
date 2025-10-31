package com.vres.dto;

import java.util.List;

public class ApproveBeneficiariesRequest {
    private List<Integer> beneficiaryIds;

    public List<Integer> getBeneficiaryIds() {
        return beneficiaryIds;
    }

    public void setBeneficiaryIds(List<Integer> beneficiaryIds) {
        this.beneficiaryIds = beneficiaryIds;
    }
}
