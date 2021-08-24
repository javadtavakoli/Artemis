package org.devocative.artemis;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Proxy {
	private final Object target;

	// ------------------------------

	private Proxy(Object target) {
		this.target = target;
	}

	// ------------------------------

	public Object get() {
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());
		enhancer.setCallback((MethodInterceptor) (obj, method, vars, proxy) -> {
			final Object result = proxy.invoke(target, vars);

			log.debug("{}.{} -> {}", target.getClass().getSimpleName(), method.getName(), result);

			if (result != null) {
				if (result instanceof List) {
					return ((List<?>) result).stream()
						.filter(Objects::nonNull)
						.map(item -> doProxify(item) ? create(item) : item)
						.collect(Collectors.toList());
				} else if (result instanceof String) {
					final String str = (String) result;
					return str.contains("${") ? eval(str, method) : str;
				} else if (doProxify(result)) {
					return create(result);
				}
				return result;
			} else if (method.getReturnType().equals(List.class)) {
				return Collections.emptyList();
			}
			return null;
		});
		return enhancer.create();
	}

	// ------------------------------

	public static <T> T create(T obj) {
		return (T) new Proxy(obj).get();
	}

	// ------------------------------

	private boolean doProxify(Object obj) {
		return obj.getClass().isAnnotationPresent(XStreamAlias.class);
	}

	private Object eval(String str, Method method) {
		try {
			return ContextHandler.eval(str);
		} catch (MissingPropertyException | MissingMethodException e) {
			final String missing;
			if (e instanceof MissingPropertyException) {
				missing = ((MissingPropertyException) e).getProperty();
			} else {
				missing = ((MissingMethodException) e).getMethod() + "()";
			}

			throw new RuntimeException(
				String.format("Invalid expression '%s' inside '%s' from '%s' attribute of '%s' (MSG = %s)",
					missing, str, extractName(method), target, e.getMessage()), e);
		} catch (GroovyRuntimeException e) {
			throw new RuntimeException(
				String.format("Invalid groovy '%s' from '%s' attribute in '%s' (MSG = %s)",
					str, extractName(method), target, e.getMessage()), e);
		}
	}

	private String extractName(Method method) {
		final String methodName = method.getName();
		return methodName.startsWith("get") ?
			methodName.substring(3, 4).toLowerCase() + methodName.substring(4) :
			methodName;
	}
}
