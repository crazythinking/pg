package net.engining.pg.support.utils;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;


/**
 * Map 工具类.
 * <p>
 * <p>
 * <p>
 * <p/>
 * <h3>hashCode与equals:</h3>
 * <blockquote>
 * <p/>
 * <p>
 * hashCode重要么?<br>
 * 对于{@link List List}集合、数组而言,不重要,他就是一个累赘; <br>
 * 但是对于{@link HashMap HashMap}、{@link java.util.HashSet HashSet}、 {@link java.util.Hashtable Hashtable} 而言,它变得异常重要.
 * </p>
 * <p/>
 * <p>
 * 在Java中hashCode的实现总是伴随着equals,他们是紧密配合的,你要是自己设计了其中一个,就要设计另外一个。
 * </p>
 * <p>
 * <img src="http://venusdrogon.github.io/feilong-platform/mysource/hashCode-and-equals.jpg"/>
 * </p>
 * <p/>
 * 整个处理流程是:
 * <ol>
 * <li>判断两个对象的hashcode是否相等,若不等,则认为两个对象不等,完毕,若相等,则比较equals。</li>
 * <li>若两个对象的equals不等,则可以认为两个对象不等,否则认为他们相等。</li>
 * </ol>
 * </blockquote>
 * <p/>
 * <h3>关于 {@link Map }:</h3>
 * <p/>
 * <blockquote>
 * <table border="1" cellspacing="0" cellpadding="4" summary="">
 * <tr style="background-color:#ccccff">
 * <th align="left">interface/class</th>
 * <th align="left">说明</th>
 * </tr>
 * <p/>
 * <tr valign="top">
 * <td>{@link Map Map}</td>
 * <td>
 * <ol>
 * <li>An object that maps keys to values.</li>
 * <li>A map cannot contain duplicate keys</li>
 * <li>Takes the place of the Dictionary class</li>
 * </ol>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top" style="background-color:#eeeeff">
 * <td>{@link HashMap HashMap}</td>
 * <td>
 * <ol>
 * <li>Hash table based implementation of the Map interface.</li>
 * <li>permits null values and the null key.</li>
 * <li>makes no guarantees as to the order of the map</li>
 * </ol>
 * <p>
 * 扩容:
 * </p>
 * <blockquote>
 * <ol>
 * <li>{@link HashMap HashMap} 初始容量 {@link HashMap#DEFAULT_INITIAL_CAPACITY }是16,DEFAULT_LOAD_FACTOR 是0.75
 * <code>java.util.HashMap#addEntry</code> 是 2 * table.length 2倍<br>
 * </ol>
 * </blockquote>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top">
 * <td>{@link LinkedHashMap LinkedHashMap}</td>
 * <td>
 * <ol>
 * <li>Hash table and linked list implementation of the Map interface,</li>
 * <li>with predictable iteration order.</li>
 * </ol>
 * Note that: insertion order is not affected if a key is re-inserted into the map.
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top" style="background-color:#eeeeff">
 * <td>{@link java.util.TreeMap TreeMap}</td>
 * <td>
 * <ol>
 * <li>A Red-Black tree based NavigableMap implementation</li>
 * <li>sorted according to the natural ordering of its keys, or by a Comparator.</li>
 * <li>默认情况 key不能为null,如果传入了 <code>NullComparator</code>那么key 可以为null.</li>
 * </ol>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top">
 * <td>{@link java.util.Hashtable Hashtable}</td>
 * <td>
 * <ol>
 * <li>This class implements a hashtable, which maps keys to values.</li>
 * <li>synchronized.</li>
 * <li>Any non-null object can be used as a key or as a value.</li>
 * </ol>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top" style="background-color:#eeeeff">
 * <td>{@link java.util.Properties Properties}</td>
 * <td>
 * <ol>
 * <li>The Properties class represents a persistent set of properties.</li>
 * <li>can be saved to a stream or loaded from a stream.</li>
 * <li>Each key and its corresponding value in the property list is a string.</li>
 * </ol>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top">
 * <td>{@link java.util.IdentityHashMap IdentityHashMap}</td>
 * <td>
 * <ol>
 * <li>using reference-equality in place of object-equality when comparing keys (and values).</li>
 * <li>使用==代替equals()对key进行比较的散列表.专为特殊问题而设计的</li>
 * </ol>
 * <p style="color:red">
 * 注意:此类不是 通用 Map 实现！它有意违反 Map 的常规协定,此类设计仅用于其中需要引用相等性语义的罕见情况
 * </p>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top" style="background-color:#eeeeff">
 * <td>{@link java.util.WeakHashMap WeakHashMap}</td>
 * <td>
 * <ol>
 * <li>A hashtable-based Map implementation with weak keys.</li>
 * <li>它对key实行"弱引用",如果一个key不再被外部所引用,那么该key可以被GC回收</li>
 * </ol>
 * </td>
 * </tr>
 * <p/>
 * <tr valign="top">
 * <td>{@link java.util.EnumMap EnumMap}</td>
 * <td>
 * <ol>
 * <li>A specialized Map implementation for use with enum type keys.</li>
 * <li>Enum maps are maintained in the natural order of their keys</li>
 * <li>不允许空的key</li>
 * </ol>
 * </td>
 * </tr>
 * </table>
 * </blockquote>
 */
