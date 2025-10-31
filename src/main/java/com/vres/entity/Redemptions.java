package com.vres.entity;

import java.sql.Date;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "redemptions")
public class Redemptions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign Key to the redeemed voucher
    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Vouchers voucher;

    // Foreign Key to the Vendor (a User) who performed the redemption
    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Users vendor;

    @Column(name = "redeemed_at") // Using redeemed_at to align with the database
    private Date redeemed_date; // Using the type defined in your service (java.sql.Date)

    @Column(name = "geo_lat")
    private Double geo_lat;

    @Column(name = "geo_lon")
    private Double geo_lon;

    @Column(name = "device_fingerprint")
    private String device_fingerprint;
    
    @CreationTimestamp // This annotation automatically sets the timestamp when a user is created
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate created_at;

    // --- Getters and Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Vouchers getVoucher() { return voucher; }
    public void setVoucher(Vouchers voucher) { this.voucher = voucher; }

    public Users getVendor() { return vendor; }
    public void setVendor(Users vendor) { this.vendor = vendor; }

    public Date getRedeemed_date() { return redeemed_date; }
    public void setRedeemed_date(Date redeemed_date) { this.redeemed_date = redeemed_date; }

    public Double getGeo_lat() { return geo_lat; }
    public void setGeo_lat(Double geo_lat) { this.geo_lat = geo_lat; }

    public Double getGeo_lon() { return geo_lon; }
    public void setGeo_lon(Double geo_lon) { this.geo_lon = geo_lon; }

    public String getDevice_fingerprint() { return device_fingerprint; }
    public void setDevice_fingerprint(String device_fingerprint) { this.device_fingerprint = device_fingerprint; }
	public LocalDate getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
}
