package app.innwaze.tmgr.com.finaltest.ui

import app.innwaze.tmgr.com.finaltest.pojo.EnterValues

interface MainActivityContract {

    interface View {
        fun showUrlError()
        fun showMaxThreadsError()
        fun showSearchError()
        fun showMaxUrlsError()
        fun startExecution()
    }

    interface Presenter {
        fun validateEnterValues(enterValues: EnterValues)
    }

}
