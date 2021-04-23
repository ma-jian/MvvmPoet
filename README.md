# MvvmPoet

## 网络库 

网络库入口类 [RetrofitGlobal](https://github.com/ma-jian/MvvmPoet/blob/master/lib_http/src/main/java/com/mm/lib_http/RetrofitGlobal.kt) 支持动态域名下发，开发环境切换，协程flow处理数据流。

## 基础工具类库

#### Activity代理 
[ActivityDelegate](https://github.com/ma-jian/MvvmPoet/blob/master/lib_util/src/main/java/com/mm/lib_util/ActivityDelegate.kt) 对Activity生命周期监听代理，为其他工具提供支持，自动为工具类进行注册。

#### 屏幕适配 
[FitDisplayMetrics](https://github.com/ma-jian/MvvmPoet/blob/master/lib_util/src/main/java/com/mm/lib_util/FitDisplayMetrics.kt) 可进行宽高维度适配，支持恢复默认适配。

#### Toast类
[ToastGlobal](https://github.com/ma-jian/MvvmPoet/blob/master/lib_util/src/main/java/com/mm/lib_util/etoast/ToastGlobal.kt) 可在通知权限关闭下弹出，支持Toast队列弹出。 

#### Dialog管理队列
[DialogQueue](https://github.com/ma-jian/MvvmPoet/blob/master/lib_util/src/main/java/com/mm/lib_util/DialogQueue.kt) 支持在指定Activity，Fragment队列弹出，支持弹窗插队，延迟弹出，自定义拦截。

------
## 下一步 
#### 扩展函数
