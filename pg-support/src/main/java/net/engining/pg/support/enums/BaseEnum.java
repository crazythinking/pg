package net.engining.pg.support.enums;

/**
 * 枚举基类.
 */
public interface BaseEnum<E extends Enum<?>, K> {

	/**
	 * 枚举值alias
	 *
	 * @return
	 */
	public K getValue();

	/**
	 * 显示描述
	 *
	 * @return
	 */
	public String getLabel();
}
