package app.innwaze.tmgr.com.finaltest.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import app.innwaze.tmgr.com.finaltest.R
import app.innwaze.tmgr.com.finaltest.core.CustomCallable
import app.innwaze.tmgr.com.finaltest.core.CustomThreadPoolManager
import app.innwaze.tmgr.com.finaltest.core.UiThreadCallback
import app.innwaze.tmgr.com.finaltest.databinding.ActivityMainBinding
import app.innwaze.tmgr.com.finaltest.pojo.EnterValues
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult
import app.innwaze.tmgr.com.finaltest.util.Util
import app.innwaze.tmgr.com.finaltest.view.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference


class MainActivity : BaseActivity<ActivityMainBinding>(), UiThreadCallback, MainActivityContract.View {

    private lateinit var mUiHandler: UiHandler
    private lateinit var adapter: ResultsRVAdapter
    private lateinit var presenter: MainActivityPresenter
    private lateinit var mCustomThreadPoolManager: CustomThreadPoolManager

    override fun publishToUiThread(message: Message) {
        if (mUiHandler != null) {
            mUiHandler.sendMessage(message)
        }
    }

    override fun provideLayoutRes(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.enterValues = EnterValues()
//        if (BuildConfig.DEBUG) {
//            binding.enterValues!!.url = "https://www.wikipedia.org/"
//            binding.enterValues!!.maxThreads = 4
//            binding.enterValues!!.searchWord = "Wikipedia"
//            binding.enterValues!!.maxUrls = 100
//        }
        presenter = MainActivityPresenter(this)
        adapter = ResultsRVAdapter()
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.addItemDecoration(DividerItemDecoration(rvResults.context, LinearLayoutManager.VERTICAL))
        binding.rvResults.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        mUiHandler = UiHandler(Looper.getMainLooper(),
                binding.tvResult,
                adapter,
                binding.pbCounter,
                binding.btnPauseResume,
                binding.tvStatus,
                binding.btnStart)
        mCustomThreadPoolManager = CustomThreadPoolManager.getsInstance()!!
        mCustomThreadPoolManager.setUiThreadCallback(this)
    }

    override fun showUrlError() {
        resetErrors()
        binding.edUrl.error = getString(R.string.invalid_url_error_message)
    }

    override fun showMaxThreadsError() {
        resetErrors()
        binding.edMaxThreads.error = getString(R.string.max_threads_error_message)
    }

    override fun showSearchError() {
        resetErrors()
        binding.edSearch.error = getString(R.string.search_error_message)
    }

    override fun showMaxUrlsError() {
        resetErrors()
        binding.edMaxUrls.error = getString(R.string.max_urls_error_message)
    }

    override fun startExecution() {
        resetErrors()
        initProgressBar()
        setupThreadPoolExecutor()
        startParsing()
    }

    fun tryStartExecution(view: View) {
        presenter.validateEnterValues(binding.enterValues!!)
    }

    fun pauseResumeExecution(view: View) {
        mCustomThreadPoolManager.pauseResume()
    }

    fun cancelAllTasksInThreadPool(view: View) {
        mCustomThreadPoolManager.cancelAllTasks()
    }

    private fun setupThreadPoolExecutor() {
        mCustomThreadPoolManager.setCorePoolSize(binding.enterValues!!.maxThreads)
        mCustomThreadPoolManager.setMaximumPoolSize(binding.enterValues!!.maxThreads)
        mCustomThreadPoolManager.setMaxRequests(binding.enterValues!!.maxUrls)
    }

    private fun startParsing() {
        setStatusTextRunning()
        clearDisplay()
        lockStartButton()
        mCustomThreadPoolManager.resetCounter()
        val searchResult = SearchResult()
        searchResult.id = 0L
        searchResult.url = binding.enterValues!!.url
        searchResult.searchWord = binding.enterValues!!.searchWord
        searchResult.executionResultStatus = SearchResult.CREATED_FLAG
        adapter.add(searchResult)
        val callable = CustomCallable(searchResult)
        callable.setCustomThreadPoolManager(mCustomThreadPoolManager)
        mCustomThreadPoolManager.addCallable(callable)
    }

    private fun lockStartButton() {
        binding.btnStart.isEnabled = false
        binding.btnStart.setBackgroundColor(Color.GRAY)
    }

    private fun unlockStartButton() {
        binding.btnStart.isEnabled = true
    }

    private fun setStatusTextRunning(){
        binding.tvStatus.text = "Running"
    }

    private fun clearDisplay() {
        binding.tvResult.text = ""
        adapter.clear()
    }

    private fun initProgressBar() {
        binding.pbCounter.progress = 0
        binding.pbCounter.max = binding.enterValues!!.maxUrls
    }

    private fun resetProgressBar() {
        binding.pbCounter.progress = 0
    }

    private fun resetErrors() {
        binding.edUrl.error = ""
        binding.edMaxThreads.error = ""
        binding.edSearch.error = ""
        binding.edMaxUrls.error = ""
    }

