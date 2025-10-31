package com.vres.dto;

public class VoucherInitiateRedemptionRequest {
    private String voucherCode;
    
    private int vendorId;

    public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	// Getter and Setter
    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}
