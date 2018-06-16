package net.engining.pg.support.utils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <pre>
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右),
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道,后台定时更新时钟，JVM退出时，线程自动回收。
 * 10亿：43410,206,210.72815533980582%；
 * 1亿：4699,29,162.0344827586207%；
 * 1000万：480,12,40.0%；
 * 100万：50,10,5.0%；
 * </pre>
 */
public class SystemClockUtil {

    private final long period;
    private final AtomicLong now;
    
    /**
     * 私有构造函数.
     *
     * @param period
     */
    private SystemClockUtil(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    /**
     * 获取实例.
     *
     * @return
     */
    private static SystemClockUtil instance() {
        return InstanceHolder.INSTANCE;
    }

    public static long now() {
        return instance().currentTimeMillis();
    }

    public static String nowDate() {
        return new Date(instance().currentTimeMillis()).toString();
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "PG System Clock");
                thread.setDaemon(true);
                return thread;
            }
        });
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                now.set(System.currentTimeMillis());
            }
        }, period, period, TimeUnit.MILLISECONDS);
    }

    private long currentTimeMillis() {
        return now.get();
    }

    private static class InstanceHolder {
        public static final SystemClockUtil INSTANCE = new SystemClockUtil(1);
    }
}
