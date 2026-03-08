package com.panstone.service;

import com.panstone.domain.dto.BaseDto;
import com.panstone.domain.entity.BaseEntity;
import com.panstone.domain.exception.*;
import com.panstone.domain.mapper.BaseMapper;
import com.panstone.repository.GenericRepository;
import com.panstone.util.ApplicationContextProvider;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
public abstract class GenericService<E extends BaseEntity<PK>, PK extends Serializable> implements IGenericService<E, PK> {
//	private final Class<D> dtoClass;
    private final Class<E> entityClass;
	private final Class<PK> pkClass;
    protected final GenericRepository<E, PK> repository;
//	protected final BaseMapper<D, E, PK> mapper;
	@Resource
	private MessageSource messageSource;

	@SuppressWarnings("unchecked")
    public GenericService(GenericRepository<E, PK> genericRepository) {
//		this.dtoClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    	this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.pkClass = (Class<PK>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.repository = genericRepository;
    }

	@Override
	public List<E> findAll() {
		return new ArrayList<>(repository.findAll());
	}

	@Override
	public List<E> findAll(Sort sort) {
		return new ArrayList<>(repository.findAll(sort));
	}

	@Override
	public Page<E> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public List<E> findAllById(Iterable<PK> ids) {
		return new ArrayList<>(repository.findAllById(ids));
	}

	@Override
	public <D extends BaseDto> E create(D dto) {
		BaseMapper<D, E, PK> mapper = getMapper(dto);
		return save(mapper.toEntity(dto));
	}

	@Override
	public <D extends BaseDto> E update(D dto) {
		BaseMapper<D, E, PK> mapper = getMapper(dto);
		E entity = mapper.toEntity(dto);
		final PK id = entity.getId();
		if (id != null) {
			entity = repository.findById(id).orElse(null);
			if (entity != null) {
				checkBeforeUpdate(entity);
				return save(mapper.partialUpdate(dto, entity));
			} else {
				throw new EntityNotFoundException(
					"errors.edit.notFound",
					MessageFormat.format("Entity《{0}》 with id {1} does not exist !", entityClass.getSimpleName(), id)
				);
			}
		} else {
			throw new EntityNotFoundException(
				"errors.edit.notFound",
				MessageFormat.format("Entity《{0}》 with id {1} does not exist !", entityClass.getSimpleName(), "null")
			);
		}
	}

	protected <S extends E> S save(S entity) {
		for (Object uniqueField : getUniqueFields()) {
			if (uniqueField instanceof String) {
				duplicateCheck(entity, (String) uniqueField);
			} else if (uniqueField instanceof String[]) {
				duplicateCheck(entity, (String[]) uniqueField);
			}
		}
		return repository.save(entity);
	}

