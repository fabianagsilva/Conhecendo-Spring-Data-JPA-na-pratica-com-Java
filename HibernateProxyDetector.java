package org.springframework.data.jpa.util;

import java.util.Optional;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.util.ProxyUtils.ProxyDetector;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

class HibernateProxyDetector implements ProxyDetector {

	private static final Optional<Class<?>> HIBERNATE_PROXY = Optional.ofNullable(loadHibernateProxyType());

	@Override
	public Class<?> getUserType(Class<?> type) {

		return HIBERNATE_PROXY //
				.map(it -> it.isAssignableFrom(type) ? type.getSuperclass() : type) //
				.filter(it -> !Object.class.equals(it)) //
				.orElse(type);
	}

	@Nullable
	private static Class<?> loadHibernateProxyType() {

		try {
			return ClassUtils.forName("org.hibernate.proxy.HibernateProxy", HibernateProxyDetector.class.getClassLoader());
		} catch (ClassNotFoundException o_O) {
			return null;
		}
	}
}
