package postest2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import jpos.JposException;

public class DeviceProperties {

	public static String getProperties(Object object, IMapWrapper objectMap) throws JposException {

		String properties = "";

		try {
			Method[] methods = Class.forName(object.getClass().getName()).getMethods();

			for (Method method : methods) {
				if (isGetter(method)) {
					String methodName = method.getName();
					try {
						if (method.getReturnType().equals(Integer.TYPE)) {
							if (objectMap != null) {
								ArrayList<String> al = BelongingPropertyChecker.invokeThis(objectMap, methodName);
								if (!al.isEmpty()) {
									Iterator<String> iterator = al.iterator();
									int rightValue = (Integer) method.invoke(object, null);
									properties += method.getName().substring(3) + ": ";
									while (iterator.hasNext()) {
										String value = iterator.next().toString();
										int temp = (Integer) Class.forName(objectMap.getClass().getName())
												.getMethod("getConstantNumberFromString", String.class)
												.invoke(objectMap, value);
										if (rightValue == temp)
											properties += value;
									}
									properties += "\n";
								} else {
									properties += method.getName().substring(3) + ": " + (Integer) method.invoke(object, null);
									properties += "\n";
								}
							}
						} else {
							properties += method.getName().substring(3) + ": " + method.invoke(object);
							properties += "\n";
						} // end if return type
					} catch (InvocationTargetException e) {
						if (e.getTargetException() instanceof JposException) {
							properties += method.getName().substring(3) + ": [No access]\n";
						} else {
							throw e;
						}
					}
				} // end if is getter
			} // end for

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return properties;
	}

	public static boolean isGetter(Method method) {
		if (!method.getName().startsWith("get"))
			return false;
		if (method.getParameterTypes().length != 0)
			return false;
		if (void.class.equals(method.getReturnType()))
			return false;
		return true;
	}

}
