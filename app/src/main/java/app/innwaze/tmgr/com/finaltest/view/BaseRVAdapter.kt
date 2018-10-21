package app.innwaze.tmgr.com.finaltest.view


import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

abstract class BaseRVAdapter<M, out DB : ViewDataBinding, VH : BaseViewHolder<DB>> : RecyclerView.Adapter<VH>() {
    private val items = ArrayList<M>()

    val isEmpty: Boolean
        get() = items.isEmpty()

    protected fun inflate(parent: ViewGroup, @LayoutRes layoutRes: Int): View {
        return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    }

    fun updateItems(newItems: List<M>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun remove(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun remove(item: M) {
        val position = getItemPosition(item)
        if (position != -1)
            remove(position)
    }

    fun replace(index: Int, item: M) {
        items[index] = item
        notifyItemChanged(index)
    }

    fun getItemPosition(item: M): Int {
        return items.indexOf(item)
    }

    fun add(item: M) {
        val oldSize = items.size
        items.add(item)
        notifyItemInserted(oldSize)
    }

    fun add(newItems: List<M>?) {
        if (newItems != null) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    fun notifyItemChanged(item: M) {
        notifyItemChanged(getItemPosition(item))
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    open fun getItem(position: Int): M? {
        return if (position >= itemCount) null else items[position]
    }

    fun getItems(): List<M> {
        return items
    }
}
