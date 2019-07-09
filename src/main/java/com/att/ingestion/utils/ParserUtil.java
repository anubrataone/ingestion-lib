package com.att.ingestion.utils;

import com.google.common.base.CharMatcher;
import com.att.ingestion.service.ResourceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParserUtil {


    private static Logger logger = LoggerFactory.getLogger(ParserUtil.class);

	/**
	 * Receive a time in format HH:MM:SS (sample 02:12:00) and returns the
	 * number of seconds
	 * 
	 * @param time
	 * @throws ParseException 
	 */
	public static Long timeToSeconds(String time) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = dateFormat.parse(time);
		return date.getTime()/1000;
	}
	
	public static Long yearToTime(String year) throws ParseException {
		return new SimpleDateFormat("yyyy").parse(year).getTime();	
	}

	/**
	 * Return system time in milliseconds
	 */
	public static Long getSystemTime(String par) {
		return System.currentTimeMillis();
	}

	public static Long mpDateToLong(String date) throws ParseException {
		//Sample: Jul 3, 2012 2:00:00 AM
		return new SimpleDateFormat("MMM d, yyyy hh:mm:ss a").parse(date).getTime();
	}
	
	public static Long dateToTime(String date) throws ParseException {
		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date).getTime();	
		} catch (Exception e) {
			return 0l;
		}
	}
	
	public static String toLowerCase(String string) {
		if (string == null) {
			return null;
		}
		return string.toLowerCase();
	}

	public static String toUpperCase(String string) {
		if (string == null) {
			return null;
		}
		return string.toUpperCase();
	}

	public static Long stringToNumber(String string) throws ParseException {
		return (long)Float.parseFloat(string);
	}

	public static String removeScientificFromNumber(String number) throws ParseException {
		if (number == null || number.isEmpty()) {
			return "";
		}
		return String.valueOf(Double.valueOf(number).longValue());
	}

	public static Float stringToFloat(String string) throws ParseException {
		if (StringUtils.isNotBlank(string)) {
			return Float.parseFloat(string);	
		}
		return null;
	}
	
	public static boolean toBoolean(String flag) throws ParseException {
		if (flag == null || flag.isEmpty()) {
			return false;
		}
		return (flag.equalsIgnoreCase("true") || flag.equalsIgnoreCase("t") || flag.equalsIgnoreCase("yes") || flag.equalsIgnoreCase("y"));
	}


	public static Object execute(String methodName, String param, ResourceLoader resourceLoader) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.debug(String.format("Going to execute method %s", methodName));
		if (methodName.indexOf(".") > 0) {
			try {
				String className = methodName.substring(0, methodName.lastIndexOf("."));
				String methodNameUpdated = methodName.substring(methodName.lastIndexOf(".") + 1);
				logger.debug(String.format("Going to execute class %s method %s with param %s", className, methodNameUpdated, param));
				Object r = execute(Class.forName(className), methodNameUpdated, param, resourceLoader);
				logger.debug(String.format("Result %s", r));
				return r;
			} catch (ClassNotFoundException e) {
				logger.error(String.format("Class not found for converter %s", methodName));
				return null;
			}
		}
		Object r = execute(ParserUtil.class, methodName, param, resourceLoader);
		logger.debug(String.format("Result %s", r));
		return r;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object execute(Class class1, String methodName, String param, ResourceLoader resourceLoader) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (param == null) {
			return null;
		}
		try {
			@SuppressWarnings("unchecked")
			Method method = class1.getMethod(methodName, String.class);
			Object o = method.invoke(null, param);
			return o;
		} catch (NoSuchMethodException e) {
			// Searching for a method with annotation to include extra params
			Method[] methods = class1.getMethods();
			for (Method m: methods) {
				if (m.getName().equals(methodName)) {
					Annotation[] annotations = m.getDeclaredAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation instanceof ParameterInfo) {
							ParameterInfo myAnnotation = (ParameterInfo) annotation;
							if (ParameterInfo.Type.INCLUDE_RESOURCE_LOADER.equals(myAnnotation.type())) {
								@SuppressWarnings("unchecked")
								Method method = class1.getMethod(methodName, String.class, ResourceLoader.class);
								Object o = method.invoke(null, param, resourceLoader);
								return o;
							}
						}
					}
				}
			}
		}
		return null;
	}


	public static List<String> splitByUnderscore(String string) {
		return splitBy(string, "_");
	}

	public static List<String> splitByComma(String string) {
		return splitBy(string, ",");
	}
	
	public static List<String> splitBySemicolon(String string) {
		return splitBy(string, ";");
	}

	public static List<String> splitBySpace(String string) {
		return splitBy(string, " ");
	}

	public static List<String> splitBy(String string, String separator) {
		return splitBy(string, separator, Integer.MAX_VALUE, true);
	}

	public static List<String> splitName(String string) {
		return splitBy(string, " ", 2, true);
	}
	
	public static List<String> splitByX(String string) {
		return splitBy(string, "x", Integer.MAX_VALUE, true);
	}

	public static List<String> splitBy(String string, String separator, int limit, boolean trim) {
		if (string == null || separator == null) {
			return new ArrayList<String>();
		}
		String[] r = string.trim().split(separator, limit);
		if (trim) {
			for (int i = 0; i < r.length; i++) {
				r[i] = r[i].trim();
			}
		}
		return Arrays.asList(r);
	}
	
	private final static CharMatcher legalCharacters = CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.inRange('0', '9'));

	public static String removeIllegalChars(String part) {
		return legalCharacters.retainFrom(part).trim();
	}

	public static Double max(String list) {
		if (list == null) {
			return null;
		}
		list = list.trim();
		if (list.charAt(0) == '[') {
			list = list.substring(1);
		}
		if (list.charAt(list.length() - 1) == ']') {
			list = list.substring(0, list.length() - 1);
		}
		List<String> l = ParserUtil.splitByComma(list);
		if (l != null && l.size() > 0) {
			double maxDouble = Double.parseDouble(l.get(0).trim());
			for (String number: l) {
				double d = Double.parseDouble(number.trim());
				if (d > maxDouble) {
					maxDouble = d;
				}
			}
			return maxDouble;
		}
		return null;
	}

	public static Double min(String list) {
		if (list == null) {
			return null;
		}
		list = list.trim();
		if (list.charAt(0) == '[') {
			list = list.substring(1);
		}
		if (list.charAt(list.length() - 1) == ']') {
			list = list.substring(0, list.length() - 1);
		}
		List<String> l = ParserUtil.splitByComma(list);
		if (l != null && l.size() > 0) {
			double minDouble = Double.parseDouble(l.get(0).trim());
			for (String number: l) {
				double d = Double.parseDouble(number.trim());
				if (d < minDouble) {
					minDouble = d;
				}
			}
			return minDouble;
		}
		return null;
	}

}