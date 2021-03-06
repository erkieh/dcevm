package com.github.dcevm.test.methods;

import static com.github.dcevm.test.util.HotSwapTestHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests accessing added method on a proxy.
 * 
 * @author Erki Ehtla
 */
public class AddMethodProxyTest {
	
	static public class DummyHandler implements InvocationHandler {
		private Object a;
		
		public DummyHandler(Object a) {
			this.a = a;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(a, args);
		}
	}
	
	// Version 0
	public static class AImpl implements A {
		public int getValue1() {
			return 1;
		}
	}
	
	// Version 1
	public static class AImpl___1 implements A___1 {
		public int getValue2() {
			return 2;
		}
	}
	
	// Version 0
	public interface A {
		public int getValue1();
	}
	
	// Version 1
	public interface A___1 {
		public int getValue2();
	}
	
	@Before
	public void setUp() throws Exception {
		__toVersion__(0);
	}
	
	@Test
	public void addMethodToInterfaceAndImplementation() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		
		assert __version__() == 0;
		
		final A a = new AImpl();
		
		assertEquals(1, a.getValue1());
		
		__toVersion__(1);
		
		Method method = getMethod(a, "getValue2");
		assertEquals(2, method.invoke(a, null));
	}
	
	@Test
	public void accessNewMethodOnProxy() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		
		assert __version__() == 0;
		
		final A a = (A) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { A.class }, new DummyHandler(
				new AImpl()));
		
		assertEquals(1, a.getValue1());
		
		__toVersion__(1);
		
		Method method = getMethod(a, "getValue2");
		assertEquals(2, method.invoke(a, null));
	}
	
	@Test
	public void accessNewMethodOnProxyCreatedAfterSwap() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		
		assert __version__() == 0;
		A a = (A) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { A.class }, new DummyHandler(
				new AImpl()));
		
		assertEquals(1, a.getValue1());
		__toVersion__(1);
		
		a = (A) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { A.class }, new DummyHandler(
				new AImpl()));
		
		Method method = getMethod(a, "getValue2");
		assertEquals(2, method.invoke(a, null));
	}
	
	private Method getMethod(Object a, String methodName) {
		Method[] declaredMethods = a.getClass().getDeclaredMethods();
		Method m = null;
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName))
				m = method;
		}
		if (m == null) {
			fail(a.getClass().getSimpleName() + " does not have method " + methodName);
		}
		return m;
	}
	
}
