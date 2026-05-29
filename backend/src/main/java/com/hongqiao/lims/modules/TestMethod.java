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
@Table(name = "test_methods")
public class TestMethod extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String standard;

    @Column(length = 64)
    private String category;

    @Column(name = "limit_value", length = 255)
    private String limitValue;

    @Column(nullable = false, length = 32)
    private String status = "draft";

    @Column(columnDefinition = "text")
    private String remark;
}
