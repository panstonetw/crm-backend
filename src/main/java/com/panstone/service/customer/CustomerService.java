package com.panstone.service.customer;

import com.panstone.domain.entity.customer.Customer;
import com.panstone.repository.customer.CustomerRepository;
import com.panstone.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService extends GenericService<Customer, Integer> {

	@Autowired
	public CustomerService(CustomerRepository repository) {
		super(repository);
	}

}
