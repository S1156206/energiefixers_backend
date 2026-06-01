package com.energiefixers.backend.visit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energiefixers.backend.visit.models.FixVisit;

import java.math.BigDecimal;
import java.util.List;

public interface FixVisitRepository extends JpaRepository<FixVisit, Long> {
    List<FixVisit> findAllByPropertyId(Long propertyId);

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
}
