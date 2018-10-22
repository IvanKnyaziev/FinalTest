package app.innwaze.tmgr.com.finaltest.ui

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import app.innwaze.tmgr.com.finaltest.R
import app.innwaze.tmgr.com.finaltest.databinding.ResultItemBinding
import app.innwaze.tmgr.com.finaltest.pojo.CustomError
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult
import app.innwaze.tmgr.com.finaltest.view.BaseRVAdapter
import app.innwaze.tmgr.com.finaltest.view.BaseViewHolder

class ResultsRVAdapter : BaseRVAdapter<SearchResult, ResultItemBinding, ResultsRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsRVAdapter.ViewHolder {
        return ViewHolder(inflate(parent, R.layout.result_item))
    }

    override fun getItem(position: Int): SearchResult {
        return getItems()[position]
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.result_item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (!payloads.isEmpty()){
            if (payloads[0] is SearchResult){
                val searchResult = payloads[0] as SearchResult
                holder.binding!!.tvSearchWord.text = holder.binding.root.context.resources.getString(R.string.matches_result, searchResult.searchWord, searchResult.matches)
                setTextColor(holder.binding, searchResult)
                setError(holder.binding, searchResult)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val result = getItem(position)
        binding!!.result = result
        setTextColor(binding, result)
        setError(binding, result)
    }

    private fun setError(binding: ResultItemBinding, result: SearchResult){
        val context = binding.root.context
        if (result.customError.code == CustomError.EMPTY) {
            binding.tvError.text = context.getString(R.string.no_info)
            binding.tvError.setTextColor(Color.BLACK)
        }
        if (result.customError.code == CustomError.NO_ERROR) {
            binding.tvError.text = context.getString(R.string.no_errors)
            binding.tvError.setTextColor(Color.GREEN)
        } else if (!result.customError.message.isEmpty()) {
            binding.tvError.text = context.getString(R.string.item_error_message, result.customError.message, result.customError.code)
            binding.tvError.setTextColor(Color.RED)
        }
    }

    private fun setTextColor(binding: ResultItemBinding, result: SearchResult) {
        when (result.executionResultStatus){
            SearchResult.FINISHED_FLAG -> {
                binding.tvUrl.setTextColor(Color.GREEN)
            }
            SearchResult.ERROR_FLAG ->{
                binding.tvUrl.setTextColor(Color.RED)
            }
            SearchResult.CREATED_FLAG ->{
                binding.tvUrl.setTextColor(Color.BLUE)
            }
            SearchResult.RUNNING_FLAG ->{
                binding.tvUrl.setTextColor(Color.YELLOW)
            }
        }
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder<ResultItemBinding>(itemView)

}
