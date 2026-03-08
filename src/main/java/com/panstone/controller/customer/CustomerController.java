package com.panstone.controller.customer;

import com.panstone.controller.BaseController;
import com.panstone.domain.dto.customer.CustomerDto;
import com.panstone.domain.entity.customer.Customer;
import com.panstone.domain.exception.EntityNotFoundException;
import com.panstone.domain.mapper.customer.CustomerMapper;
import com.panstone.exception.DeleteEntityException;
import com.panstone.service.customer.CustomerService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class CustomerController extends BaseController {

    private final CustomerService customerService;
	private final CustomerMapper customerMapper;

    @ModelAttribute("customerDto")
    public CustomerDto getCustomerDto(@PathVariable("id") Optional<Integer> id, Model model) {
		Customer customer;
	    if (id.isPresent()) {
		    customer = customerService.findById(id.get()).orElse(null);
	    } else {
			customer = new Customer();
	    }
		model.addAttribute("customer", customer);
	    if (customer != null) {
		    return customerMapper.toDto(customer);
	    }
        return new CustomerDto();
    }

    @GetMapping
    public Page<Customer> list(@RequestParam Optional<String> name, @RequestParam Optional<String> taxId,
                               @RequestParam Optional<String> owner, @RequestParam Optional<String> phone,
                               @RequestParam Optional<String> contactPerson, @RequestParam Optional<String> email,
                               @RequestParam Optional<String> address, Pageable pageable) {
	    Specification<Customer> specification = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			name.ifPresent(n -> predicates.add(cb.like(root.get("name"), "%" + n + "%")));
		    taxId.ifPresent(id -> predicates.add(cb.like(root.get("taxId"), "%" + id + "%")));
		    owner.ifPresent(o -> predicates.add(cb.like(root.get("owner"), "%" + o + "%")));
		    phone.ifPresent(p -> predicates.add(cb.like(root.get("phone"), "%" + p + "%")));
		    contactPerson.ifPresent(cp -> predicates.add(cb.like(root.get("contactPerson"), "%" + cp + "%")));
		    email.ifPresent(e -> predicates.add(cb.like(root.get("email"), "%" + e + "%")));
		    address.ifPresent(a -> predicates.add(cb.like(root.get("address"), "%" + a + "%")));
			return cb.and(predicates.toArray(predicates.toArray(new Predicate[0])));
	    };
        return customerService.findAll(specification, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getCustomerById(@PathVariable("id") Integer id) {
	    Customer customer = customerService.findById(id).orElse(null);
	    if (customer == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getText("errors.search.notFound", getText("customer")));
	    }
	    return ResponseEntity.ok(customer);
    }

    @RequestMapping(value = { "/new", "/{id:\\d+}"}, method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<String> save(@PathVariable("id") Optional<Integer> id, @RequestBody CustomerDto customerDto) {
        Customer customer;
        try {
	        if (id.isEmpty()) {
		        customer = customerService.create(customerDto);
	        } else {
                customer = customerService.update(customerDto);
            }
        } catch (EntityNotFoundException e) {
            log.error(e.toString(), e);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        if (id.isEmpty()) {
            return ResponseEntity.ok(getText("messages.added", getText("customer"), customer.getDistinguishedName()));
        } else {
            return ResponseEntity.ok(getText("messages.updated", getText("customer"), customer.getDistinguishedName()));
        }
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteCustomerById(Model model) {
	    Customer customer = (Customer) model.getAttribute("customer");
	    if (customer != null) {
		    if (customer.isDeletable()) {
                try {
                    customerService.deleteById(customer.getId());
                    return ResponseEntity.ok(getText("messages.deleted", getText("customer"), customer.getDistinguishedName()));
                } catch (DeleteEntityException e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(getText(e.getErrorCode(), getText("customer"), customer.getDistinguishedName()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(getText("errors.delete.notAllowed", getText("customer"), customer.getDistinguishedName()));
            }
	    }
        return ResponseEntity.badRequest().build();
    }

}
