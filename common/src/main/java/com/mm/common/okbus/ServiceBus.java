package com.mm.common.okbus;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.mm.common.Constants;
import com.mm.common.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.0
 * Created by m
 * 服务总线
 */
@SuppressWarnings("unchecked")
public class ServiceBus {

    private final OkBus okBus;
    private final Map<Integer, String> serviceIdModule = new HashMap<>();
    private final ExecutorService service;
    private static final int threadNum = 5;//执行任务的子线程数量

    private static class Holder {
        public static final ServiceBus instance = new ServiceBus();
    }

    public static ServiceBus getInstance() {
        return Holder.instance;
    }

    private ServiceBus() {
        okBus = OkBus.getInstance();
        service = Executors.newFixedThreadPool(threadNum);
    }


    /**
     * 异步调用服务
     *
     * @param serviceId 服务id
     * @param callback  回调
     */
    public void fetchService(final int serviceId, final Event callback) {
        if (serviceId > 0 || serviceId % 2 == 0) {
            assert false : "请求ID必须是负奇值!";
            return;
        }
        if (okBus.isModule() && !okBus.isModuleConnected()) {
            BaseAppModuleApp.getBaseApplication().connectService();
            return;
        }

        //自动唤醒目标进程
        if (okBus.isModule()) {
            noticeModule(serviceIdModule.get(serviceId), serviceId, null);
        }

        //1、先注册回调
        okBus.register(serviceId - 1, msg -> {
            callback.call(msg);
            okBus.unRegister(serviceId - 1);//服务是单次调用，触发后即取消注册
        });
        //2、通知目标模块
        okBus.onEvent(serviceId);
    }

    /**
     * 唤醒目标进程
     *
     * @param module_name 模块名
     * @param serviceId   服务ID
     * @param url         要打开的url
     */
    public void noticeModule(String module_name, int serviceId, String url) {
        Intent ait = new Intent(NoticeService.class.getCanonicalName());// 5.0+ need explicit intent        //唤醒目标进程的服务Action名
        ait.setPackage(module_name); // the package name of Remote Service  //唤醒目标进程的包名
        BaseAppModuleApp.getBaseApplication().bindService(ait, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service != null) {
                    LogUtil.i(Constants.TAG, "已经自动唤醒" + module_name);
                    Messenger moduleNameMessenger = new Messenger(service);
                    Message _msg = Message.obtain();
                    Bundle _data = new Bundle();
                    _data.putBoolean(Constants.NOTICE_MSG, true);
                    _msg.setData(_data);
                    _msg.replyTo = okBus.mServiceMessenger;//把服务器的信使给目标组件的信使，让他俩自己联系，这里仅仅是通知
                    try {
                        moduleNameMessenger.send(_msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(200);//给服务器和目标组件500ms联系的时间
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.i(Constants.TAG, module_name + "进程,本来就是醒的");
                }

                if (serviceId < 0) {  //唤醒成功，继续发送异步请求，通知目标模块
                    okBus.onEvent(serviceId);
                }
                if (!TextUtils.isEmpty(url)) {  //目标url不为空，继续打开目标
                    OkBus.getInstance().onEvent(Constants.ROUTER_OPEN_URL, url);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.i(Constants.TAG, "自动唤醒目标进程失败 module_name:" + module_name);
            }
        }, BIND_AUTO_CREATE);
    }


    /**
     * 同步调用服务
     *
     * @param serviceId
     */
    public <T> T fetchService(int serviceId) {
        return fetchService(serviceId, 3);
    }

    /**
     * 同步调用服务
     *
     * @param serviceId 服务ID
     * @param timeout   超时时间
     */
    public synchronized <T> T fetchService(final int serviceId, int timeout) {
        if (okBus.isModule() && !okBus.isModuleConnected()) {
            LogUtil.i(Constants.TAG, "请求失败，服务已经断开链接，尝试重新打开服务，进行请求");
            BaseAppModuleApp.getBaseApplication().connectService();
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> resultRef = new AtomicReference<>();
        service.execute(new Runnable() {
            @Override
            public void run() {
                fetchService(serviceId, new Event() {
                    @Override
                    public void call(@NonNull Message msg) {
                        try {
                            resultRef.set((T) msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
        });
        try {
            latch.await(timeout, TimeUnit.SECONDS); //最多等待timeout秒
        } catch (Exception e) { //等待中断
            e.printStackTrace();
        }
        return resultRef.get();
    }


    /**
     * 注册服务
     *
     * @param serviceId 服务id
     * @param callback  服务调用的回调
     * @param <T>       服务返回的数据范型
     */
    public <T> void registerService(String module, final int serviceId, final CallBack<T> callback) {
        serviceIdModule.put(serviceId, module);
        LogUtil.i(Constants.TAG, "注册服务  " + Integer.toHexString(Math.abs(serviceId)));
        okBus.unRegister(serviceId);//服务提供者只能有一个
        okBus.register(serviceId, msg -> {
            //TODO 优化到子线程
            okBus.onEvent(serviceId - 1, callback.onCall(msg));
        });
    }
}
