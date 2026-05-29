package com.hongqiao.lims.personnel;

import com.hongqiao.lims.common.CrudRepository;

public interface PersonnelRepository extends CrudRepository<Personnel> {

    boolean existsByEmployeeNo(String employeeNo);
}
