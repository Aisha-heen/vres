package com.vres.service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vres.entity.Beneficiaries;
import com.vres.entity.ProjectUser;
import com.vres.entity.Redemptions;
import com.vres.entity.Users;
import com.vres.entity.Vouchers;
import com.vres.repository.BeneficiariesRepository;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.RedemptionsRepository;
import com.vres.repository.UsersRepository;
import com.vres.repository.VouchersRepository;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private BeneficiariesRepository beneficiariesRepository;

    @Autowired
    private VouchersRepository vouchersRepository;

    @Autowired
    private RedemptionsRepository redemptionsRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private UsersRepository usersRepository;

    public Map<String, Object> getDashboardByProjectId(int projectId) {
        logger.info("Fetching dashboard data for project ID: {}", projectId);

        Map<String, Object> dashboard = new HashMap<>();

        // ===================== Project Users =====================
        List<ProjectUser> projectUsers = projectUserRepository.findByProjectId(projectId);
        int totalUsers = projectUsers.size();
        logger.debug("Total project users found: {}", totalUsers);

        // Group project users by role name (lowercased)
        Map<String, List<ProjectUser>> roleGroups = projectUsers.stream()
                .filter(u -> u.getRole() != null && u.getRole().getName() != null)
                .collect(Collectors.groupingBy(u -> u.getRole().getName().trim().toLowerCase()));

        List<ProjectUser> makers = roleGroups.getOrDefault("maker", List.of());
        List<ProjectUser> checkers = roleGroups.getOrDefault("checker", List.of());
        List<ProjectUser> issuers = roleGroups.getOrDefault("issuer", List.of());
        List<ProjectUser> vendors = roleGroups.getOrDefault("vendor", List.of());

        long makerCount = makers.size();
        long checkerCount = checkers.size();
        long issuerCount = issuers.size();
        long vendorCount = vendors.stream()
                .filter(v -> v.getVendorStatus() != null && v.getVendorStatus() == 1)
                .count();

        logger.debug("Makers: {}, Checkers: {}, Issuers: {}, Vendors: {}",
                makerCount, checkerCount, issuerCount, vendorCount);

        // ===================== Beneficiaries =====================
        List<Beneficiaries> beneficiaries = beneficiariesRepository.findByProjectId(projectId);
        int totalBeneficiaries = beneficiaries.size();
        long approvedBeneficiaries = beneficiaries.stream()
                .filter(Beneficiaries::isIs_approved)
                .count();
        logger.debug("Total beneficiaries: {}, Approved: {}", totalBeneficiaries, approvedBeneficiaries);

        // ===================== Vouchers =====================
        List<Vouchers> vouchers = vouchersRepository.findByProjectId(projectId);
        long totalVouchers = vouchers.size();
        Map<String, Long> voucherStatus = voucherService.getVoucherStatusCountByProject(projectId);

        // ===================== Redemptions =====================
        long totalRedemptions = redemptionsRepository.countByVoucher_Project_Id(projectId);
        List<Redemptions> redemptionsList = redemptionsRepository.findByVoucher_Project_Id(projectId);

        // Get the most recent redemption date
        String lastRedeemedAt = redemptionsList.stream()
        	    .filter(r -> r.getRedeemed_date() != null)
        	    .map(Redemptions::getRedeemed_date)
        	    .max(Comparator.comparing(Date::getTime))
        	    .map(Date::toString)
        	    .orElse("N/A");

        // ===================== User Name Mapping =====================
        Set<Integer> allUserIds = projectUsers.stream()
                .map(ProjectUser::getUserId)
                .collect(Collectors.toSet());

        Map<Integer, String> userNameMap = usersRepository.findAllById(allUserIds)
                .stream()
                .collect(Collectors.toMap(Users::getId, Users::getName));

        String makerName = makers.stream()
                .map(u -> userNameMap.getOrDefault(u.getUserId(), "N/A"))
                .findFirst().orElse("N/A");

        String checkerName = checkers.stream()
                .map(u -> userNameMap.getOrDefault(u.getUserId(), "N/A"))
                .findFirst().orElse("N/A");

        String issuerName = issuers.stream()
                .map(u -> userNameMap.getOrDefault(u.getUserId(), "N/A"))
                .findFirst().orElse("N/A");

        String vendorName = vendors.stream()
                .filter(v -> v.getVendorStatus() != null && v.getVendorStatus() == 1)
                .map(v -> userNameMap.getOrDefault(v.getUserId(), "N/A"))
                .findFirst().orElse("N/A");

        // ===================== Beneficiary Details Enriched =====================
        List<Map<String, Object>> beneficiaryDetails = beneficiaries.stream()
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("beneficiaryId", b.getId());
                    map.put("name", b.getName());
                    map.put("phone", b.getPhone());
                    map.put("approved", b.isIs_approved());
                    map.put("makerName", makerName);
                    map.put("checkerName", checkerName);
                    map.put("issuerName", issuerName);
                    map.put("vendorName", vendorName);
                    return map;
                })
                .collect(Collectors.toList());

        // ===================== Final Dashboard Response =====================
        dashboard.put("projectId", projectId);
        dashboard.put("totalUsers", totalUsers);
        dashboard.put("makerCount", makerCount);
        dashboard.put("checkerCount", checkerCount);
        dashboard.put("issuerCount", issuerCount);
        dashboard.put("vendorCount", vendorCount);
        dashboard.put("totalBeneficiaries", totalBeneficiaries);
        dashboard.put("approvedBeneficiaries", approvedBeneficiaries);
        dashboard.put("totalVouchers", totalVouchers);
        dashboard.put("voucherStatus", voucherStatus);
        dashboard.put("totalRedemptions", totalRedemptions);
        dashboard.put("lastRedeemedAt", lastRedeemedAt);
        dashboard.put("makerName", makerName);
        dashboard.put("checkerName", checkerName);
        dashboard.put("issuerName", issuerName);
        dashboard.put("vendorName", vendorName);
        dashboard.put("beneficiaryList", beneficiaryDetails);
        dashboard.put("vouchersList", vouchers);
        dashboard.put("redemptionsList", redemptionsList);

        logger.info("Dashboard data successfully prepared for project ID: {}", projectId);
        return dashboard;
    }
}
