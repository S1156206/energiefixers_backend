package com.energiefixers.backend.config;

import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.repository.InvitationRepository;
import com.energiefixers.backend.property.models.FixRound;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Region;
import com.energiefixers.backend.property.repository.FixRoundRepository;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.property.repository.RegionRepository;
import com.energiefixers.backend.user.models.Role;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.models.InstalledMaterial;
import com.energiefixers.backend.visit.models.Material;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import com.energiefixers.backend.visit.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final FixVisitRepository fixVisitRepository;
    private final EnergyReadingRepository energyReadingRepository;
    private final InvitationRepository invitationRepository;
    private final FixRoundRepository fixRoundRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (regionRepository.count() > 0) {
            log.info("DataSeeder: database already contains data, skipping.");
            return;
        }
        log.info("DataSeeder: seeding test data...");

        Region noord  = createRegion("Leiden-Noord",   "2316");
        Region zuid   = createRegion("Leiden-Zuid",    "2321");
        Region centrum = createRegion("Leiden-Centrum", "2311");

        FixRound ronde1 = createFixRound("Ronde 1", LocalDate.of(2023, 1,  1),  LocalDate.of(2023, 3, 31), false);
        FixRound ronde2 = createFixRound("Ronde 2", LocalDate.of(2023, 7,  1),  LocalDate.of(2023, 8, 31), false);
        FixRound ronde3 = createFixRound("Ronde 3", LocalDate.of(2024, 2,  1),  LocalDate.of(2024, 3, 31), false);
        FixRound ronde4 = createFixRound("Ronde 4", LocalDate.of(2024, 5,  1),  LocalDate.of(2024, 6, 30), false);
        FixRound ronde5 = createFixRound("Ronde 5", LocalDate.of(2024, 9,  1),  LocalDate.of(2024, 10, 31), true);

        Property prop1 = createProperty("Rijnsburgerweg", "14",  null, "2316HA", Property.EnergyLabel.E, Property.EnergyLabel.C, noord,   ronde3);
        Property prop2 = createProperty("Tjalklaan",       "7",  "B",  "2316KP", Property.EnergyLabel.F, Property.EnergyLabel.D, noord,   ronde4);
        Property prop3 = createProperty("Kettingstraat",   "22", null, "2321BK", Property.EnergyLabel.D, null,                    zuid,    ronde5);
        Property prop4 = createProperty("Lange Mare",      "45", null, "2312GT", Property.EnergyLabel.G, null,                    centrum, ronde5);

        createUser("admin@energiefixers.nl",    "Admin@1234",   Role.ADMIN,  "Admin",    null);
        createUser("staff@energiefixers.nl",    "Staff@1234",   Role.STAFF,  "Pieter",   null);
        createUser("huurder1@energiefixers.nl", "Huurder@1234", Role.TENANT, "Jan",      prop1);
        createUser("huurder2@energiefixers.nl", "Huurder@1234", Role.TENANT, "Lena",     prop2);
        createUser("huurder3@energiefixers.nl", "Huurder@1234", Role.TENANT, "Mohammed", prop3);
        createUser("huurder4@energiefixers.nl", "Huurder@1234", Role.TENANT, "Sara",     prop4);

        Material dakisolatie = createMaterial(
                "Dakisolatie",
                "Isolatiedeken voor platte en schuine daken",
                "89.99", "120.00", "0.00",
                Material.Category.INSULATION);

        Material spouwmuur = createMaterial(
                "Spouwmuurisolatie",
                "Ingeblazen isolatiemateriaal voor spouwmuren",
                "75.00", "95.00", "0.00",
                Material.Category.INSULATION);

        Material led = createMaterial(
                "LED-verlichting set",
                "Set van 6 LED-lampen (E27, 8W)",
                "24.95", "0.00", "180.00",
                Material.Category.LIGHTING);

        Material douchekop = createMaterial(
                "Waterbesparende douchekop",
                "Spaardouchekop met max. 7 liter/minuut",
                "19.99", "30.00", "0.00",
                Material.Category.WATER);

        createMaterial(
                "WTW-ventilatie-unit",
                "Warmteterugwinningsunit voor gebalanceerde ventilatie",
                "149.00", "60.00", "40.00",
                Material.Category.VENTILATION);

        // Visit 1 — prop1 — 2024-03-15
        FixVisit visit1 = new FixVisit();
        visit1.setProperty(prop1);
        visit1.setVisitDate(LocalDate.of(2024, 3, 15));
        visit1.setNotes("Dakisolatie en LED-verlichting geplaatst. Bewoner aanwezig.");
        visit1.setTotalMaterialCost(new BigDecimal("114.94"));
        visit1.setInstalledMaterials(List.of(
                installedMaterial(visit1, dakisolatie, 1),
                installedMaterial(visit1, led, 1)
        ));
        fixVisitRepository.save(visit1);

        // Visit 2 — prop2 — 2024-06-01
        FixVisit visit2 = new FixVisit();
        visit2.setProperty(prop2);
        visit2.setVisitDate(LocalDate.of(2024, 6, 1));
        visit2.setNotes("Spouwmuurisolatie en waterbesparende douchekop geïnstalleerd.");
        visit2.setTotalMaterialCost(new BigDecimal("94.99"));
        visit2.setInstalledMaterials(List.of(
                installedMaterial(visit2, spouwmuur, 1),
                installedMaterial(visit2, douchekop, 1)
        ));
        fixVisitRepository.save(visit2);

        // Energy readings — prop1 (before and after visit on 2024-03-15)
        createEnergyReading(prop1, LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1), "1850.00", "3200.00", "2890.00");
        createEnergyReading(prop1, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), "1580.00", "2780.00", "2410.00");

        // Energy readings — prop2 (before and after visit on 2024-06-01)
        createEnergyReading(prop2, LocalDate.of(2022, 6, 1), LocalDate.of(2023, 6, 1), "2100.00", "2900.00", "3200.00");
        createEnergyReading(prop2, LocalDate.of(2025, 6, 1), LocalDate.of(2026, 6, 1), "1820.00", "2650.00", "2740.00");

        // Pending invitations for properties without a registered tenant yet
        createInvitation(prop3, "huurder3@energiefixers.nl");
        createInvitation(prop4, "huurder4@energiefixers.nl");

        log.info("DataSeeder: seeding completed.");
    }

    private Region createRegion(String name, String postcodePrefix) {
        Region r = new Region();
        r.setName(name);
        r.setPostcodePrefix(postcodePrefix);
        return regionRepository.save(r);
    }

    private FixRound createFixRound(String name, LocalDate startDate, LocalDate endDate, boolean current) {
        FixRound r = new FixRound();
        r.setName(name);
        r.setStartDate(startDate);
        r.setEndDate(endDate);
        r.setCurrent(current);
        return fixRoundRepository.save(r);
    }

    private Property createProperty(String street, String houseNumber, String suffix,
                                    String postcode, Property.EnergyLabel before,
                                    Property.EnergyLabel after, Region region, FixRound fixRound) {
        Property p = new Property();
        p.setStreet(street);
        p.setHouseNumber(houseNumber);
        p.setHouseNumberSuffix(suffix);
        p.setPostcode(postcode);
        p.setEnergyLabelBefore(before);
        p.setEnergyLabelAfter(after);
        p.setRegion(region);
        p.setFixRound(fixRound);
        return propertyRepository.save(p);
    }

    private void createUser(String email, String password, Role role, String firstName, Property property) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        u.setFirstName(firstName);
        u.setProperty(property);
        userRepository.save(u);
    }

    private Material createMaterial(String name, String description,
                                    String price, String gasSaving, String electricitySaving,
                                    Material.Category category) {
        Material m = new Material();
        m.setName(name);
        m.setDescription(description);
        m.setPriceEuros(new BigDecimal(price));
        m.setEstimatedGasSavingM3(new BigDecimal(gasSaving));
        m.setEstimatedElectricitySavingKwh(new BigDecimal(electricitySaving));
        m.setCategory(category);
        return materialRepository.save(m);
    }

    private InstalledMaterial installedMaterial(FixVisit visit, Material material, int quantity) {
        InstalledMaterial im = new InstalledMaterial();
        im.setFixVisit(visit);
        im.setMaterial(material);
        im.setQuantity(quantity);
        return im;
    }

    private void createEnergyReading(Property property, LocalDate start, LocalDate end,
                                     String gas, String electricity, String cost) {
        EnergyReading er = new EnergyReading();
        er.setProperty(property);
        er.setPeriodStart(start);
        er.setPeriodEnd(end);
        er.setGasUsageM3(new BigDecimal(gas));
        er.setElectricityUsageKwh(new BigDecimal(electricity));
        er.setTotalCostEuros(new BigDecimal(cost));
        er.setSourceType(EnergyReading.SourceType.ANNUAL_BILL_MANUAL);
        energyReadingRepository.save(er);
    }

    private void createInvitation(Property property, String recipientEmail) {
        Invitation inv = new Invitation();
        inv.setProperty(property);
        inv.setType(Invitation.InvitationType.REGISTRATION);
        inv.setRecipientEmail(recipientEmail);
        invitationRepository.save(inv);
    }
}
