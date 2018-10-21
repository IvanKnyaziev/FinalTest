package app.innwaze.tmgr.com.finaltest.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnterValues(var url: String = "",
                  var searchWord: String = "",
                  var maxThreads: Int = 0,
                  var maxUrls: Int = 0) : Parcelable
