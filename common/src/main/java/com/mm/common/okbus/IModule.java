package com.mm.common.okbus;

import android.app.Application;

import androidx.fragment.app.Fragment;
import java.util.Map;

/**
 * @since 1.0
 * Created by m
 * 模块接口类
 */

public interface IModule {
    /**
     * 模块初始化，只有组建时才调用，用于开启子线程轮训消息
     */
    void init();

    /**
     * 模块ID
     *
     * @return 模块ID 以module_开头的字符命名。命名不包含改规则，则在注册是默认添加
     */
    String getModuleId();

    /**
     * 模块注册并连接成功后，可以做以下事情：
     * <p>
     * 1、注册监听事件
     * 2、发送事件
     * 3、注册服务
     * 4、调用服务
     */
    void afterConnected(Application application);
}