public class MapUtilsExt {

	/**
	 * <code>MapUtilsExt</code> should not normally be instantiated. Also cannot be extended
	 */
	private MapUtilsExt() {}

    /**
     * 将多值的<code>arrayValueMap</code> 转成单值的map.
     * <p/>
     * <h3>示例1:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, String[]>} arrayValueMap = new LinkedHashMap{@code <>}();
     * <p/>
     * arrayValueMap.put("province", new String[] { "江苏省" });
     * arrayValueMap.put("city", new String[] { "南通市" });
     * LOGGER.info(JsonUtil.format(ParamUtil.toSingleValueMap(arrayValueMap)));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "province": "江苏省",
     * "city": "南通市"
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <p>
     * 如果arrayValueMap其中有key的值是多值的数组,那么转换到新的map中的时候,value取第一个值,
     * </p>
     * <p/>
     * <h3>示例2:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, String[]>} arrayValueMap = new LinkedHashMap{@code <>}();
     * <p/>
     * arrayValueMap.put("province", new String[] { "浙江省", "江苏省" });
     * arrayValueMap.put("city", new String[] { "南通市" });
     * LOGGER.info(JsonUtil.format(ParamUtil.toSingleValueMap(arrayValueMap)));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "province": "浙江省",
     * "city": "南通市"
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>返回的map是 提取参数 <code>arrayValueMap</code>的key做为key,value数组的第一个元素做<code>value</code></li>
     * <li>返回的是 {@link LinkedHashMap},保证顺序和参数 <code>arrayValueMap</code>顺序相同</li>
     * <li>和该方法正好相反的是 {@link #toArrayValueMap(Map)}</li>
     * </ol>
     * </blockquote>
     *
     * @param <K>           the key type
     * @param <V>           the value type
     * @param arrayValueMap the array value map
     * @return 如果<code>arrayValueMap</code>是null或者empty,那么返回 {@link Collections#emptyMap()},<br>
     * 如果<code>arrayValueMap</code>其中有key的值是多值的数组,那么转换到新的map中的时候,value取第一个值,<br>
     * 如果<code>arrayValueMap</code>其中有key的value是null,那么转换到新的map中的时候,value以 null替代
     */
    public static <K, V> Map<K, V> toSingleValueMap(Map<K, V[]> arrayValueMap) {
        if (ValidateUtilExt.isNullOrEmpty(arrayValueMap)) {
            return emptyMap();
        }
        Map<K, V> singleValueMap = newLinkedHashMap(arrayValueMap.size());//保证顺序和参数 arrayValueMap 顺序相同
        for (Map.Entry<K, V[]> entry : arrayValueMap.entrySet()) {
            singleValueMap.put(entry.getKey(), null == entry.getValue() ? null : entry.getValue()[0]);
        }
        return singleValueMap;
    }

