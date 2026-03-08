package com.panstone.domain.entity.customer;

import com.panstone.domain.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer extends BaseAuditableEntity<Integer> {

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 10)
    @Column(name = "tax_id", length = 10)
    private String taxId;

    @Size(max = 100)
    @Column(name = "owner", length = 100)
    private String owner;

    @Size(max = 100)
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Override
    public boolean isDeletable() {
        return true;
    }

    @Transient
    public String getDistinguishedName() {
        return StringUtils.stripToEmpty(getName());
    }

}
