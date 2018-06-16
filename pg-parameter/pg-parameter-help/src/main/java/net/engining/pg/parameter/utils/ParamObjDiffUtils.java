package net.engining.pg.parameter.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 参数对象对比工具
 * 
 * @author Heyu.wang
 */
public class ParamObjDiffUtils {
	private static Logger logger = LoggerFactory.getLogger(ParamObjDiffUtils.class);

	private static Set<Class<?>> SIMPLE_CLAZZS = new HashSet<Class<?>>();
	static {
		SIMPLE_CLAZZS.add(Integer.class);
		SIMPLE_CLAZZS.add(Long.class);
		SIMPLE_CLAZZS.add(Boolean.class);
		SIMPLE_CLAZZS.add(Short.class);
		SIMPLE_CLAZZS.add(Double.class);
		SIMPLE_CLAZZS.add(Character.class);
		SIMPLE_CLAZZS.add(Float.class);
		SIMPLE_CLAZZS.add(Byte.class);
		SIMPLE_CLAZZS.add(BigDecimal.class);
		SIMPLE_CLAZZS.add(Date.class);
		SIMPLE_CLAZZS.add(String.class);
		SIMPLE_CLAZZS.add(Object.class);
		SIMPLE_CLAZZS.add(Class.class);
	}

	/**
	 * TODO 比较List或map是存在问题的；重构为通过Object》Json，然后通过Json进行比较；避免对象内属性值相同，但是实例在内存中是不同的情况；
	 * @param newObj
	 *            新对象
	 * @param oldObj
	 *            原对象
	 * @param fieldInfo
	 *            {@link Field}信息
	 * @param parentId
	 *            父级标签(3.1)
	 * @param seq
	 *            本级标签顺序
	 * @param lv
	 *            缩进层次
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String diff(Object newObj, Object oldObj, String fieldInfo, String parentId, int seq, int lv) throws IllegalArgumentException, IllegalAccessException {
		
		if (newObj == null && oldObj == null)
			return "暂无比较日志";

		String log = "";
		String id = "";
		String head = "";
		if (seq != 0) {
			id = StringUtils.isBlank(parentId) ? String.valueOf(seq) : parentId + "." + seq;
			if (StringUtils.isNotBlank(fieldInfo))
				head = tab(lv++) + id + ". " + fieldInfo + ":\r\n";
		} else {
			if (StringUtils.isNotBlank(fieldInfo))
				head = tab(lv++) + fieldInfo + ":\r\n";
		}

		if (newObj == null) {
			log += head;
			log += tab(lv) + MessageFormat.format("由[{0}]改为null\r\n", oldObj.toString());
			return log;
		}

		if (oldObj == null) {
			log += head;
			log += tab(lv) + MessageFormat.format("由null改为[{0}]\r\n", newObj.toString());
			return log;
		}

		if (logger.isDebugEnabled())
			logger.debug("正在比较的原参数类型与新参数类型： oldOne[{}], newOne[{}]", oldObj.getClass(), newObj.getClass());

		//参数是否可作为Map来进行比较处理
		if (ClassUtils.isAssignable(oldObj.getClass(), Map.class) && ClassUtils.isAssignable(newObj.getClass(), Map.class)) {
			Map<?, ?> oldMap = (Map<?, ?>) oldObj;
			Map<?, ?> newMap = (Map<?, ?>) newObj;

			Collection<?> newElementKeys = null;
			Collection<?> removeElementKeys = null;
			Collection<?> updateElementKeys = null;

			if (oldMap != null && newMap != null) {
				removeElementKeys = CollectionUtils.subtract(oldMap.keySet(), newMap.keySet());
				newElementKeys = CollectionUtils.subtract(newMap.keySet(), oldMap.keySet());
				updateElementKeys = CollectionUtils.intersection(oldMap.keySet(), newMap.keySet());

				Set<Object> eqElementKeys = new HashSet<Object>();
				for (Iterator<?> i = updateElementKeys.iterator(); i.hasNext();) {
					Object e = i.next();

					if (oldMap.get(e) == null) {
						if (newMap.get(e) == null)
							eqElementKeys.add(e);
					} else {
						if (oldMap.get(e).equals(newMap.get(e)))
							eqElementKeys.add(e);
					}
				}
				updateElementKeys = CollectionUtils.subtract(updateElementKeys, eqElementKeys);
			}

			String tmpLog = "";

			if (newElementKeys != null && !newElementKeys.isEmpty()) {
				for (Object e : newElementKeys) {
					tmpLog += tab(lv) + MessageFormat.format("新增元素key[{0}]-value[{1}]\r\n", e.toString(), newMap.get(e));
				}
			}

			if (removeElementKeys != null && !removeElementKeys.isEmpty()) {
				for (Object e : removeElementKeys) {
					tmpLog += tab(lv) + MessageFormat.format("删除元素key[{0}]-value[{1}]\r\n", e.toString(), oldMap.get(e));
				}
			}

			if (updateElementKeys != null && !updateElementKeys.isEmpty()) {
				for (Object e : updateElementKeys) {
					String valueUpLog = diff(newMap.get(e), oldMap.get(e), MessageFormat.format("修改元素key[{0}]", e.toString()), "", 0, lv);
					if (StringUtils.isNotBlank(valueUpLog))
						tmpLog += valueUpLog;
				}
			}

			if (StringUtils.isNotBlank(tmpLog)) {
				log += head;
				log += tmpLog;
			}
		}
		//参数是否可作为Collection来进行比较处理
		else if ((ClassUtils.isAssignable(newObj.getClass(), Collection.class) || newObj.getClass().isArray()) && (ClassUtils.isAssignable(oldObj.getClass(), Collection.class) || oldObj.getClass().isArray())) {
			Collection<?> newElements = null;
			Collection<?> removeElements = null;

			Collection<?> oldCollection;
			Collection<?> newCollection;

			if (newObj.getClass().isArray()) {
				newCollection = (Collection<?>) (newObj == null ? null : Arrays.asList(newObj));
			} else {
				newCollection = (Collection<?>) newObj;
			}

			if (oldObj.getClass().isArray()) {
				oldCollection = (Collection<?>) (oldObj == null ? null : Arrays.asList(oldObj));
			} else {
				oldCollection = (Collection<?>) oldObj;
			}

			if (oldCollection == null) {
				newElements = newCollection;
				removeElements = null;
			}

			if (newCollection == null) {
				removeElements = oldCollection;
				newElements = null;
			}

			if (oldCollection != null && newCollection != null) {
				removeElements = CollectionUtils.subtract(oldCollection, newCollection);
				newElements = CollectionUtils.subtract(newCollection, oldCollection);
			}

			String tmpLog = head;

			if (newElements != null && !newElements.isEmpty()) {
				for (Object e : newElements) {
					tmpLog += tab(lv) + MessageFormat.format("增加元素-[{0}]\r\n", e.toString());
				}
			}

			if (removeElements != null && !removeElements.isEmpty()) {
				for (Object e : removeElements) {
					tmpLog += tab(lv) + MessageFormat.format("删除元素-[{0}]\r\n", e.toString());
				}
			}

			if (StringUtils.isNotBlank(tmpLog)) {
//				log += head;
				log += tmpLog;
			}
		} 
		else if (!oldObj.getClass().equals(newObj.getClass())) {
			throw new IllegalArgumentException("类型不同无法比较");
		}
		//参数是否可作为Simpletype来进行比较处理
		else if (isSimpleType(oldObj.getClass())) {
			if (!newObj.equals(oldObj)) {
				log += head;
				log += tab(lv) + MessageFormat.format("由[{0}]改为[{1}]\r\n", oldObj.toString(), newObj.toString());
			}
		} 
		//参数作为Class来进行比较处理
		else {
			Class<?> clazz = oldObj.getClass();

			String tmpLog = "";
			int i = 1;
			for (Field f : clazz.getFields()) {
				Object newFO = f.get(newObj);
				Object oldFO = f.get(oldObj);

				PropertyInfo pInfo = f.getAnnotation(PropertyInfo.class);
				String tmpFieldInfo = pInfo == null ? f.getName() : MessageFormat.format("{0}({1})", f.getName(), pInfo.name());
				String tmpFieldLog = diff(newFO, oldFO, tmpFieldInfo, id, i++, lv);
				if (StringUtils.isNotBlank(tmpFieldLog)) {
					tmpLog += tmpFieldLog;
				} else {
					i--;
				}
			}

			if (StringUtils.isNotBlank(tmpLog)) {
				log += head;
				log += tmpLog;
			}
		}

		return log;
	}

	public static boolean isSimpleType(Class<?> clazz) {
		if (clazz.isEnum())
			return true;
		if (clazz.isPrimitive())
			return true;
		if (SIMPLE_CLAZZS.contains(clazz))
			return true;

		return false;
	}
	
	public static String tab(int lv){
		
		return Strings.repeat(StringUtils.SPACE, lv);
		
	}
}
