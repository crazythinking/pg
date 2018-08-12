package net.engining.pg.support.enums;

/**
 * 枚举基类.
 */
public interface BaseEnum<E extends Enum<?>, K> {
    /**
     * 枚举值
     *
     * @return
     */
    public K getValue();

    /**
     * 显示值
     *
     * @return
     */
    public String getLabel();
}
