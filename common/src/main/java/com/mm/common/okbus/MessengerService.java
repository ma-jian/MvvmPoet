package com.mm.common.okbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;

import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @since 1.0
 * Created by m
 * <p>
 * 让客户端和服务端能互相发送和接受消息
 */

public class MessengerService extends Service {
    public WorkThread mWorkThread;
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Messenger> resultRef = new AtomicReference<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            latch.await(10, TimeUnit.SECONDS); //最多等待10秒
        } catch (Exception e) { //等待中断
            e.printStackTrace();
        }
        Messenger mMessenger = resultRef.get();
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWorkThread = new WorkThread();
        mWorkThread.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWorkThread.quit();
    }


    public class WorkThread extends Thread {
        public ServiceHandler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new ServiceHandler();
            Messenger mMessenger = new Messenger(mHandler);
            //  OkBus.getInstance().mServiceMessenger = mMessenger;
            try {
                resultRef.set(mMessenger);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
            Looper.loop();
        }

        public void quit() {
            mHandler.getLooper().quit();
        }
    }
}