    /**
     * 将单值的<code>singleValueMap</code> 转成多值的map.
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, String>} singleValueMap = new LinkedHashMap{@code <>}();
     * <p/>
     * singleValueMap.put("province", "江苏省");
     * singleValueMap.put("city", "南通市");
     * <p/>
     * LOGGER.info(JsonUtil.format(ParamUtil.toArrayValueMap(singleValueMap)));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "province": ["江苏省"],
     * "city": ["南通市"]
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>返回的是 {@link LinkedHashMap},保证顺序和参数 <code>singleValueMap</code>顺序相同</li>
     * <li>和该方法正好相反的是 {@link #toSingleValueMap(Map)}</li>
     * </ol>
     * </blockquote>
     *
     * @param <K>            the key type
     * @param singleValueMap the name and value map
     * @return 如果参数 <code>singleValueMap</code> 是null或者empty,那么返回 {@link Collections#emptyMap()}<br>
     * 否则迭代 <code>singleValueMap</code> 将value转成数组,返回新的 <code>arrayValueMap</code>
     * @since 1.6.2
     */
    public static <K> Map<K, String[]> toArrayValueMap(Map<K, String> singleValueMap) {
        if (ValidateUtilExt.isNullOrEmpty(singleValueMap)) {
            return emptyMap();
        }
        Map<K, String[]> arrayValueMap = newLinkedHashMap(singleValueMap.size());//保证顺序和参数singleValueMap顺序相同
        for (Map.Entry<K, String> entry : singleValueMap.entrySet()) {
            arrayValueMap.put(entry.getKey(), ArrayUtilsExt.toArray(entry.getValue()));//注意此处的Value不要声明成V,否则会变成Object数组
        }
        return arrayValueMap;
    }

    /**
     * 仅当 <code>null != map 并且 null != value</code>才将key/value put到map中.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>如果 <code>map</code> 是null,什么都不做</li>
     * <li>如果 <code>value</code> 是null,也什么都不做</li>
     * <li>如果 <code>key</code> 是null,依照<code>map</code>的<code>key</code>是否允许是null的 规则</li>
     * </ol>
     * </blockquote>
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param map   the map to add to
     * @param key   the key
     * @param value the value
     * @see MapUtilsExt#safeAddToMap(Map, Object, Object)
     * @since 1.4.0
     */
    public static <K, V> void putIfValueNotNull(final Map<K, V> map, final K key, final V value) {
        if (null != map && null != value) {
            map.put(key, value);
        }
    }

    /**
     * 仅当 {@code null != map && null != m},才会进行 {@code map.putAll(m)} 操作
     * <p/>
     * <h3>重构:</h3>
     * <p/>
     * <blockquote>
     * <p>
     * 对于以下代码:
     * </p>
     * <p/>
     * <pre class="code">
     * if (isNotNullOrEmpty(specialSignMap)){
     * map.putAll(specialSignMap);
     * }
     * </pre>
     * <p/>
     * <b>可以重构成:</b>
     * <p/>
     * <pre class="code">
     * MapUtil.putAllIfNotNull(map, specialSignMap)
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param m   mappings to be stored in this map
     * @see Map#putAll(Map)
     */
    public static <K, V> void putAllIfNotNull(final Map<K, V> map, Map<? extends K, ? extends V> m) {
        if (null != map && null != m) {
            map.putAll(m);// m 如果是null 会报错
        }
    }

    /**
     * 仅当 <code>null != map 并且 isNotNullOrEmpty(value)</code>才将key/value put到map中.
     * <p>
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>如果 <code>map</code> 是null,什么都不做</li>
     * <li>如果 <code>value</code> 是null或者empty,也什么都不做</li>
     * <li>如果 <code>key</code> 是null,依照<code>map</code>的<code>key</code>是否允许是null的规则</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>重构:</h3>
     * <p/>
     * <blockquote>
     * <p>
     * 对于以下代码:
     * </p>
     * <p/>
     * <pre class="code">
     * <p/>
     * if (isNotNullOrEmpty(taoBaoOAuthLoginForCodeEntity.getState())){
     * nameAndValueMap.put("state", taoBaoOAuthLoginForCodeEntity.getState());
     * }
     * <p/>
     * </pre>
     * <p/>
     * <b>可以重构成:</b>
     * <p/>
     * <pre class="code">
     * MapUtil.putIfValueNotNullOrEmpty(nameAndValueMap, "state", taoBaoOAuthLoginForCodeEntity.getState());
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param map   the map
     * @param key   the key
     * @param value the value
     * @since 1.6.3
     */
    public static <K, V> void putIfValueNotNullOrEmpty(final Map<K, V> map, final K key, final V value) {
        if (null != map && ValidateUtilExt.isNotNullOrEmpty(value)) {
            map.put(key, value);
        }
    }

