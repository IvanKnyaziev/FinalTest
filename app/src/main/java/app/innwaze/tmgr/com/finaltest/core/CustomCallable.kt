package app.innwaze.tmgr.com.finaltest.core

import android.os.Message
import android.util.Log
import android.webkit.URLUtil
import app.innwaze.tmgr.com.finaltest.pojo.CustomError
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult
import app.innwaze.tmgr.com.finaltest.util.Util
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.select.Elements
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Callable
import javax.net.ssl.SSLException

class CustomCallable(private val searchResult: SearchResult) : Callable<SearchResult> {

    private lateinit var mCustomThreadPoolManagerWeakReference: WeakReference<CustomThreadPoolManager>

    @Throws(Exception::class)
    override fun call(): SearchResult? {
        val regex = StringBuilder()
                .append("\\b")
                .append(searchResult.searchWord)
                .append("\\b")
                .toString()

        try {
            // check if thread is interrupted before lengthy operation
            if (Thread.interrupted()) throw InterruptedException()

//            searchResult.executionResultStatus = SearchResult.RUNNING_FLAG
//            mCustomThreadPoolManagerWeakReference.get()!!.sendMessageToUI(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))

            val doc = Jsoup.connect(searchResult.url).get()
            val links = doc.select("a[href]")
            val wordMatches = doc.getElementsMatchingOwnText(regex)
            val linksSet = HashSet<String>()

            searchResult.matches = wordMatches.size
            searchResult.linksSet = getLinksSet(links, linksSet)
            searchResult.customError.code = CustomError.NO_ERROR
            sendMessage(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))
        } catch (e: Exception) {
            setError(e)
            Log.e("Unconnected ", searchResult.url)
            e.printStackTrace()
        }
        return null
    }

    private fun setError(e: Exception) {
        if (e is HttpStatusException) {
            searchResult.customError.code = e.statusCode
            searchResult.customError.message = e.message!!
        }
        if (e is SSLException) {
            searchResult.customError.code = 2
            searchResult.customError.message = e.message!!
        }
        if (e is UnsupportedMimeTypeException) {
            searchResult.customError.code = 2
            searchResult.customError.message = e.message!!
        }
        searchResult.executionResultStatus = SearchResult.ERROR_FLAG
        mCustomThreadPoolManagerWeakReference.get()!!.sendMessageToUI(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))
    }

    private fun getLinksSet(links: Elements, linksSet: HashSet<String>): HashSet<String> {
        for (link in links) {
            val tempLink = link.attr("href")
            if (URLUtil.isNetworkUrl(tempLink) && !tempLink.matches("^.*\\.pdf$".toRegex())) {
                linksSet.add(tempLink)
            }
        }
        return linksSet
    }

    private fun sendMessage(message: Message) {
        if (mCustomThreadPoolManagerWeakReference != null && mCustomThreadPoolManagerWeakReference.get() != null) {
            mCustomThreadPoolManagerWeakReference.get()!!.addNewCallables(message)
        }
    }

    fun setCustomThreadPoolManager(customThreadPoolManager: CustomThreadPoolManager) {
        this.mCustomThreadPoolManagerWeakReference = WeakReference(customThreadPoolManager)
    }
}
