package app.innwaze.tmgr.com.finaltest.core

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class CustomThreadPoolExecutor(corePoolSize: Int,
                               maximumPoolSize: Int,
                               keepAliveTime: Long,
                               unit: TimeUnit,
                               workQueue: BlockingQueue<Runnable>,
                               threadFactory: ThreadFactory) :
        ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory) {

    var isPaused: Boolean = false
    private val pauseLock = ReentrantLock()
    private val unpaused = pauseLock.newCondition()

    override fun beforeExecute(t: Thread, r: Runnable) {
        super.beforeExecute(t, r)
        pauseLock.lock()
        try {
            while (isPaused) unpaused.await()
        } catch (ie: InterruptedException) {
            t.interrupt()
        } finally {
            pauseLock.unlock()
        }
    }

    fun pauseResume() {
        if (isPaused) {
            resume()
        } else {
            pause()
        }
    }

    private fun pause() {
        pauseLock.lock()
        try {
            isPaused = true
        } finally {
            pauseLock.unlock()
        }
    }

    fun resume() {
        pauseLock.lock()
        try {
            isPaused = false
            unpaused.signalAll()
        } finally {
            pauseLock.unlock()
        }
    }
}