# MvvmPoet

### 网络缓存库

[RetrofitCache](https://github.com/ma-jian/retrofit_cache) 网络缓存,支持动态域名下发，协程flow处理数据流。

### Router路由

[Router](https://github.com/ma-jian/router) 路由导航、获取跨module的服务接口

当module需要提供自身功能对外的能力时，使用 include_with_api 来替代 include，以便自动生成对应的module-api 并在使用此接口的module内添加依赖 implementationApi

[settings.gradle](settings.gradle)
``` gradle
include_with_api ':module_1'
include_with_api ':module_2'
```
[module_2/build.gradle](module_2/build.gradle)
``` gradle
apply from: "../compile-api.gradle"
....

implementationApi project(":module_1")
```

对外提供接口的module，将接口更改文件名为 .java -> .api (.kt -> .kapi) 并在module内实现当前接口的实现类，build 编译后会自动生成对应的module-api
``` kotlin
/** 
 * Module1Service.kapi
 * Date : 2023/5/25
 * module1对外提供接口
 */
interface Module1Service : IProvider {

    fun moduleName(): String

    fun version(): Int

}

/**
 * Date : 2023/5/25
 * Module1Service 实现类、并标记 @ServiceProvider
 */
@ServiceProvider(returnType = Module1Service::class, des = "module1 对外提供的接口")
class Module1ServiceImpl : Module1Service {

    override fun moduleName(): String {
        return BuildConfig.LIBRARY_PACKAGE_NAME
    }

    override fun version(): Int {
        return 1
    }
}
```
通过Router获取Service接口
``` kotlin
val module1 = Router.init(this).open(Module1Service::class.java).doProvider<Module1Service>()
val moduleName = module1?.moduleName()
val version = module1?.version()
```
