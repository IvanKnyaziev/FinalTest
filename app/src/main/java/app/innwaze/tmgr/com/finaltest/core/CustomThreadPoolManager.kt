package app.innwaze.tmgr.com.finaltest.core


import android.os.Message
import android.os.Process
import android.util.Log
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult
import app.innwaze.tmgr.com.finaltest.util.Util
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class CustomThreadPoolManager private constructor() {
    // Counter starts from 1 because first callable was created from Activity
    private val counter = AtomicInteger(1)
    private var maxRequest = 0
    private val mExecutorService: ExecutorService
    private val mTaskQueue: BlockingQueue<Runnable>
    private val mRunningTaskList: MutableList<Future<*>>
    private val linksQueue: BlockingQueue<String>

    private var uiThreadCallbackWeakReference: WeakReference<UiThreadCallback>? = null

    fun resetCounter() {
        counter.set(1)
    }

    fun setCorePoolSize(corePoolSize: Int) {
        if (corePoolSize > MAX_RECOMMENDED_THREAD_POOL_SIZE) {
            (mExecutorService as CustomThreadPoolExecutor).corePoolSize = MAX_RECOMMENDED_THREAD_POOL_SIZE
        } else {
            (mExecutorService as CustomThreadPoolExecutor).corePoolSize = corePoolSize
        }
    }

    fun setMaximumPoolSize(maximumPoolSize: Int) {
        if (maximumPoolSize > MAX_RECOMMENDED_THREAD_POOL_SIZE) {
            (mExecutorService as CustomThreadPoolExecutor).maximumPoolSize = MAX_RECOMMENDED_THREAD_POOL_SIZE
        } else {
            (mExecutorService as CustomThreadPoolExecutor).maximumPoolSize = maximumPoolSize
        }
    }

    fun setMaxRequests(maxRequests: Int) {
        this.maxRequest = maxRequests
    }

    init {
        mTaskQueue = LinkedBlockingQueue()
        mRunningTaskList = ArrayList()
        linksQueue = LinkedBlockingQueue()
        mExecutorService = CustomThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mTaskQueue,
                CustomThreadFactory())
    }

    fun addCallable(callable: Callable<*>) {
        val future = mExecutorService.submit(callable)
        mRunningTaskList.add(future)
    }

    fun cancelAllTasks() {
        synchronized(this) {
            mTaskQueue.clear()
            linksQueue.clear()
            counter.set(maxRequest)
            for (task in mRunningTaskList) {
                if (task != null && !task.isDone) {
                    task.cancel(true)
                }
            }
            mRunningTaskList.clear()
        }
        sendMessageToUI(Util.createMessage(Util.MESSAGE_STOP_ID, "Execution aborted, all tasks stopped"))
        resetExecutorLocks()
    }

    private fun resetExecutorLocks() {
        (mExecutorService as CustomThreadPoolExecutor).resume()
    }

    fun pauseResume() {
        if ((mExecutorService as CustomThreadPoolExecutor).isPaused) {
            sendMessageToUI(Util.createMessage(Util.MESSAGE_RESUMED_ID, "Pause"))
        } else {
            sendMessageToUI(Util.createMessage(Util.MESSAGE_PAUSED_ID, "Resume"))
        }
        mExecutorService.pauseResume()
    }

    fun setUiThreadCallback(uiThreadCallback: UiThreadCallback) {
        this.uiThreadCallbackWeakReference = WeakReference(uiThreadCallback)
    }

    fun addNewCallables(message: Message?) {
        val searchResult = message?.obj as SearchResult
        if (searchResult != null) {


            val (_, _, searchWord, _, linksSet) = message.obj as SearchResult
            linksQueue.addAll(linksSet!!)
            for (link in linksQueue) {
                if (counter.get() < maxRequest) {

                    val searchResult = SearchResult()
                    searchResult.id = counter.get().toLong()
                    searchResult.url = link
                    searchResult.searchWord = searchWord
                    searchResult.executionResultStatus = SearchResult.CREATED_FLAG

                    sendMessageToUI(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))

                    val callable = CustomCallable(searchResult)
                    Log.d("Link to parse", link)
                    callable.setCustomThreadPoolManager(this)
                    addCallable(callable)
                    counter.incrementAndGet()
                } else
                    break
            }
            searchResult.executionResultStatus = SearchResult.FINISHED_FLAG
            sendMessageToUI(message)
        }
    }

    fun sendMessageToUI(message: Message) {
        if (uiThreadCallbackWeakReference != null && uiThreadCallbackWeakReference!!.get() != null) {
            uiThreadCallbackWeakReference!!.get()!!.publishToUiThread(message)
        }
    }

    private class CustomThreadFactory : ThreadFactory {

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "CustomThread$sTag"
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND
            thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, ex -> Log.e(Util.LOG_TAG, thread.name + " encountered an error: " + ex.message) }
            return thread
        }

        companion object {
            private const val sTag = 1110
        }
    }

    companion object {
        private var sInstance: CustomThreadPoolManager? = null
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        private val MAX_RECOMMENDED_THREAD_POOL_SIZE = NUMBER_OF_CORES * 2
        private const val KEEP_ALIVE_TIME = 5L
        private val KEEP_ALIVE_TIME_UNIT: TimeUnit = TimeUnit.SECONDS

        init {
            sInstance = CustomThreadPoolManager()
        }

        fun getsInstance(): CustomThreadPoolManager? {
            return sInstance
        }
    }
}
