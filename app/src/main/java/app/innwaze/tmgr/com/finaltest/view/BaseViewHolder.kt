package app.innwaze.tmgr.com.finaltest.view

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.View

open class BaseViewHolder<out DB : ViewDataBinding>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding: DB? = DataBindingUtil.bind(itemView)
}