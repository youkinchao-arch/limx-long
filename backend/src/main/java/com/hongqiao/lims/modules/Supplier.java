package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 128)
    private String contact;

    @Column(length = 32)
    private String phone;

    @Column(columnDefinition = "text")
    private String remark;
}
