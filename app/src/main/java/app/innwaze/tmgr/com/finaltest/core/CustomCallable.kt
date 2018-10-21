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

class CustomCallable(private val searchResult: SearchResult) : Callable<SearchResult> {

    private lateinit var mCustomThreadPoolManagerWeakReference: WeakReference<CustomThreadPoolManager>

    @Throws(Exception::class)
    override fun call(): SearchResult? {
//        val searchResult = SearchResult()
//        searchResult.id = id
//        searchResult.url = url
//        searchResult.searchWord = searchWord
        searchResult.executionResultStatus = SearchResult.RUNNING_FLAG
        sendMessage(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))

        var message: Message? = null
        val regex = StringBuilder()
                .append("\\b")
                .append(searchResult.searchWord)
                .append("\\b")
                .toString()

        try {
            // check if thread is interrupted before lengthy operation
            if (Thread.interrupted()) throw InterruptedException()

            val doc = Jsoup.connect(searchResult.url).get()
            val links = doc.select("a[href]")
            val wordMatches = doc.getElementsMatchingOwnText(regex)
            val linksSet = HashSet<String>()

            searchResult.matches = wordMatches.size
            searchResult.linksSet = getLinksSet(links, linksSet)
            searchResult.executionResultStatus = SearchResult.FINISHED_FLAG
            sendMessage(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))
        } catch (e: Exception) {

            searchResult.executionResultStatus = SearchResult.ERROR_FLAG
            sendMessage(Util.createMessage(Util.MESSAGE_SEARCH_RESULT_ID, searchResult))

            Log.e("Unconnected ", searchResult.url)
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
