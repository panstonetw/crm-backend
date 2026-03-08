package com.panstone.domain.dto.customer;

import com.panstone.domain.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Dto for {@link com.panstone.domain.entity.customer.Customer}
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class CustomerDto extends BaseDto {

	private Integer id;

	@Size(max = 100)
	@NotNull
	private String name;

	@Size(max = 10)
	private String taxId;

	@Size(max = 100)
	private String owner;

	@Size(max = 20)
	private String phone;

	@Size(max = 100)
	private String contactPerson;

	@Size(max = 255)
	private String address;

	@Size(max = 255)
	private String email;

}
