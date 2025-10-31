package com.vres.dto;

public class VendorAssignmentDto {
    private int userId;
    private String gst;
    private String address;

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getGst() { return gst; }
    public void setGst(String gst) { this.gst = gst; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}