    /**
     * 将<code>key</code>和<code>value</code> 累加的形式put到 map中,如果<code>map</code>中存在<code>key</code>,那么累加<code>value</code>值;如果不存在那么直接put.
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * <p/>
     * Map{@code <String, Integer>} map = new HashMap{@code <>}();
     * MapUtil.putSumValue(map, "1000001", 5);
     * MapUtil.putSumValue(map, "1000002", 5);
     * MapUtil.putSumValue(map, "1000002", 5);
     * LOGGER.debug(JsonUtil.format(map));
     * <p/>
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "1000001": 5,
     * "1000002": 10
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>重构:</h3>
     * <p/>
     * <blockquote>
     * <p>
     * 对于以下代码:
     * </p>
     * <p/>
     * <pre class="code">
     * <p/>
     * if (disadvantageMap.containsKey(disadvantageToken)){
     * disadvantageMap.put(disadvantageToken, disadvantageMap.get(disadvantageToken) + 1);
     * }else{
     * disadvantageMap.put(disadvantageToken, 1);
     * }
     * <p/>
     * </pre>
     * <p/>
     * <b>可以重构成:</b>
     * <p/>
     * <pre class="code">
     * MapUtil.putSumValue(disadvantageMap, disadvantageToken, 1);
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>   the key type
     * @param map   the map
     * @param key   the key
     * @param value 数值,不能为null,可以是负数
     * @return 如果 <code>map</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>value</code> 是null,抛出 {@link NullPointerException}<br>
     * @see org.apache.commons.collections4.bag.HashBag
     * @see org.apache.commons.lang3.mutable.MutableInt
     * @see "java.util.Map#getOrDefault(Object, Object)"
     * @see <a href="http://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java">most-efficient-way-to-
     * increment-a-map-value-in-java</a>
     * @since 1.5.5
     */
    public static <K> Map<K, Integer> putSumValue(Map<K, Integer> map, K key, Integer value) {
    	ValidateUtilExt.notNull(map, "map can't be null!");
    	ValidateUtilExt.notNull(value, "value can't be null!");

        Integer v = map.get(key);//这里不要使用 map.containsKey(key),否则会有2次  two potentially expensive operations
        map.put(key, null == v ? value : value + v);//Suggestion: you should care about code readability more than little performance gain in most of the time.
        return map;
    }

    /**
     * 往 map 中put 指定 key value(多值形式).
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>map已经存在相同名称的key,那么value以list的形式累加.</li>
     * <li>如果map中不存在指定名称的key,那么会构建一个ArrayList</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * <p/>
     * Map{@code <String, List<String>>} mutiMap = newLinkedHashMap(2);
     * MapUtil.putMultiValue(mutiMap, "name", "张飞");
     * MapUtil.putMultiValue(mutiMap, "name", "关羽");
     * MapUtil.putMultiValue(mutiMap, "age", "30");
     * <p/>
     * LOGGER.debug(JsonUtil.format(mutiMap));
     * <p/>
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "name":         [
     * "张飞",
     * "关羽"
     * ],
     * "age": ["30"]
     * }
     * <p/>
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>对于下面的代码:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * <p/>
     * private void putItemToMap(Map{@code <String, List<Item>>} map,String tagName,Item item){
     * List{@code <Item>} itemList = map.get(tagName);
     * <p/>
     * if (isNullOrEmpty(itemList)){
     * itemList = new ArrayList{@code <Item>}();
     * }
     * itemList.add(item);
     * map.put(tagName, itemList);
     * }
     * <p/>
     * </pre>
     * <p/>
     * 可以重构成:
     * <p/>
     * <pre class="code">
     * <p/>
     * private void putItemToMap(Map{@code <String, List<Item>>} map,String tagName,Item item){
     * com.feilong.core.util.MapUtil.putMultiValue(map, tagName, item);
     * }
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param map   the map
     * @param key   the key
     * @param value the value
     * @return 如果 <code>map</code> 是null,抛出 {@link NullPointerException}<br>
     * @see "com.google.common.collect.ArrayListMultimap"
     * @see org.apache.commons.collections4.MultiValuedMap
     * @see org.apache.commons.collections4.IterableMap
     * @see org.apache.commons.collections4.MultiMapUtils
     * @see org.apache.commons.collections4.multimap.AbstractMultiValuedMap#put(Object, Object)
     */
    public static <K, V> Map<K, List<V>> putMultiValue(Map<K, List<V>> map, K key, V value) {
    	ValidateUtilExt.notNull(map, "map can't be null!");

        List<V> list = defaultIfNull(map.get(key), new ArrayList<V>());
        list.add(value);

        map.put(key, list);
        return map;
    }

