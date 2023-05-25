package com.mm.common.okbus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import com.mm.common.utils.LogUtil;
import com.mm.common.Constants;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 1.0
 * Created by m
 * 事件总线用于接收和发送消息
 */

public class OkBus {
    private final ConcurrentHashMap<Integer, CopyOnWriteArrayList<BusEvent>> mEventList = new ConcurrentHashMap<>();//存储所有事件ID以及其回调
    private final ConcurrentHashMap<Integer, Object> mStickyEventList = new ConcurrentHashMap<>();//存储粘连事件ID以及其数据
    private final ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(5);
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private OkBus() {
    }

    private static class Holder {
        public static OkBus eb = new OkBus();
    }

    public static OkBus getInstance() {
        return Holder.eb;
    }

    public void register(int tag, Event ev) {
        register(tag, ev, Bus.DEFAULT);
    }

    public void register(int tag, final Event ev, int thread) {
        String className = "";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = stackTrace.length - 2; i >= 0; i--) {
            if (stackTrace[i].getMethodName().equals("register")) {
                StackTraceElement element = stackTrace[i + 1];
                className = element.getClassName();
                break;
            }
        }
        CopyOnWriteArrayList<BusEvent> busEvents = mEventList.get(tag);
        BusEvent event = BusEvent.create(this, className, ev, thread);
        if (busEvents != null) {
            busEvents.add(event);
        } else {
            busEvents = new CopyOnWriteArrayList<>();
            busEvents.add(event);
            mEventList.put(tag, busEvents);
        }
        LogUtil.i(Constants.TAG, "Bus register   " + tag + " :" + busEvents.size());
        if (mStickyEventList.get(tag) != null) {//注册时分发粘连事件
            final Message msg = Message.obtain();
            msg.obj = mStickyEventList.get(tag);
            msg.what = tag;
            event.dispatch(msg);
            LogUtil.i(Constants.TAG, "mStickyEvent register  and  onEvent " + tag + " :" + mEventList.get(tag).size());
        }
    }

    /**
     * 一次性注销所有当前事件监听器
     *
     * @param ev 事件
     */
    public void unRegister(Event ev) {
        Enumeration<Integer> keys = mEventList.keys();
        while (keys.hasMoreElements()) {
            int key = (int) keys.nextElement();
            CopyOnWriteArrayList<BusEvent> list = mEventList.get(key);
            if (list != null) {
                for (BusEvent event : list) {
                    if (event.matchEvent(ev)) {
                        list.remove(event);
                        LogUtil.i(Constants.TAG, "remove Event " + "key :" + key + "   keys:" + ev.toString());
                        break;
                    }
                }
            }
        }
    }

    public void unRegister(Object obj, int tag) {
        if (obj == null) {
            mEventList.remove(tag);
            return;
        }
        CopyOnWriteArrayList<BusEvent> list = mEventList.get(tag);
        if (list != null) {
            String name = obj.getClass().getName();
            for (BusEvent event : list) {
                if (event.matchClass(name)) {
                    list.remove(event);
                    LogUtil.i(Constants.TAG, "remove Event " + "key :" + tag + "   event:" + event);
                }
            }
        }
    }

    public void unRegister(int tag) {
        unRegister(null, tag);
    }


    public void onEvent(int tag) {
        onEvent(tag, null);
    }

    public void onStickyEvent(int tag, Object data) {
        LogUtil.i(Constants.TAG, "Bus onStickyEvent " + tag + " ");
        mStickyEventList.put(tag, (data == null ? tag : data));
        onEvent(tag, data);
    }

    public void onStickyEvent(int tag) {
        onStickyEvent(tag, null);
    }

    /**
     * @param tag  发送消息的事件ID
     * @param data 发送消息的数据
     */
    public void onEvent(int tag, Object data) {
        String hex = Integer.toHexString(Math.abs(tag));
        LogUtil.i("Message OkBus", "onEvent  " + (tag > 0 ? "[普通]" : "[服务]") + "  tag: " + hex);

        //1、本地先处理非服务消息
        if (tag >= 0) onLocalEvent(tag, data);

        //2、如果是组建化，向服务器发消息
        if (isModule.get()) {
            if (!isModuleConnected()) {
                LogUtil.i("Message OkBus", "发消息失败，服务已经断开链接，尝试重新打开服务，进行发消息");
                BaseAppModuleApp.getBaseApplication().connectService();
            }
            if (data == null || data instanceof Serializable) {
                Message newMsg = new Message();
                if (data != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.MESSAGE_DATA, (Serializable) data);
                    newMsg.setData(bundle);
                }
                newMsg.arg1 = mModuleId.hashCode();
                newMsg.what = tag;
                try {
                    mServiceMessenger.send(newMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                assert false : "跨进程时，你传递的对象没有序列化！";
            }
        } else if (tag < 0) {//非组件化时本地处理服务消息
            onLocalEvent(tag, data);
        }
    }

    /**
     * 触发本地消息事件
     *
     * @param tag
     * @param data
     */
    public void onLocalEvent(int tag, Object data) {
        Message msg = Message.obtain();
        msg.obj = data;
        msg.what = tag;
        //1、本地先处理消息
        CopyOnWriteArrayList<BusEvent> events = mEventList.get(tag);
        if (events != null) {
            LogUtil.i(Constants.TAG + " OkBus", "Bus onEvent " + tag + " :" + events.size());
            for (BusEvent ev : events) {
                ev.dispatch(msg);
            }
        }
    }


    private final AtomicBoolean isModule = new AtomicBoolean(false);// 是否是模块化
    public Messenger mServiceMessenger;
    private BaseModule mBaseModule;
    public String mModuleId;

    public void initModule(BaseModule mBaseModule, Messenger mServiceMessenger, String mModuleId, Messenger mClientMessenger) {
        this.mServiceMessenger = mServiceMessenger;
        this.mModuleId = mModuleId;
        this.mBaseModule = mBaseModule;
        isModule.set(true);
        mBaseModule.isConnected.set(true);

        Message msg = Message.obtain();
        Bundle data = new Bundle();
        //模块类型的注册事件 以 module_ 开头
        if (mModuleId.startsWith("module_")) {
            data.putString(Constants.REGISTER_ID, mModuleId);//注册模块
        } else {
            data.putString(Constants.REGISTER_ID, "module_" + mModuleId);//注册模块
        }
        msg.setData(data);
        msg.replyTo = mClientMessenger;   //将处理消息的Messenger绑定到消息上带到服务端
        try {
            mServiceMessenger.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isModule() {
        return isModule.get();
    }

    public boolean isModuleConnected() {
        return mBaseModule.isConnected.get();
    }

    static class BusEvent {
        private final String className;
        private final Event event;
        private final int thread;
        private final Handler mHandler;
        private final Executor mExecutor;

        static BusEvent create(OkBus bus, String cl, Event ev, int thread) {
            return new BusEvent(bus, cl, ev, thread);
        }

        private BusEvent(OkBus bus, String cl, Event ev, int thread) {
            this.className = cl;
            this.event = ev;
            this.thread = thread;
            this.mHandler = bus.mHandler;
            this.mExecutor = bus.mExecutor;
        }

        public void dispatch(Message msg) {
            switch (thread) {
                case Bus.DEFAULT:
                    event.call(msg);
                    break;
                case Bus.UI:
                    mHandler.post(() -> event.call(msg));
                    break;
                case Bus.BG:
                    mExecutor.execute(() -> event.call(msg));
                    break;
            }
        }

        public boolean matchClass(String cla) {
            return !TextUtils.isEmpty(cla) && cla.equals(className);
        }

        public boolean matchEvent(Event ev) {
            return ev != null && ev == event;
        }

        @Override
        public String toString() {
            return "BusEvent{" +
                    "className='" + className + '\'' +
                    ", event=" + event +
                    ", thread=" + thread +
                    '}';
        }
    }
}
