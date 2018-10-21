package app.innwaze.tmgr.com.finaltest

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class FinalTestApp : Application(){

    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this)
    }

}
