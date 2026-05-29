package com.hongqiao.lims.personnel;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "personnel")
public class Personnel extends BaseEntity {

    @Column(name = "employee_no", nullable = false, unique = true, length = 64)
    private String employeeNo;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 16)
    private String gender;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(length = 64)
    private String position;

    @Column(length = 32)
    private String phone;

    @Column(length = 128)
    private String email;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(length = 255)
    private String qualification;

    @Column(name = "qualification_expiry")
    private LocalDate qualificationExpiry;

    @Column(nullable = false, length = 32)
    private String status = "active";

    @Column(columnDefinition = "text")
    private String remark;
}
