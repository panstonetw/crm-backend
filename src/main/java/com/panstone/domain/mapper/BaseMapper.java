package com.panstone.domain.mapper;


import com.panstone.domain.dto.BaseDto;
import com.panstone.domain.entity.BaseEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.io.Serializable;
import java.util.List;

public interface BaseMapper<D extends BaseDto, E extends BaseEntity<PK>, PK extends Serializable> {

    E toEntity(D dto);
    D toDto(E entity);
    List<E> toEntity(List<D> dtoList);
    List<D> toDto(List<E> entityList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    E partialUpdate(D dto, @MappingTarget E entity);

}
