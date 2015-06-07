package com.caijin.I000Wan.common.dao;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
/**
 * 基础dao
 * @author jeff
 * @since 2014-07-21
 * @param <E>
 * @param <ID>
 */
@NoRepositoryBean
public interface CommonDao<E,ID extends Serializable>  extends JpaRepository<E,ID>, JpaSpecificationExecutor<E>{

}
