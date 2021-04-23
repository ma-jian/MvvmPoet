package com.mm.mvvmpoet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mm.lib_util.DialogQueue

/**
 * Created by : majian
 * Date : 4/23/21
 * Describe :
 */

class Fragment1 : Fragment(), DialogQueue.FragmentObserver {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.framge_layout, container, false)
        view.findViewById<TextView>(R.id.text).text = "Fragment1"
        view.setBackgroundColor(Color.RED)
        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        notifyVisible(this,!hidden)
    }
}