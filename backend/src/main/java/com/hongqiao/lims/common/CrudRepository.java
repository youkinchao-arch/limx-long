package com.hongqiao.lims.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/** Base repository giving every ledger entity paging + dynamic search support. */
@NoRepositoryBean
public interface CrudRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
