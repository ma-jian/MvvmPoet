package com.mm.common.okbus

/**
 * Bus 事件总线
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Bus(
    /**
     * 事件订阅的线程
     */
    val thread: Int = DEFAULT,
    /**
     * 事件id
     */
    val value: String
) {
    companion object {
        const val DEFAULT = -1
        const val UI = 0
        const val BG = 1
    }
}