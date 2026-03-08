package com.panstone.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class BaseEntity<PK extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private PK id;

    @Transient
    public boolean isEditable() {
        return true;
    }

    @Transient
    public boolean isDeletable() {
        return true;
    }

    @Transient
    public boolean isVoidable() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!this.getClass().isInstance(o)) {
            return false;
        }
        BaseEntity<?> that = (BaseEntity<?>) o;
        return getId() != null && getId().equals(that.getId());
    }

}
