package org.springframework.data.jpa.util;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

class JpaMetamodelCacheCleanup implements DisposableBean {

	@Override
	public void destroy() throws Exception {
		JpaMetamodel.clear();
	}
}
