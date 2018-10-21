package app.innwaze.tmgr.com.finaltest.ui

import android.webkit.URLUtil
import app.innwaze.tmgr.com.finaltest.pojo.EnterValues

class MainActivityPresenter(private val mainActivityContract: MainActivityContract.View): MainActivityContract.Presenter {

    override fun validateEnterValues(enterValues: EnterValues) {
        if (!URLUtil.isNetworkUrl(enterValues.url)) {
            mainActivityContract.showUrlError()
            return
        }
        if (enterValues.maxThreads < 1) {
            mainActivityContract.showMaxThreadsError()
            return
        }
        if (enterValues.searchWord.isEmpty()) {
            mainActivityContract.showSearchError()
            return
        }
        if (enterValues.maxUrls < 1) {
            mainActivityContract.showMaxUrlsError()
            return
        }
        mainActivityContract.startExecution()
    }

}