    /**
     * 获得一个<code>map</code> 中的按照指定的<code>key</code> 整理成新的map.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>返回的map为 {@link LinkedHashMap},key的顺序 按照参数 <code>keys</code>的顺序</li>
     * <li>如果循环的 key不在map key里面,则返回的map中忽略该key,并输出warn level log</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Integer>} map = new HashMap{@code <>}();
     * map.put("a", 3007);
     * map.put("b", 3001);
     * map.put("c", 3001);
     * map.put("d", 3003);
     * LOGGER.debug(JsonUtil.format(MapUtil.getSubMap(map, "a", "c")));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "a": 3007,
     * "c": 3001
     * }
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>  the key type
     * @param <T>  the generic type
     * @param map  the map
     * @param keys 如果循环的 key不在map key里面,则返回的map中忽略该key,并输出warn level log
     * @return 如果 <code>map</code> 是null或者empty,返回 {@link Collections#emptyMap()};<br>
     * 如果 <code>keys</code> 是null或者empty,直接返回 <code>map</code><br>
     * 如果循环的 key不在map key里面,则返回的map中忽略该key,并输出warn level log
     */
    @SafeVarargs
    public static <K, T> Map<K, T> getSubMap(Map<K, T> map, K... keys) {
        if (ValidateUtilExt.isNullOrEmpty(map)) {
            return emptyMap();
        }
        if (ValidateUtilExt.isNullOrEmpty(keys)) {
            return map;
        }
        //保证元素的顺序 ,key的顺序 按照参数 <code>keys</code>的顺序
        Map<K, T> returnMap = newLinkedHashMap(keys.length);
        for (K key : keys) {
            if (map.containsKey(key)) {
                returnMap.put(key, map.get(key));
            } else {
            }
        }
        return returnMap;
    }

    /**
     * 获得 sub map(去除不需要的keys).
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>返回值为 {@link LinkedHashMap},key的顺序 按照参数 <code>map</code>的顺序</li>
     * <li>如果 <code>excludeKeys</code>中含有 map 中不存在的key,将会输出warn级别的log</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Integer>} map = new LinkedHashMap{@code <>}();
     * <p/>
     * map.put("a", 3007);
     * map.put("b", 3001);
     * map.put("c", 3002);
     * map.put("g", -1005);
     * <p/>
     * LOGGER.debug(JsonUtil.format(MapUtil.getSubMapExcludeKeys(map, "a", "g", "m")));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "b": 3001,
     * "c": 3002
     * }
     * <p/>
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>         the key type
     * @param <T>         the generic type
     * @param map         the map
     * @param excludeKeys the keys
     * @return 如果 <code>map</code> 是null或者empty,返回 {@link Collections#emptyMap()};<br>
     * 如果 <code>excludeKeys</code> 是null或者empty,直接返回 <code>map</code>
     * @since 1.0.9
     */
    @SafeVarargs
    public static <K, T> Map<K, T> getSubMapExcludeKeys(Map<K, T> map, K... excludeKeys) {
        if (ValidateUtilExt.isNullOrEmpty(map)) {
            return emptyMap();
        }
        return ValidateUtilExt.isNullOrEmpty(excludeKeys) ? map : removeKeys(new LinkedHashMap<>(map), excludeKeys); //保证元素的顺序 
    }

