package com.bloodbank;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity class Admin { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true, nullable = false) public String username; @Column(nullable = false) public String passwordHash; }

@Entity class Donor { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @NotBlank(message = "Name is required") public String fullName;
    @Min(value = 18, message = "Donor must be 18-65 years old") @Max(value = 65, message = "Donor must be 18-65 years old") public int age;
    public String gender;
    @Pattern(regexp = "A[+-]|B[+-]|AB[+-]|O[+-]", message = "Invalid blood group") public String bloodGroup;
    @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") public String phone;
    @Email(message = "Invalid email") public String email;
    public String address; public LocalDate lastDonationDate; public boolean medicallyEligible = true;
    public LocalDate registrationDate = LocalDate.now(); }

@Entity class Donation { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long donorId; public String donorName, bloodGroup, collectionCenter;
    public int units; public LocalDate donationDate, expiryDate; }

@Entity class Inventory { @Id public String bloodGroup; public int available, reserved, expired; }

@Entity class BloodRequest { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @NotBlank public String patientName, hospitalName; @NotBlank public String bloodGroup;
    @Min(1) public int units; public String reason, priority = "Normal", status = "Pending";
    public LocalDate requestDate = LocalDate.now(), requiredDate; }

interface AdminRepo extends JpaRepository<Admin, Long> { Optional<Admin> findByUsername(String u); }
interface DonorRepo extends JpaRepository<Donor, Long> { List<Donor> findByFullNameContainingIgnoreCase(String q); }
interface DonationRepo extends JpaRepository<Donation, Long> {}
interface InvRepo extends JpaRepository<Inventory, String> {}
interface RequestRepo extends JpaRepository<BloodRequest, Long> {}
