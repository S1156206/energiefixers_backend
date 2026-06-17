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
import java.util.Set;

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

        Region noord   = createRegion("Leiden-Noord",   Set.of("2316", "2317"));
        Region zuid    = createRegion("Leiden-Zuid",    Set.of("2321", "2322", "2323"));
        Region centrum = createRegion("Leiden-Centrum", Set.of("2311", "2312", "2313"));

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

        Material radiatorventilator = createMaterial(
                "Radiatorventilatoren",
                "Ventilator voor radiator om warmteafgifte te verbeteren",
                "71.37", "25.00", "5.00",
                Material.Category.VENTILATION);

        createMaterial(
                "Extra ventilator kabeltje",
                "Aansluitkabeltje voor radiatorventilator",
                "1.71", "0.00", "0.00",
                Material.Category.VENTILATION);

        createMaterial(
                "Radiatorfolie rol 40 cm",
                "Reflecterende folie achter radiator, 40 cm breed",
                "26.72", "12.00", "0.00",
                Material.Category.INSULATION);

        createMaterial(
                "Radiatorfolie rol 50 cm",
                "Reflecterende folie achter radiator, 50 cm breed",
                "30.35", "15.00", "0.00",
                Material.Category.INSULATION);

        createMaterial(
                "Magneten",
                "Magneten voor bevestiging van radiatorfolie",
                "0.30", "0.00", "0.00",
                Material.Category.OTHER);

        createMaterial(
                "Rol magneetband",
                "Zelfklevende magneetband voor bevestiging van radiatorfolie",
                "18.13", "0.00", "0.00",
                Material.Category.OTHER);

        Material douchekop = createMaterial(
                "Besparende douchekop",
                "Waterbesparende douchekop met verminderd debiet",
                "22.69", "35.00", "0.00",
                Material.Category.WATER);

        createMaterial(
                "Douchetimer",
                "Timer om douchtijd te beperken en warm waterverbruik te verminderen",
                "1.83", "15.00", "0.00",
                Material.Category.WATER);

        createMaterial(
                "Stekkerdoos groot",
                "Grote stekkerdoos met schakelaar om standby-verbruik te reduceren",
                "25.75", "0.00", "35.00",
                Material.Category.OTHER);

        createMaterial(
                "Stekkerdoos klein",
                "Kleine stekkerdoos met schakelaar om standby-verbruik te reduceren",
                "3.00", "0.00", "20.00",
                Material.Category.OTHER);

        createMaterial(
                "Deur tochtband",
                "Tochtband voor onderzijde van deuren",
                "1.60", "5.00", "0.00",
                Material.Category.INSULATION);

        createMaterial(
                "Deurdranger veer",
                "Veer om deuren automatisch te sluiten en warmteverlies te beperken",
                "3.40", "8.00", "0.00",
                Material.Category.INSULATION);

        Material tochtstrip_p = createMaterial(
                "Tochtstrip P",
                "P-profiel tochtstrip voor raam- en deurkozijnen",
                "3.30", "10.00", "0.00",
                Material.Category.INSULATION);

        createMaterial(
                "Tochtstrip M",
                "M-profiel tochtstrip voor raam- en deurkozijnen",
                "2.90", "8.00", "0.00",
                Material.Category.INSULATION);

        createMaterial(
                "4,9W (40W) LED E27",
                "LED-lamp E27 4,9W ter vervanging van 40W gloeilamp",
                "1.44", "0.00", "52.00",
                Material.Category.LIGHTING);

        Material led_e27_7w = createMaterial(
                "7W (60W) LED E27",
                "LED-lamp E27 7W ter vervanging van 60W gloeilamp",
                "2.13", "0.00", "79.00",
                Material.Category.LIGHTING);

        createMaterial(
                "2,5W (25W) LED E14",
                "LED-lamp E14 2,5W ter vervanging van 25W gloeilamp",
                "2.00", "0.00", "34.00",
                Material.Category.LIGHTING);

        createMaterial(
                "Hygrometer",
                "Vochtmeter om luchtvochtigheid te monitoren en schimmel te voorkomen",
                "4.40", "0.00", "0.00",
                Material.Category.OTHER);

        createMaterial(
                "Brievenbusborstel",
                "Borsteldichting voor brievenbus om tocht te voorkomen",
                "6.26", "3.00", "0.00",
                Material.Category.INSULATION);

        // Visit 1 — prop1 — 2024-03-15
        FixVisit visit1 = new FixVisit();
        visit1.setProperty(prop1);
        visit1.setVisitDate(LocalDate.of(2024, 3, 15));
        visit1.setNotes("Radiatorventilator en LED-verlichting geplaatst. Bewoner aanwezig.");
        visit1.setTotalMaterialCost(new BigDecimal("75.63"));
        visit1.setInstalledMaterials(List.of(
                installedMaterial(visit1, radiatorventilator, 1),
                installedMaterial(visit1, led_e27_7w, 2)
        ));
        fixVisitRepository.save(visit1);

        // Visit 2 — prop2 — 2024-06-01
        FixVisit visit2 = new FixVisit();
        visit2.setProperty(prop2);
        visit2.setVisitDate(LocalDate.of(2024, 6, 1));
        visit2.setNotes("Besparende douchekop en tochtstrips geïnstalleerd.");
        visit2.setTotalMaterialCost(new BigDecimal("29.29"));
        visit2.setInstalledMaterials(List.of(
                installedMaterial(visit2, douchekop, 1),
                installedMaterial(visit2, tochtstrip_p, 2)
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

    private Region createRegion(String name, Set<String> postcodePrefixes) {
        Region r = new Region();
        r.setName(name);
        r.setPostcodePrefixes(postcodePrefixes);
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
