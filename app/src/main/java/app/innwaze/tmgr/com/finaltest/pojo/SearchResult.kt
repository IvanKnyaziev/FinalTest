package app.innwaze.tmgr.com.finaltest.pojo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class SearchResult(var id: Long = 0,
                        var url: String? = null,
                        var searchWord: String? = null,
                        var matches: Int? = 0,
                        var linksSet: HashSet<String>? = HashSet(),
                        var executionResultStatus: Int? = 0 ) : Parcelable {

    companion object {
        const val CREATED_FLAG = 1
        const val RUNNING_FLAG = 2
        const val FINISHED_FLAG = 3
        const val ERROR_FLAG = 4
    }

}
