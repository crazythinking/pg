package net.engining.pg.support.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 系统工具类.
 */
public abstract class SystemUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemUtil.class);

    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String FILE_ENCODING = System.getProperty("file.encoding");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    public static final boolean IS_OS_WINDOWS = (File.separatorChar == '\\');
    public static final boolean IS_OS_UNIX = (File.separatorChar == '/');

    public static void die() {
        die(null, null);
    }

    /**
     * @param message
     * @param cause
     */
    public static void die(String message, Throwable cause) {
        if (message == null) {
            message = "die";
        }
        Throwable ex = new Exception(message, cause);
        logger.error("***************************************************");
        logger.error("!!! SYSTEM DEAD !!!");
        logger.error("------Exception------");
        logger.error(message, ex);
        logger.error("------System.getProperties------");
        logger.error(System.getProperties().toString());
        logger.error("------System.getenv------");
        logger.error(System.getenv().toString());
        logger.error("------Threads------");
        logger.error(JVMUtil.getThreadDump());
        logger.error("***************************************************");
        System.exit(1);
    }
}
