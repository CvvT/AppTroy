package com.cc.apptroy.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Utility {

	public static int getApiLevel() {

		try {
			Class<?> mClassType = Class.forName("android.os.SystemProperties");
			Method mGetIntMethod = mClassType.getDeclaredMethod("getInt",
					String.class, int.class);
			mGetIntMethod.setAccessible(true);
			return (Integer)mGetIntMethod.invoke(null, "ro.build.version.sdk",14);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 14;

	}

	public static String dottoslash(String name){
		if (name.length() <= 2) //for I and [I, and it can not contain "."
			return name;
		name = name.replaceAll("\\.", "/");
		if (name.charAt(0) == '[')
			return name;
        switch (name){
            case "void":
                return "V";
            case "boolean":
                return "Z";
            case "float":
                return "F";
            case "short":
                return "S";
            case "char":
                return "C";
            case "double":
                return "D";
            case "long":
                return "J";
            case "byte":
                return "B";
            case "int":
                return "I";
        }

		return "L" + name + ";";
	}

	public static String getClassName(String signature) {
		String clsName = signature.replaceAll("/", ".");
		if (clsName.charAt(0) == 'L' && clsName.charAt(clsName.length()-1) == ';') {
			return clsName.substring(1, clsName.length() - 1);
		}
		return clsName;
	}

	public static List<String> listDesc(String desc) {
		List<String> list = new ArrayList<String>(5);
		char[] chars = desc.toCharArray();
		int i = 0;
		while (i < chars.length) {
			switch (chars[i]) {
				case 'V':
				case 'Z':
				case 'C':
				case 'B':
				case 'S':
				case 'I':
				case 'F':
				case 'J':
				case 'D':
					list.add(Character.toString(chars[i]));
					i++;
					break;
				case '[': {
					int count = 1;
					while (chars[i + count] == '[') {
						count++;
					}
					if (chars[i + count] == 'L') {
						count++;
						while (chars[i + count] != ';') {
							count++;
						}
					}
					count++;
					list.add(new String(chars, i, count));
					i += count + 1;
					break;
				}
				case 'L': {
					int count = 1;
					while (chars[i + count] != ';') {
						++count;
					}
					count++;
					list.add(new String(chars, i, count));
					i += count + 1;
					break;
				}
				default:
			}
		}
		return list;
	}
}
