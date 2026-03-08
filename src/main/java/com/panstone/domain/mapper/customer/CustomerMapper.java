package com.panstone.domain.mapper.customer;

import com.panstone.domain.dto.customer.CustomerDto;
import com.panstone.domain.entity.customer.Customer;
import com.panstone.domain.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper extends BaseMapper<CustomerDto, Customer, Integer> {
}
