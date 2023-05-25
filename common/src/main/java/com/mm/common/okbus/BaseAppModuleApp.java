package com.mm.common.okbus;

import android.app.Application;
import android.content.Intent;

import com.mm.common.Constants;
import com.mm.common.R;
import com.mm.common.etoast.ToastCompat;
import java.util.ServiceLoader;

/**
 * @since 1.0
 * 组件的独立运行期间的Application,最终打包时根本没有这个类
 */

public class BaseAppModuleApp extends Application {

    public BaseModule mBaseModule;
    public static BaseAppModuleApp mBaseAppModuleApp;

    //只有当是组建单独运行时，才当Application运行，才会走onCreate,最终打包时根本没有这个类
    @Override
    public void onCreate() {
        super.onCreate();
        mBaseAppModuleApp = this;

        //自动注册服务器
        ServiceLoader<IModule> modules = ServiceLoader.load(IModule.class);
        mBaseModule = (BaseModule) modules.iterator().next();

        //模块初始化
        mBaseModule.init();

        //连接服务
        connectService();
    }

    /**
     * 连接服务器
     */
    public void connectService() {
        Intent intent = new Intent(MessengerService.class.getCanonicalName());// 5.0+ need explicit intent
        intent.setPackage(Constants.SERVICE_PACKAGE_NAME); // the package name of Remote Service
        boolean mIsBound = bindService(intent, mBaseModule.mConnection, BIND_AUTO_CREATE);
        if (mIsBound) {
            ToastCompat.makeText(R.string.service_connected).show();
        }
    }

    public static BaseAppModuleApp getBaseApplication() {
        return mBaseAppModuleApp;
    }
}