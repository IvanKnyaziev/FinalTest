package app.innwaze.tmgr.com.finaltest.util.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.TextView;

import app.innwaze.tmgr.com.finaltest.R;
import app.innwaze.tmgr.com.finaltest.pojo.CustomError;
import app.innwaze.tmgr.com.finaltest.pojo.SearchResult;

public class DataBindingAdapters {

    @BindingAdapter("resultMatches")
    public static void resultMatches(@NonNull TextView view, @NonNull SearchResult results){
        if (results == null) return;
        Context context = view.getContext();
        view.setText(context.getResources().getString(R.string.matches_result, results.getSearchWord(), results.getMatches()));
    }

    @BindingAdapter("errorStatus")
    public static void errorStatus(@NonNull TextView view, @NonNull SearchResult results){
        if (results == null) return;
        Context context = view.getContext();
        if (results.getCustomError().getCode() == CustomError.EMPTY){
            view.setText(R.string.no_info);
        }
        if (results.getCustomError().getCode() == CustomError.NO_ERROR){
            view.setText(R.string.no_errors);
            view.setTextColor(Color.GREEN);
        } else {
            view.setText(context.getResources().getString(R.string.item_error_message, results.getCustomError().getMessage(), results.getCustomError().getCode()));
            view.setTextColor(Color.RED);
        }
    }
}
