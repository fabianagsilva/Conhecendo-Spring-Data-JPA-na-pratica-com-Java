package org.springframework.data.jpa.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.util.Lazy;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;

public class JpaMetamodel {

	private static final Map<Metamodel, JpaMetamodel> CACHE = new ConcurrentHashMap<>(4);

	private final Metamodel metamodel;

	private Lazy<Collection<Class<?>>> managedTypes;

	private JpaMetamodel(Metamodel metamodel) {

		Assert.notNull(metamodel, "Metamodel must not be null!");

		this.metamodel = metamodel;
		this.managedTypes = Lazy.of(() -> metamodel.getManagedTypes().stream() //
				.map(ManagedType::getJavaType) //
				.filter(it -> it != null) //
				.collect(StreamUtils.toUnmodifiableSet()));
	}

	public static JpaMetamodel of(Metamodel metamodel) {
		return CACHE.computeIfAbsent(metamodel, JpaMetamodel::new);
	}

	public boolean isJpaManaged(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		return managedTypes.get().contains(type);
	}

	public boolean isSingleIdAttribute(Class<?> entity, String name, Class<?> attributeType) {

		return metamodel.getEntities().stream() //
				.filter(it -> entity.equals(it.getJavaType())) //
				.findFirst() //
				.flatMap(it -> getSingularIdAttribute(it)) //
				.filter(it -> it.getJavaType().equals(attributeType)) //
				.map(it -> it.getName().equals(name)) //
				.orElse(false);
	}

	static void clear() {
		CACHE.clear();
	}

	private static Optional<? extends SingularAttribute<?, ?>> getSingularIdAttribute(EntityType<?> entityType) {

		if (!entityType.hasSingleIdAttribute()) {
			return Optional.empty();
		}

		return entityType.getSingularAttributes().stream() //
				.filter(SingularAttribute::isId) //
				.findFirst();
	}
}
