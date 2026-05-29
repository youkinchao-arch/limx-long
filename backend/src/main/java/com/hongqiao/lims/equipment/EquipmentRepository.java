package com.hongqiao.lims.equipment;

import com.hongqiao.lims.common.CrudRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface EquipmentRepository extends CrudRepository<Equipment> {

    boolean existsByAssetNo(String assetNo);

    @Query("select e.status, count(e) from Equipment e group by e.status")
    List<Object[]> countByStatus();
}
