package app.innwaze.tmgr.com.finaltest.core

import android.os.Message
import android.util.Log
import android.webkit.URLUtil
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.lang.ref.WeakReference
import java.util.HashSet
import java.util.concurrent.Callable
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult
import app.innwaze.tmgr.com.finaltest.util.Util

class CustomCallable(private val id: Long, private val url: String, private val searchWord: String) : Callable<SearchResult> {

    private lateinit var mCustomThreadPoolManagerWeakReference: WeakReference<CustomThreadPoolManager>

    @Throws(Exception::class)
    override fun call(): SearchResult? {
        val searchResult = SearchResult()
        searchResult.id = id
        searchResult.url = url
        searchResult.searchWord = searchWord
        var message: Message? = null
        val regex = StringBuilder()
                .append("\\b")
                .append(searchWord)
                .append("\\b")
                .toString()

        try {
            // check if thread is interrupted before lengthy operation
            if (Thread.interrupted()) throw InterruptedException()

            val doc = Jsoup.connect(url).get()
            val links = doc.select("a[href]")
            val wordMatches = doc.getElementsMatchingOwnText(regex)
            val linksSet = HashSet<String>()

            searchResult.matches = wordMatches.size
            searchResult.linksSet = getLinksSet(links, linksSet)
            searchResult.executionResultStatus = SearchResult.FINISHED_FLAG
            message = Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult)
            sendMessage(message)
        } catch (e: Exception) {

            searchResult.executionResultStatus = SearchResult.ERROR_FLAG
            message = Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult)
            sendMessage(message)

            Log.e("Unconnected ", url)
            e.printStackTrace()
        }
        return null
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
