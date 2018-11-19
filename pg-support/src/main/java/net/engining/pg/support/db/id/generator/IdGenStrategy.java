package net.engining.pg.support.db.id.generator;

/**
 * ID号生成策略
 */
public interface IdGenStrategy {

    /**
     * ID号生成策略.
     *
     * @param originalId 原始ID号.
     * @param clazz
     * @return
     */
    String makeId(String originalId, Class<?> clazz);
}