    private class UiHandler(looper: Looper,
                            display: TextView,
                            rvAdapter: ResultsRVAdapter,
                            progressBar: ProgressBar,
                            pauseResumeButton: Button,
                            status: TextView,
                            startButton: Button) : Handler(looper) {
        private val mWeakRefProgressStatusDisplay: WeakReference<TextView>?
        private val mWeakRefStatusDisplay: WeakReference<TextView>?
        private val mWeakRefPauseResumeButton: WeakReference<Button>?
        private val mWeakRefRVAdapter: WeakReference<ResultsRVAdapter>?
        private val mWeakRefProgressBar: WeakReference<ProgressBar>?
        private val mWeakRefStartButton: WeakReference<Button>?

        init {
            this.mWeakRefProgressStatusDisplay = WeakReference(display)
            this.mWeakRefStatusDisplay = WeakReference(status)
            this.mWeakRefPauseResumeButton = WeakReference(pauseResumeButton)
            this.mWeakRefRVAdapter = WeakReference(rvAdapter)
            this.mWeakRefProgressBar = WeakReference(progressBar)
            this.mWeakRefStartButton = WeakReference(startButton)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                Util.MESSAGE_SEARCH_RESULT_ID -> {
                    val result = msg.obj as SearchResult
                    when (result.executionResultStatus){
                            SearchResult.CREATED_FLAG -> {
                                if (mWeakRefRVAdapter?.get() != null) mWeakRefRVAdapter.get()!!.add(result)
                            }
                            SearchResult.RUNNING_FLAG -> {
                                if (mWeakRefRVAdapter?.get() != null) mWeakRefRVAdapter.get()!!.notifyItemChanged(mWeakRefRVAdapter.get()!!.getItemPosition(result) ,result)
                            }
                            SearchResult.FINISHED_FLAG, SearchResult.ERROR_FLAG -> {
                                if (mWeakRefRVAdapter?.get() != null) mWeakRefRVAdapter.get()!!.notifyItemChanged(mWeakRefRVAdapter.get()!!.getItemPosition(result) ,result)
                                if (mWeakRefProgressBar?.get() != null){
                                    setProgressAndColor(result)
                                    // no much time to do good workaround in blocking start button
                                    if (mWeakRefStartButton?.get() != null) {
                                        if (mWeakRefProgressBar.get()!!.progress == mWeakRefProgressBar.get()!!.max) {
                                            mWeakRefStartButton.get()!!.isEnabled = true
                                            mWeakRefStartButton.get()!!.setBackgroundColor(Color.parseColor("#008577"))
                                        }
                                    }
                                }
                            }
                    }
                }
                Util.MESSAGE_STOP_ID -> {
                    val bundle = msg.data
                    val messageText = bundle.getString(Util.MESSAGE_TEXT, Util.EMPTY_MESSAGE)
                    if (mWeakRefStatusDisplay?.get() != null)
                        mWeakRefStatusDisplay.get()!!.text = messageText
                }
                Util.MESSAGE_PAUSED_ID -> {
                    val bundle = msg.data
                    val messageText = bundle.getString(Util.MESSAGE_TEXT, Util.EMPTY_MESSAGE)
                    if (mWeakRefPauseResumeButton?.get() != null)mWeakRefPauseResumeButton.get()!!.text = messageText
                    if (mWeakRefStatusDisplay?.get() != null)mWeakRefStatusDisplay.get()!!.text = "Paused"
                }
                Util.MESSAGE_RESUMED_ID -> {
                    val bundle = msg.data
                    val messageText = bundle.getString(Util.MESSAGE_TEXT, Util.EMPTY_MESSAGE)
                    if (mWeakRefPauseResumeButton?.get() != null)mWeakRefPauseResumeButton.get()!!.text = messageText
                    if (mWeakRefStatusDisplay?.get() != null)mWeakRefStatusDisplay.get()!!.text = "Running"
                }
            }
        }

        private fun setProgressAndColor(result: SearchResult) {
            mWeakRefProgressBar!!.get()!!.progress = mWeakRefProgressBar.get()!!.progress.plus(1)
            val status = StringBuilder()
                    .append(mWeakRefProgressBar.get()!!.progress)
                    .append(" of ")
                    .append(mWeakRefProgressBar.get()!!.max)
            mWeakRefProgressStatusDisplay!!.get()!!.text = status
            val percentage = Math.round(((mWeakRefProgressBar.get()!!.progress * 100 / mWeakRefProgressBar.get()!!.max).toDouble()))
            when {
                percentage <= 33 -> mWeakRefProgressBar.get()?.progressDrawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                percentage <= 67 -> mWeakRefProgressBar.get()?.progressDrawable?.setColorFilter(Color.parseColor("#FFFF9F00"), PorterDuff.Mode.SRC_IN)
                else -> mWeakRefProgressBar.get()?.progressDrawable?.setColorFilter(Color.parseColor("#FF6CCC02"), PorterDuff.Mode.SRC_IN)
            }
        }
    }
}