	protected void checkBeforeUpdate(E entity) {
		boolean editable = true;
		try {
			editable = (Boolean) MethodUtils.invokeMethod(entity, "isEditable");
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		if (!editable) {
			String distinguishedName = getDistinguishedName(entity);
			Field voidedField = FieldUtils.getField(entityClass, "voided", true);
			if (voidedField != null) {
				Boolean voided = false;
				try {
					//voided = (Short) FieldUtils.readField(entity, "voided", true);
					voided = (Boolean) MethodUtils.invokeMethod(entity, "getVoided");
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
				if (voided) {
					throw new EntityModifyException(distinguishedName,
						"errors.edit.voided",
						MessageFormat.format("Cannot update an entity《{0}[{1}]》that is voided !",
						entityClass.getSimpleName(), distinguishedName)
					);
				} else {
					throw new EntityModifyException(distinguishedName,
						"errors.edit.notAllowed",
						MessageFormat.format("Update an entity《{0}[{1}]》is not allowed !",
						entityClass.getSimpleName(), distinguishedName)
					);
				}
			} else {
				throw new EntityModifyException(distinguishedName,
					"errors.edit.notAllowed",
					MessageFormat.format("Update an entity《{0}[{1}]》is not allowed !",
					entityClass.getSimpleName(), distinguishedName)
				);
			}
		}
	}

	@Override
	public boolean isDuplicate(E entity, String fieldName) {
		return isDuplicate(entity, new String[] { fieldName });
	}

	@Override
	public boolean isDuplicate(E entity, String[] fieldNames) {
		try {
			Map<String, Object> checkFields = new LinkedHashMap<>();
			for (String fieldName : fieldNames) {
				Object checkValue;
				String[] keys = fieldName.split("\\.");
				if (keys.length > 1) {
					checkValue = entity;
					for (String key : keys) {
						//checkValue = FieldUtils.readField(checkValue, key, true);
						checkValue = MethodUtils.invokeMethod(checkValue, "get" + StringUtils.capitalize(key));
					}
				} else {
					//checkValue = FieldUtils.readField(entity, fieldName, true);
					checkValue = MethodUtils.invokeMethod(entity, "get" + StringUtils.capitalize(fieldName));
				}
				checkFields.put(fieldName, checkValue);
			}
			Field voidedField = FieldUtils.getField(entityClass, "voided", true);
			List<E> existed = repository.findAll((root, query, cb) -> {
				List<Predicate> predicates = new ArrayList<>();
				for (Map.Entry<String, Object> entry : checkFields.entrySet()) {
					String fieldName = entry.getKey();
					Object checkValue = entry.getValue();
					String[] keys = fieldName.split("\\.");
					Path<Object> path = root.get(keys[0]);
					int i = 1;
					while (i < keys.length) {
						path = path.get(keys[i]);
						i++;
					}
					predicates.add(cb.equal(path, checkValue));
				}
				if (voidedField != null) {
					predicates.add(cb.equal(root.get("voided"), false));
				}
				return cb.and(predicates.toArray(new Predicate[0]));
			});
			for (E e : existed) {
				//if (!FieldUtils.readField(t, "id", true).equals(FieldUtils.readField(entity, "id", true))) {
				if (!MethodUtils.invokeMethod(e, "getId").equals(MethodUtils.invokeMethod(entity, "getId"))) {
					return true;
				}
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		return false;
	}

	protected void duplicateCheck(E entity, String fieldName) {
		duplicateCheck(entity, new String[] { fieldName });
	}

	protected void duplicateCheck(E entity, String[] fieldNames) {
		if (isDuplicate(entity, fieldNames)) {
			String fieldName = fieldNames[0];
			Object fieldValue = null;
			try {
				//fieldValue = FieldUtils.readField(entity, fieldName, true);
				fieldValue = MethodUtils.invokeMethod(entity, "get" + StringUtils.capitalize(fieldName));
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
			throw new EntityDuplicateException(fieldName, fieldValue,
				"errors.save.duplicate",
				MessageFormat.format("Duplicate Entity《{0}[{1}]》is not allowed !",
				entityClass.getSimpleName(), fieldValue)
			);
		}
	}

	protected Object[] getUniqueFields() {
		return new Object[0];
	}

	@Override
	public Optional<E> findById(PK id) {
		return repository.findById(id);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public boolean existsById(PK id) {
		return repository.existsById(id);
	}

	@Override
	public void deleteById(PK id) {
		E entity = repository.findById(id).orElse(null);
		if (entity != null) {
			checkBeforeDelete(entity);
			repository.deleteById(id);
		}
	}

	@Override
	public <D extends BaseDto> void delete(D dto) {
		BaseMapper<D, E, PK> mapper = getMapper(dto);
		deleteById(mapper.toEntity(dto).getId());
	}

	@Override
	public void deleteAllById(Iterable<PK> ids) {
		for (PK id : ids) {
			deleteById(id);
		}
	}

	protected void checkBeforeDelete(E entity) {
		boolean deletable = true;
		try {
			deletable = (Boolean) MethodUtils.invokeMethod(entity, "isDeletable");
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		if (!deletable) {
			String distinguishedName = getDistinguishedName(entity);
			throw new EntityDeleteException(distinguishedName,
				"errors.delete.constraintViolation",
				MessageFormat.format("Cannot delete an entity《{0}[{1}]》that is referenced by other entities !",
				entityClass.getSimpleName(), distinguishedName)
			);
		}
	}

	@Override
	public void invalidateById(PK id) {
		Field voidedField = FieldUtils.getField(entityClass, "voided", true);
		if (voidedField != null) {
			E entity = repository.findById(id).orElse(null);
			if (entity != null) {
				checkBeforeInvalidate(entity);
				try {
					FieldUtils.writeField(entity, "voided", true, true);
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
				repository.save(entity);
			} else {
				throw new EntityNotFoundException(
					"errors.void.notFound",
					MessageFormat.format("Entity《{0}》 with id {1} does not exist !", entityClass.getSimpleName(), id)
				);
			}
		} else {
			throw new EntityVoidException("",
					"errors.void.fieldNotExist",
					MessageFormat.format("Cannot void an entity《{0}》that 'voided' field does not exist !",
							entityClass.getSimpleName())
			);
		}
	}

	@Override
	public <D extends BaseDto> void invalidate(D dto) {
		BaseMapper<D, E, PK> mapper = getMapper(dto);
		invalidateById(mapper.toEntity(dto).getId());
	}

	@Override
	public void invalidateAllById(Iterable<PK> ids) {
		for (PK id : ids) {
			invalidateById(id);
		}
	}

	protected void checkBeforeInvalidate(E entity) {
		boolean voidable = true;
		try {
			voidable = (Boolean) MethodUtils.invokeMethod(entity, "isVoidable");
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		if (!voidable) {
			String distinguishedName = getDistinguishedName(entity);
			Boolean voided = false;
			try {
				//voided = (Short) FieldUtils.readField(entity, "voided", true);
				voided = (Boolean) MethodUtils.invokeMethod(entity, "getVoided");
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
			if (voided) {
				throw new EntityVoidException(distinguishedName,
					"errors.void.voided",
					MessageFormat.format("Cannot void an entity《{0}[{1}]》that is voided !",
					entityClass.getSimpleName(), distinguishedName)
				);
			} else {
				throw new EntityVoidException(distinguishedName,
					"errors.void.constraintViolation",
					MessageFormat.format("Cannot void an entity《{0}[{1}]》that is referenced by other entities !",
					entityClass.getSimpleName(), distinguishedName)
				);
			}
		}
	}

	@Override
	public Optional<E> findOne(Specification<E> spec) {
		return repository.findOne(spec);
	}
	@Override
	public List<E> findAll(Specification<E> spec) {
		return repository.findAll(spec);
	}

	@Override
	public Page<E> findAll(Specification<E> spec, Pageable pageable) {
		return repository.findAll(spec, pageable);
	}

	@Override
	public List<E> findAll(Specification<E> spec, Sort sort) {
		return repository.findAll(spec, sort);
	}

	@Override
	public long count(Specification<E> spec) {
		return repository.count(spec);
	}

	protected String getDistinguishedName(E entity) {
		String distinguishedName = entity.toString();
		try {
			distinguishedName = (String) MethodUtils.invokeMethod(entity, "getDistinguishedName");
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		return distinguishedName;
	}

	@SuppressWarnings("unchecked")
	protected <D extends BaseDto, M extends BaseMapper<D, E, PK>> M getMapper(D dto) {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		ResolvableType type = ResolvableType.forClassWithGenerics(BaseMapper.class, dto.getClass(), entityClass, pkClass);
		return (M) context.getBeanProvider(type).getObject();
	}

}
