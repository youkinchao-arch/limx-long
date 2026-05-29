package com.hongqiao.lims.personnel;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 64)
    private String code;

    @Column(name = "parent_id")
    private Long parentId;
}
