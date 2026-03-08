package com.panstone.service;

import com.panstone.domain.dto.BaseDto;
import com.panstone.domain.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface IGenericService<E extends BaseEntity<PK>, PK extends Serializable> {

	List<E> findAll();

	List<E> findAll(Sort sort);
	
	Page<E> findAll(Pageable pageable);

	List<E> findAllById(Iterable<PK> ids);

	Optional<E> findById(PK id);

	long count();

	boolean existsById(PK id);

	<D extends BaseDto> E create(D dto);

	<D extends BaseDto> E update(D dto);

	boolean isDuplicate(E entity, String fieldName);

	boolean isDuplicate(E entity, String[] fieldNames) ;

	void deleteById(PK id);

	<D extends BaseDto> void delete(D dto);

	void deleteAllById(Iterable<PK> ids);

	void invalidateById(PK id);

	<D extends BaseDto> void invalidate(D dto);

	void invalidateAllById(Iterable<PK> ids);

	Optional<E> findOne(Specification<E> spec);

	List<E> findAll(Specification<E> spec);

	Page<E> findAll(Specification<E> spec, Pageable pageable);

	List<E> findAll(Specification<E> spec, Sort sort);

	long count(Specification<E> spec);

}
