package com.sina.util.dnscache.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RealTimeThreadPool {
    private static final Object lock = new Object();
    private static RealTimeThreadPool mInstance;
    private static ExecutorService executorService;

    public static RealTimeThreadPool getInstance() {
        if (null == mInstance) {
            synchronized (lock) {
                if (null == mInstance) {
                    mInstance = new RealTimeThreadPool();
                    executorService = new ThreadPoolExecutor(1, 100, 120, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                            new DefaultThreadFactory(),new ThreadPoolExecutor.DiscardPolicy());
                }
            }
        }
        return mInstance;
    }

    public void execute(Runnable command) {
        if (null != executorService && !executorService.isShutdown()) {
            executorService.execute(command);
        } else {
            Thread thread = new Thread(command);
            thread.start();
        }
    }

    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