    /**
     * 删除 <code>map</code> 的指定的 <code>keys</code>.
     * <p/>
     * <h3>注意</h3>
     * <p/>
     * <blockquote>
     * <p>
     * 直接操作的是参数<code>map</code>,迭代 <code>keys</code>,<br>
     * 如果 <code>map</code>包含key,那么直接调用 {@link Map#remove(Object)},<br>
     * 如果不包含,那么输出warn级别日志
     * </p>
     * </blockquote>
     * <p/>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * <p/>
     * Map{@code <String, String>} map = newLinkedHashMap(3);
     * <p/>
     * map.put("name", "feilong");
     * map.put("age", "18");
     * map.put("country", "china");
     * <p/>
     * LOGGER.debug(JsonUtil.format(MapUtil.removeKeys(map, "country")));
     * <p/>
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "name": "feilong",
     * "age": "18"
     * }
     * <p/>
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map
     * @param keys the keys
     * @return 如果 <code>map</code> 是null,返回null<br>
     * 如果 <code>keys</code> 是null或者empty,直接返回 <code>map</code><br>
     * @since 1.6.3
     */
    @SafeVarargs
    public static <K, V> Map<K, V> removeKeys(Map<K, V> map, K... keys) {
        if (null == map) {// since 1.8.6
            return null;
        }

        if (ValidateUtilExt.isNullOrEmpty(keys)) {
            return map;
        }
        for (K key : keys) {
            if (map.containsKey(key)) {
                map.remove(key);
            } else {
            }
        }
        return map;
    }

