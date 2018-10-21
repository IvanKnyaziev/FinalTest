package app.innwaze.tmgr.com.finaltest.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomError(var message: String = "",
                       var code: Int = EMPTY) : Parcelable{

    companion object {
        const val EMPTY = 0
        const val NO_ERROR = 1
    }

}
