package com.mm.common.crash

import android.os.Message

/**
 * @since 1.0
 */
interface IActivityKiller {

    fun finishLaunchActivity(message: Message)

    fun finishResumeActivity(message: Message)

    fun finishPauseActivity(message: Message)

    fun finishStopActivity(message: Message)

    companion object {
        const val DONT_FINISH_TASK_WITH_ACTIVITY = 0
    }
}