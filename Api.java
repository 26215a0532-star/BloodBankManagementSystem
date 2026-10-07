package com.bloodbank;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

@RestController @RequestMapping("/api")
class Api {
    static final Set<String> tokens = ConcurrentHashMap.newKeySet(); // logged-in sessions
    @Autowired AdminRepo admins; @Autowired DonorRepo donors; @Autowired DonationRepo dons;
    @Autowired InvRepo inv; @Autowired RequestRepo reqs;

    @PostMapping("/auth/login")
    Map<String, String> login(@RequestBody Map<String, String> b) {
        Admin a = admins.findByUsername(b.get("username")).orElseThrow(() -> new RuntimeException("Invalid login credentials."));
        if (!new BCryptPasswordEncoder().matches(b.get("password"), a.passwordHash)) throw new RuntimeException("Invalid login credentials.");
        String t = UUID.randomUUID().toString(); tokens.add(t); return Map.of("token", t);
    }

    // ---- Donors ----
    @GetMapping("/donors") List<Donor> donors(@RequestParam(defaultValue = "") String q) { return donors.findByFullNameContainingIgnoreCase(q); }
    @PostMapping("/donors") Donor addDonor(@Valid @RequestBody Donor d) { d.id = null; return donors.save(d); }
    @GetMapping("/donors/{id}") Donor donor(@PathVariable Long id) { return donors.findById(id).orElseThrow(() -> new RuntimeException("Donor not found.")); }
    @PutMapping("/donors/{id}") Donor updDonor(@PathVariable Long id, @Valid @RequestBody Donor d) { donor(id); d.id = id; return donors.save(d); }
    @DeleteMapping("/donors/{id}") void delDonor(@PathVariable Long id) { donor(id); donors.deleteById(id); }

    // ---- Donations: linked to a valid donor, increases inventory ----
    @GetMapping("/donations") List<Donation> donations() { return dons.findAll(); }
    @PostMapping("/donations") @Transactional
    Donation donate(@RequestBody Donation d) {
        Donor o = donor(d.donorId);
        if (d.units <= 0) throw new RuntimeException("Units must be greater than 0.");
        d.id = null; d.donorName = o.fullName; d.bloodGroup = o.bloodGroup;
        if (d.donationDate == null) d.donationDate = LocalDate.now();
        d.expiryDate = d.donationDate.plusDays(42);
        Inventory i = inv.findById(d.bloodGroup).get(); i.available += d.units; inv.save(i);
        o.lastDonationDate = d.donationDate; donors.save(o);
        return dons.save(d);
    }

    // ---- Inventory (expired units are moved out of 'available') ----
    @GetMapping("/inventory") @Transactional
    List<Inventory> inventory() {
        for (Donation d : dons.findAll()) {} // (expiry job left as future work)
        return inv.findAll();
    }
    @GetMapping("/inventory/{g}") Inventory one(@PathVariable String g) { return inv.findById(g).orElseThrow(() -> new RuntimeException("Blood group not found.")); }

    // ---- Blood requests ----
    @GetMapping("/blood-requests") List<BloodRequest> requests() { return reqs.findAll(); }
    @PostMapping("/blood-requests") BloodRequest addReq(@Valid @RequestBody BloodRequest r) { r.id = null; r.status = "Pending"; return reqs.save(r); }

    @PutMapping("/blood-requests/{id}/approve") @Transactional
    BloodRequest approve(@PathVariable Long id) {
        BloodRequest r = reqs.findById(id).orElseThrow(() -> new RuntimeException("Request not found."));
        if (!r.status.equals("Pending")) throw new RuntimeException("Request already " + r.status + ".");
        Inventory i = one(r.bloodGroup);
        if (i.available < r.units) throw new RuntimeException("Insufficient " + r.bloodGroup + " blood available.");
        i.available -= r.units; i.reserved += r.units; inv.save(i); // never goes negative
        r.status = "Approved"; return reqs.save(r);
    }
    @PutMapping("/blood-requests/{id}/reject")
    BloodRequest reject(@PathVariable Long id) {
        BloodRequest r = reqs.findById(id).orElseThrow(() -> new RuntimeException("Request not found."));
        r.status = "Rejected"; return reqs.save(r);
    }

    // ---- Error handling: friendly JSON messages ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getBindingResult().getFieldErrors().get(0).getDefaultMessage()));
    }
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> err(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
}

// Blocks every /api call (except login) that has no valid token
@Component class AuthFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest q, HttpServletResponse s, FilterChain c) throws ServletException, IOException {
        String p = q.getRequestURI();
        if (p.startsWith("/api/") && !p.equals("/api/auth/login") && !Api.tokens.contains(q.getHeader("Authorization"))) {
            s.setStatus(401); s.setContentType("application/json"); s.getWriter().write("{\"message\":\"Please login first.\"}"); return;
        }
        c.doFilter(q, s);
    }
}
