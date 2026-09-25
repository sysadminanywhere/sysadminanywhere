package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.common.inventory.model.HardwareChangeItem;
import com.sysadminanywhere.inventory.entity.HardwareChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HardwareChangeRepository extends JpaRepository<HardwareChange, Long> {
    boolean existsByComputerId(Long computerId);

    @Query("select new com.sysadminanywhere.common.inventory.model.HardwareChangeItem(h.id, c.id, c.name, hm.hardwareType, hm.name, h.changeType, h.propertyName, h.oldValue, h.newValue, h.changedAt) "
            + "from HardwareChange h join h.computer c left join h.hardwareModel hm "
            + "where (:computerId is null or c.id = :computerId) and (:modelId is null or hm.id = :modelId) "
            + "order by h.changedAt desc, h.id desc")
    Page<HardwareChangeItem> findHistory(@Param("computerId") Long computerId,
                                         @Param("modelId") Long modelId, Pageable pageable);
}
