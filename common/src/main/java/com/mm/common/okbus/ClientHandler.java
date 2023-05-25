package com.mm.common.okbus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mm.common.Constants;
import com.mm.common.utils.LogUtil;

/**
 * @since 1.0
 * Created by m
 * 客户端消息处理器
 * <p>
 * 1、普通消息，转发给自己的OkBus
 * 2、模块注册消息，打印
 */

public class ClientHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle bundle = msg.getData();
        boolean noticeFlag = bundle.getBoolean(Constants.NOTICE_MSG, false);
        BaseModule mBaseModule = BaseAppModuleApp.getBaseApplication().mBaseModule;
        if (noticeFlag && !mBaseModule.isConnected.get()) {//唤醒通知，自动注册
            OkBus.getInstance().initModule(mBaseModule, msg.replyTo, mBaseModule.getModuleId(), mBaseModule.mWorkThread.clientHandler);
            return;
        }
        int resCode = bundle.getInt(Constants.REGISTER_RES, -1);
        if (resCode < 0) {//收到普通消息
            String hex = Integer.toHexString(Math.abs(msg.what));
            OkBus.getInstance().onLocalEvent(msg.what, bundle.getSerializable(Constants.MESSAGE_DATA));
        } else {//收到模块注册结果消息
            boolean isRegisterSec = resCode == Constants.REGISTER_SEC;
            if (isRegisterSec) {
                LogUtil.i(Constants.TAG, "handleMessage() : reply = [注册成功]");
            }
        }
    }
}