    /**
     * 将 <code>map</code> 的key和value互转.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li><span style="color:red">这个操作map预先良好的定义</span>.</li>
     * <li>如果传过来的map,不同的key有相同的value,那么返回的map(key)只会有一个(value),其他重复的key被丢掉了</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Integer>} map = new HashMap{@code <>}();
     * map.put("a", 3007);
     * map.put("b", 3001);
     * map.put("c", 3001);
     * map.put("d", 3003);
     * LOGGER.debug(JsonUtil.format(MapUtil.invertMap(map)));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "3001": "c",
     * "3007": "a",
     * "3003": "d"
     * }
     * </pre>
     * <p/>
     * 可以看出 b元素被覆盖了
     * <p/>
     * </blockquote>
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return 如果<code>map</code> 是null,返回 null<br>
     * 如果<code>map</code> 是empty,返回 一个 new HashMap
     * @see MapUtilsExt#invertMap(Map)
     * @since 1.2.2
     */
    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        return null == map ? null : MapUtilsExt.invertMap(map);//返回的是 HashMap
    }

    /**
     * 以参数 <code>map</code>的key为key,以参数 <code>map</code> value的指定<code>extractPropertyName</code>属性值为值,拼装成新的map返回.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>返回map的顺序,按照参数 map key的顺序</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <Long, User>} map = new LinkedHashMap{@code <>}();
     * map.put(1L, new User(100L));
     * map.put(2L, new User(200L));
     * map.put(5L, new User(500L));
     * map.put(4L, new User(400L));
     * <p/>
     * LOGGER.debug(JsonUtil.format(MapUtil.extractSubMap(map, "id")));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "1": 100,
     * "2": 200,
     * "5": 500,
     * "4": 400
     * }
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>                 key的类型
     * @param <O>                 map value bean类型
     * @param <V>                 map value bean相关 属性名称 <code>extractPropertyName</code> 的值类型
     * @param map                 the map
     * @param extractPropertyName 泛型O对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *                            <a href="../bean/BeanUtil.html#propertyName">propertyName</a>
     * @return 如果 <code>map</code> 是null或者empty,返回 {@link Collections#emptyMap()}<br>
     * 如果 <code>extractPropertyName</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>extractPropertyName</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * @since 1.8.0 remove class param
     */
    public static <K, O, V> Map<K, V> extractSubMap(Map<K, O> map, String extractPropertyName) {
        return extractSubMap(map, null, extractPropertyName);
    }

    /**
     * 以参数 <code>map</code>的key为key,以参数 <code>map</code>value的指定<code>extractPropertyName</code>
     * 属性值为值,拼装成新的map返回.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>如果在抽取的过程中,<code>map</code>没有某个 <code>includeKeys</code>,将会忽略该key的抽取,并输出 warn log</li>
     * <li>如果参数 <code>includeKeys</code>是null或者 empty,那么会抽取map所有的key</li>
     * <li>返回map的顺序,按照参数includeKeys的顺序(如果includeKeys是null,那么按照map key的顺序)</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <Long, User>} map = new LinkedHashMap{@code <>}();
     * map.put(1L, new User(100L));
     * map.put(2L, new User(200L));
     * map.put(53L, new User(300L));
     * map.put(5L, new User(500L));
     * map.put(6L, new User(600L));
     * map.put(4L, new User(400L));
     * <p/>
     * Long[] includeKeys = { 5L, 4L };
     * LOGGER.debug(JsonUtil.format(MapUtil.extractSubMap(map, includeKeys, "id")));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "5": 500,
     * "4": 400
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>典型示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * <p/>
     * private Map{@code <Long, Long>} constructPropertyIdAndItemPropertiesIdMap(
     * String properties,
     * Map{@code <Long, PropertyValueSubViewCommand>} itemPropertiesIdAndPropertyValueSubViewCommandMap){
     * Long[] itemPropertiesIds = StoCommonUtil.toItemPropertiesIdLongs(properties);
     * <p/>
     * Map{@code <Long, Long>} itemPropertiesIdAndPropertyIdMap = MapUtil
     * .<b>extractSubMap</b>(itemPropertiesIdAndPropertyValueSubViewCommandMap, itemPropertiesIds, "propertyId");
     * <p/>
     * return MapUtil.invertMap(itemPropertiesIdAndPropertyIdMap);
     * }
     * <p/>
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <K>                 key的类型
     * @param <O>                 map value bean类型
     * @param <V>                 map value bean相关 属性名称 <code>extractPropertyName</code> 的值类型
     * @param map                 the map
     * @param includeKeys         the include keys
     * @param extractPropertyName 泛型O对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *                            <a href="../bean/BeanUtil.html#propertyName">propertyName</a>
     * @return 如果 <code>map</code> 是null或者empty,返回 {@link Collections#emptyMap()}<br>
     * 如果 <code>extractPropertyName</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>extractPropertyName</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * 如果 <code>includeKeys</code> 是null或者empty, then will extract map total keys<br>
     * @since 1.8.0 remove class param
     */
    public static <K, O, V> Map<K, V> extractSubMap(Map<K, O> map, K[] includeKeys, String extractPropertyName) {
        if (ValidateUtilExt.isNullOrEmpty(map)) {
            return emptyMap();
        }

        Validate.notBlank(extractPropertyName, "extractPropertyName can't be null/empty!");

        //如果excludeKeys是null,那么抽取所有的key
        @SuppressWarnings("unchecked") // NOPMD - false positive for generics
            K[] useIncludeKeys = ValidateUtilExt.isNullOrEmpty(includeKeys) ? (K[]) map.keySet().toArray() : includeKeys;

        //保证元素的顺序,顺序是参数  includeKeys的顺序
        Map<K, V> returnMap = newLinkedHashMap(useIncludeKeys.length);
        for (K key : useIncludeKeys) {
            if (map.containsKey(key)) {
                returnMap.put(key, PropertyUtilsExt.<V>getProperty(map.get(key), extractPropertyName));
            } else {
            }
        }
        return returnMap;
    }

    //*************************************************************************************************

    /**
     * 创建 {@code HashMap}实例,拥有足够的 "initial capacity" 应该控制{@code expectedSize} elements without growth.
     * <p/>
     * <p>
     * This behavior cannot be broadly guaranteed, but it is observed to be true for OpenJDK 1.7. <br>
     * It also can't be guaranteed that the method isn't inadvertently <i>oversizing</i> the returned map.
     * </p>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, String>} newHashMap = MapUtil.newHashMap(3);
     * newHashMap.put("name", "feilong");
     * newHashMap.put("age", "18");
     * newHashMap.put("address", "shanghai");
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>使用该方法的好处:</h3>
     * <p/>
     * <blockquote>
     * <ol>
     * <li><b>简化代码书写方式</b>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * 以前你可能需要这么写代码:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>HashMap</b>{@code <String, Map<Long, List<String>>>}(16);
     * </pre>
     * <p/>
     * <p>
     * 如果你是使用JDK1.7或者以上,你可以使用钻石符:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>HashMap</b>{@code <>}(16);
     * </pre>
     * <p/>
     * <p>
     * 不过只要你是使用1.5+,你都可以写成:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = MapUtil.<b>newHashMap</b>(16);
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * </li>
     * <li><b>减少扩容次数</b>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * 如果你要一次性初始一个能存放100个元素的map,并且不需要扩容,提高性能的话,你需要
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>HashMap</b>{@code <String, Map<Long, List<String>>>}(100/0.75+1);
     * </pre>
     * <p/>
     * <p>
     * 使用这个方法,你可以直接写成:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = MapUtil.<b>newHashMap</b>(100);
     * </pre>
     * <p/>
     * </blockquote>
     * </li>
     * <p/>
     * </ol>
     * </blockquote>
     *
     * @param <K>          the key type
     * @param <V>          the value type
     * @param expectedSize the number of entries you expect to add to the returned map
     * @return a new, empty {@code HashMap} with enough capacity to hold {@code expectedSize} entries without resizing
     * @throws IllegalArgumentException 如果 size{@code  < }0
     * @see "com.google.common.collect.Maps#newHashMapWithExpectedSize(int)"
     * @see HashMap#HashMap(int)
     * @since 1.7.1
     */
    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(toInitialCapacity(expectedSize));
    }

    /**
     * 创建 {@code LinkedHashMap}实例,拥有足够的 "initial capacity" 应该控制{@code expectedSize} elements without growth.
     * <p/>
     * <p>
     * This behavior cannot be broadly guaranteed, but it is observed to be true for OpenJDK 1.7. <br>
     * It also can't be guaranteed that the method isn't inadvertently <i>oversizing</i> the returned map.
     * </p>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * Map{@code <String, String>} map = MapUtil.newLinkedHashMap(3);
     * map.put("name", "feilong");
     * map.put("age", "18");
     * map.put("address", "shanghai");
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>使用该方法的好处:</h3>
     * <p/>
     * <blockquote>
     * <ol>
     * <li><b>简化代码书写方式</b>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * 以前你可能需要这么写代码:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>LinkedHashMap</b>{@code <String, Map<Long, List<String>>>}(16);
     * </pre>
     * <p/>
     * <p>
     * 如果你是使用JDK1.7或者以上,你可以使用钻石符:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>LinkedHashMap</b>{@code <>}(16);
     * </pre>
     * <p/>
     * <p>
     * 不过只要你是使用1.5+,你都可以写成:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = MapUtil.<b>newLinkedHashMap</b>(16);
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * </li>
     * <p/>
     * <li><b>减少扩容次数</b>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * 如果你要一次性初始一个能存放100个元素的map,并且不需要扩容,提高性能的话,你需要
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = new <b>LinkedHashMap</b>{@code <String, Map<Long, List<String>>>}(100/0.75+1);
     * </pre>
     * <p/>
     * <p>
     * 使用这个方法,你可以直接写成:
     * </p>
     * <p/>
     * <pre class="code">
     * Map{@code <String, Map<Long, List<String>>>} map = MapUtil.<b>newLinkedHashMap</b>(100);
     * </pre>
     * <p/>
     * </blockquote>
     * </li>
     * <p/>
     * </ol>
     * </blockquote>
     *
     * @param <K>          the key type
     * @param <V>          the value type
     * @param expectedSize the number of entries you expect to add to the returned map
     * @return a new, empty {@code LinkedHashMap} with enough capacity to hold {@code expectedSize} entries without resizing
     * @throws IllegalArgumentException 如果 size{@code  < }0
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
        return new LinkedHashMap<>(toInitialCapacity(expectedSize));
    }

    /**
     * 将map的size转成initialCapacity
     *
     * @param size map的size
     * @return the int
     * @throws IllegalArgumentException 如果 size < 0
     */
    private static int toInitialCapacity(int size) {
    	ValidateUtilExt.isTrue(size >= 0, "size :[%s] must >=0", size);
        //借鉴了 google guava 的实现,不过 guava 不同版本实现不同
        //guava 19 (int) (expectedSize / 0.75F + 1.0F)
        //guava 18  expectedSize + expectedSize / 3
        //google-collections 1.0  Math.max(expectedSize * 2, 16)
        return (int) (size / 0.75f) + 1;
    }

}
