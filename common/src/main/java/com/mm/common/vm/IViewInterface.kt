package com.mm.common.vm

import androidx.lifecycle.ViewModel

/**
 * Created by : m
 * @since 1.0
 * vm 接口
 */

interface IViewInterface {

     /**
      * 生成ViewModel
      */
     fun <T : ViewModel> createViewModel(clazz: Class<T>): T

     fun showToast(message: CharSequence?)

}