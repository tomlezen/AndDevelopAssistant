package com.tlz.ada

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

/**
 * Activity生命周期监听.
 * Created by tomlezen.
 * Data: 2019/3/18.
 * Time: 16:36.
 */
class AdaActivityLifeCycleListener(private val ctx: Context) {

    /** 当前Activity实例. */
    var currentActivityInstance: Activity? = null

    /** Activity生命周期回调. */
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {

        }

        override fun onActivityResumed(activity: Activity?) {
            currentActivityInstance = activity
        }

        override fun onActivityStarted(activity: Activity?) {

        }

        override fun onActivityDestroyed(activity: Activity?) {

        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

        }

        override fun onActivityStopped(activity: Activity?) {
            if (activity == currentActivityInstance) {
                currentActivityInstance = null
            }
        }

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

        }

    }

    fun install() {
        (ctx as Application).unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        ctx.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

}