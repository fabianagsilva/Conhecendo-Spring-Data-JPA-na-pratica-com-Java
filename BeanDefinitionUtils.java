package org.springframework.data.jpa.util;

import static java.util.Arrays.*;
import static org.springframework.beans.factory.BeanFactoryUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public final class BeanDefinitionUtils {

	private static final String JNDI_OBJECT_FACTORY_BEAN = "org.springframework.jndi.JndiObjectFactoryBean";
	private static final List<Class<?>> EMF_TYPES;

	private BeanDefinitionUtils() {}

	static {

		List<Class<?>> types = new ArrayList<Class<?>>();
		types.add(EntityManagerFactory.class);
		types.add(AbstractEntityManagerFactoryBean.class);

		if (ClassUtils.isPresent(JNDI_OBJECT_FACTORY_BEAN, ClassUtils.getDefaultClassLoader())) {
			types.add(JndiObjectFactoryBean.class);
		}

		EMF_TYPES = Collections.unmodifiableList(types);
	}

	public static Iterable<String> getEntityManagerFactoryBeanNames(ListableBeanFactory beanFactory) {

		String[] beanNames = beanNamesForTypeIncludingAncestors(beanFactory, EntityManagerFactory.class, true, false);
		Set<String> names = new HashSet<>(asList(beanNames));

		for (String factoryBeanName : beanNamesForTypeIncludingAncestors(beanFactory,
				AbstractEntityManagerFactoryBean.class, true, false)) {
			names.add(transformedBeanName(factoryBeanName));
		}

		return names;
	}

	public static Collection<EntityManagerFactoryBeanDefinition> getEntityManagerFactoryBeanDefinitions(
			ConfigurableListableBeanFactory beanFactory) {

		Set<EntityManagerFactoryBeanDefinition> definitions = new HashSet<EntityManagerFactoryBeanDefinition>();

		for (Class<?> type : EMF_TYPES) {

			for (String name : beanFactory.getBeanNamesForType(type, true, false)) {
				registerEntityManagerFactoryBeanDefinition(transformedBeanName(name), beanFactory, definitions);
			}
		}

		BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();

		if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
			definitions.addAll(getEntityManagerFactoryBeanDefinitions((ConfigurableListableBeanFactory) parentBeanFactory));
		}

		return definitions;
	}

	private static void registerEntityManagerFactoryBeanDefinition(String name,
			ConfigurableListableBeanFactory beanFactory, Collection<EntityManagerFactoryBeanDefinition> definitions) {

		BeanDefinition definition = beanFactory.getBeanDefinition(name);

		if (JNDI_OBJECT_FACTORY_BEAN.equals(definition.getBeanClassName())) {
			if (!EntityManagerFactory.class.getName().equals(definition.getPropertyValues().get("expectedType"))) {
				return;
			}
		}

		Class<?> type = beanFactory.getType(name);
		if (type == null || !EntityManagerFactory.class.isAssignableFrom(type)) {
			return;
		}

		definitions.add(new EntityManagerFactoryBeanDefinition(name, beanFactory));
	}

	public static BeanDefinition getBeanDefinition(String name, ConfigurableListableBeanFactory beanFactory) {

		try {
			return beanFactory.getBeanDefinition(name);
		} catch (NoSuchBeanDefinitionException o_O) {

			BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();

			if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
				return getBeanDefinition(name, (ConfigurableListableBeanFactory) parentBeanFactory);
			}

			throw o_O;
		}
	}

	public static class EntityManagerFactoryBeanDefinition {

		private final String beanName;
		private final ConfigurableListableBeanFactory beanFactory;

		public EntityManagerFactoryBeanDefinition(String beanName, ConfigurableListableBeanFactory beanFactory) {

			this.beanName = beanName;
			this.beanFactory = beanFactory;
		}

		public String getBeanName() {
			return beanName;
		}

		public BeanFactory getBeanFactory() {
			return beanFactory;
		}

		public BeanDefinition getBeanDefinition() {
			return beanFactory.getBeanDefinition(beanName);
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}

			if (!(o instanceof EntityManagerFactoryBeanDefinition)) {
				return false;
			}

			EntityManagerFactoryBeanDefinition that = (EntityManagerFactoryBeanDefinition) o;

			if (!ObjectUtils.nullSafeEquals(beanName, that.beanName)) {
				return false;
			}

			return ObjectUtils.nullSafeEquals(beanFactory, that.beanFactory);
		}

		@Override
		public int hashCode() {
			int result = ObjectUtils.nullSafeHashCode(beanName);
			result = 31 * result + ObjectUtils.nullSafeHashCode(beanFactory);
			return result;
		}
	}
}
