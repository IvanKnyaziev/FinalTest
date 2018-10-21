package app.innwaze.tmgr.com.finaltest.core

import android.os.Message

interface UiThreadCallback {
    fun publishToUiThread(message: Message)
}
