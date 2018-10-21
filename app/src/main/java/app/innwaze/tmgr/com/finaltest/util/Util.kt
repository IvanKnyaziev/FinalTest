package app.innwaze.tmgr.com.finaltest.util

import android.os.Bundle
import android.os.Message
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult

object Util {
    val LOG_TAG = "BackgroundThread"
    val MESSAGE_STOP_ID = 1
    val MESSAGE_SEARCH_RESULT_ID = 2
    val MESSAGE_PAUSED_ID = 3
    val MESSAGE_RESUMED_ID = 4
    val MESSAGE_TEXT = "MESSAGE_TEXT"
    val EMPTY_MESSAGE = "<EMPTY_MESSAGE>"

    fun createMessage(id: Int, dataString: String): Message {
        val bundle = Bundle()
        bundle.putString(Util.MESSAGE_TEXT, dataString)
        val message = Message()
        message.what = id
        message.data = bundle
        return message
    }

    fun createMessage(id: Int, searchResult: SearchResult): Message {
        val message = Message()
        message.what = id
        message.obj = searchResult
        return message
    }

}