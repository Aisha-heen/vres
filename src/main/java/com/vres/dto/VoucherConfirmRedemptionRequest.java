package com.vres.dto;

public class VoucherConfirmRedemptionRequest {
    private String voucherCode;
    private String otp;
    private int vendorId;
    private Double geo_lat;
    private Double geo_lon;
    private String deviceFingerprint;

    public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public Double getGeo_lat() {
		return geo_lat;
	}

	public void setGeo_lat(Double geo_lat) {
		this.geo_lat = geo_lat;
	}

	public Double getGeo_lon() {
		return geo_lon;
	}

	public void setGeo_lon(Double geo_lon) {
		this.geo_lon = geo_lon;
	}

	public String getDeviceFingerprint() {
		return deviceFingerprint;
	}

	public void setDeviceFingerprint(String deviceFingerprint) {
		this.deviceFingerprint = deviceFingerprint;
	}

	// Getters and Setters
    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
