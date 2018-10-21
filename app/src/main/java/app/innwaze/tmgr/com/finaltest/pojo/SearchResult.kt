package app.innwaze.tmgr.com.finaltest.pojo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.HashSet

@Parcelize
data class SearchResult(var id: Long = 0,
                        var url: String? = null,
                        var searchWord: String? = null,
                        var matches: Int? = 0,
                        var linksSet: HashSet<String>? = HashSet(),
                        var executionResultStatus: Int? = 0 ) : Parcelable {

    companion object {
        var FINISHED_FLAG = 1
        var ERROR_FLAG = 2
    }

}
