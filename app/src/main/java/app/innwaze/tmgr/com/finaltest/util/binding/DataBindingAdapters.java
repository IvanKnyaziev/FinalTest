package app.innwaze.tmgr.com.finaltest.util.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.widget.TextView;

import app.innwaze.tmgr.com.finaltest.R;
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult;

public class DataBindingAdapters {

    @BindingAdapter("resultMatches")
    public static void resultMatches(@NonNull TextView view, @NonNull SearchResult results){
        if (results == null) return;
        Context context = view.getContext();
        view.setText(context.getResources().getString(R.string.matches_result, results.getSearchWord(), results.getMatches()));
    }
}
