package com.mm.common

/**
 * Created by : m
 * @since 1.0
 * 常规一般参数
 */

object Constants {

    const val ENCRYPT_KEY = "com.mm.mvvm_poet"

    //==================URL========================//
    //服务器的包名
    const val SERVICE_PACKAGE_NAME = "com.mm.module_service"

    //==================Message Constants============//
    const val TAG = "Message"
    const val MODULE_NAME = "module"
    const val MESSAGE_DATA = "message_data"
    const val REGISTER_ID = "registerId"
    const val REGISTER_RES = "registerRes" //注册结果  0 失败 1 成功
    const val NOTICE_MSG = "notice_message"
    const val REGISTER_SEC = 1
    const val REGISTER_FAIL = 0

    //==========模块以及模块下的事件============//
    const val ROUTER_OPEN_URL = 0x0000 //打开制定url

    /**
     * 模块定义说明：
     * 模块定为 module_ 开头
     * 模块下的事件 以module名为开头
     */
    const val MODULE_MINE = "module_mine" //我的
    const val MODULE_NOTICE = "module_notice"//服务通知
    const val MODULE_KNOWLDGE = "module_knowldge"//知识文章

    //okbus 事件通知id 16进制 4位数字的状态码
    object BUS {
        const val UNREAD_NUM = -0x0001 //未读消息数
        const val UPDATE_READ_MESSAGE = -0x0002 //更新未读消息数量
        const val REGISTER_PUSH = -0x0003 //向服务器注册推送
        const val UNREGISTER_PUSH = -0x0004 //向服务器取消注册
        const val REFRESH_USERINFO = -0x0005 //更新用户信息
        const val REFRESH_DATA = -0x0006   //更新数据

        //知识文章模块
        const val KNOWLDGE_SEE = -0x1001//知识文章浏览量
        const val KNOWLDGE_LIKE = -0x1002//知识文章点赞

        //工单列表页
        const val WORKORDER_LIST_MAIN = -0x2001//工单列表待接单
        const val WORKORDER_LIST_NO_COMPLETE = -0x2002//工单列表待我完成
        const val WORKORDER_LIST_COMPLETE = -0x2003//工单列表已完成
        const val LIST_SERVICE_APPOINT = -0x2004//服务预约列表

    }


    //==================模块间的服务定义============//
    /**
     * 服务定义规则： 16进制 5位数字的状态码
     * 1、服务的请求ID必须是负值(正值表示事件)
     * 2、服务的请求ID必须是奇数，偶数表示该服务的返回事件，
     * 即：   requestID－1 ＝ returnID
     * 例如  -0xa001表示服务请求  -0xa002表示-0xa001的服务返回
     */
    object SERVICE_BUS {
        const val SERVICE_A_UID = -0xa00001
    }

    object KEY {
        const val AGREE_AGREEMENT = "agree_agreement" //是否已经统一协议
        const val GUIDE_SHOWED = "guide_showed" //是否已经查看引导页
        const val SERVER_URL = "server_url" //服务地址
        const val CHECKED_SERVER_URL = "checked_server_url" //选中的服务地址
        const val LOGIN_PHONE = "login_phone" //手机登录
        const val ACCESS_TOKEN = "access_token" //access_token
        const val FRESH_TOKEN = "fresh_token" //免登陆
        const val BINDING = "binding" //binding
        const val USER_INFO = "user_info" //userinfo
        const val LOGIN_TYPE = "login_type" //登录方式  私有云账号登录0  账号登录1  手机号密码2  手机号验证码3
        const val DEVICE_TOKEN = "device_token" //友盟推送token
        const val PUSH_ENABLE = "push_enable" //友盟推送是否允许
        //host 地址
        const val LIGHTNING_URL = "lightning_url" //lightning 地址
        const val DOMAIN_URL = "domain_url" //domain 地址
        const val GLOBAL_URL = "global_url" //global 小程序地址
    }
}