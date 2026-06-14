package com.energiefixers.backend.visit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energiefixers.backend.visit.models.FixVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface FixVisitRepository extends JpaRepository<FixVisit, Long> {
    List<FixVisit> findAllByPropertyId(Long propertyId);
    List<FixVisit> findAllByPropertyIdIn(Collection<Long> propertyIds);

    @Query("SELECT SUM(fv.totalMaterialCost) FROM FixVisit fv")
    BigDecimal sumTotalMaterialCost();

    @Query("SELECT SUM(fv.totalMaterialCost) FROM FixVisit fv WHERE fv.property.region.id = :regionId")
    BigDecimal sumTotalMaterialCostByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT SUM(im.quantity * m.estimatedGasSavingM3) FROM FixVisit fv JOIN fv.installedMaterials im JOIN im.material m")
    BigDecimal sumEstimatedGasSavings();

    @Query("SELECT SUM(im.quantity * m.estimatedElectricitySavingKwh) FROM FixVisit fv JOIN fv.installedMaterials im JOIN im.material m")
    BigDecimal sumEstimatedElectricitySavings();

    @Query("SELECT SUM(im.quantity * m.estimatedGasSavingM3) FROM FixVisit fv JOIN fv.installedMaterials im JOIN im.material m WHERE fv.property.region.id = :regionId")
    BigDecimal sumEstimatedGasSavingsByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT SUM(im.quantity * m.estimatedElectricitySavingKwh) FROM FixVisit fv JOIN fv.installedMaterials im JOIN im.material m WHERE fv.property.region.id = :regionId")
    BigDecimal sumEstimatedElectricitySavingsByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT fv.property.id, fv.property.street, fv.property.houseNumber, fv.property.postcode, fv.visitDate, " +
           "fv.totalMaterialCost, " +
           "SUM(im.quantity * m.estimatedGasSavingM3), SUM(im.quantity * m.estimatedElectricitySavingKwh) " +
           "FROM FixVisit fv LEFT JOIN fv.installedMaterials im LEFT JOIN im.material m " +
           "GROUP BY fv.property.id, fv.property.street, fv.property.houseNumber, fv.property.postcode, fv.visitDate, fv.totalMaterialCost")
    List<Object[]> getHouseholdSavingsBase();

    @Query("SELECT COUNT(DISTINCT fv.property.id), COUNT(fv.id), " +
           "COALESCE(SUM(fv.totalMaterialCost), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedGasSavingM3), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedElectricitySavingKwh), 0) " +
           "FROM FixVisit fv LEFT JOIN fv.installedMaterials im LEFT JOIN im.material m " +
           "WHERE (:from IS NULL OR fv.visitDate >= :from) " +
           "AND (:to IS NULL OR fv.visitDate <= :to)")
    Object[] getSavingsTotals(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT fv.property.region.id, fv.property.region.name, " +
           "COUNT(DISTINCT fv.property.id), COUNT(fv.id), " +
           "COALESCE(SUM(fv.totalMaterialCost), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedGasSavingM3), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedElectricitySavingKwh), 0) " +
           "FROM FixVisit fv LEFT JOIN fv.installedMaterials im LEFT JOIN im.material m " +
           "WHERE (:from IS NULL OR fv.visitDate >= :from) " +
           "AND (:to IS NULL OR fv.visitDate <= :to) " +
           "GROUP BY fv.property.region.id, fv.property.region.name")
    List<Object[]> getSavingsByRegion(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT fv.property.id, fv.property.street, fv.property.houseNumber, fv.property.postcode, " +
           "fv.property.region.id, fv.property.region.name, t.firstName, t.email, " +
           "COUNT(fv.id), " +
           "COALESCE(SUM(fv.totalMaterialCost), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedGasSavingM3), 0), " +
           "COALESCE(SUM(im.quantity * m.estimatedElectricitySavingKwh), 0) " +
           "FROM FixVisit fv " +
           "LEFT JOIN fv.property.tenant t " +
           "LEFT JOIN fv.installedMaterials im LEFT JOIN im.material m " +
           "WHERE (:regionId IS NULL OR fv.property.region.id = :regionId) " +
           "AND (:from IS NULL OR fv.visitDate >= :from) " +
           "AND (:to IS NULL OR fv.visitDate <= :to) " +
           "GROUP BY fv.property.id, fv.property.street, fv.property.houseNumber, fv.property.postcode, " +
           "fv.property.region.id, fv.property.region.name, t.firstName, t.email")
    List<Object[]> getSavingsByProperty(@Param("regionId") Long regionId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

    @Query("SELECT im.material.name, im.material.category, SUM(im.quantity) " +
           "FROM FixVisit fv JOIN fv.installedMaterials im " +
           "GROUP BY im.material.name, im.material.category " +
           "ORDER BY SUM(im.quantity) DESC")
    List<Object[]> sumInstalledQuantityByMaterial();

    @Query("SELECT im.material.name, im.material.category, im.material.priceEuros, SUM(im.quantity) " +
           "FROM FixVisit fv JOIN fv.installedMaterials im " +
           "GROUP BY im.material.name, im.material.category, im.material.priceEuros " +
           "ORDER BY im.material.name")
    List<Object[]> getMaterialCostSummary();
}
