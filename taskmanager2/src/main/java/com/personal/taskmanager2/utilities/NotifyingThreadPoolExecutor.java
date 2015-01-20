package com.personal.taskmanager2.utilities;

import android.os.Handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Omid Ghomeshi on 1/20/15.
 */
public class NotifyingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final String TAG = "NotifyingThreadPoolExecutor";

    public interface Callback {
        void onAllTasksComplete();
    }

    private int      mNumTasks = 0;
    private Callback mCallback;
    private Handler handler = new Handler();

    public NotifyingThreadPoolExecutor(int corePoolSize,
                                       int maximumPoolSize,
                                       long keepAliveTime,
                                       TimeUnit unit,
                                       BlockingQueue<Runnable> workQueue,
                                       NotifyingThreadPoolExecutor.Callback callback) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        mNumTasks = 0;
        mCallback = callback;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        mNumTasks++;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        mNumTasks--;
        if (mNumTasks == 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onAllTasksComplete();
                }
            });
        }
    }
}
