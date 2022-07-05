package org.drinklink.app.common.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;

import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.utils.TimeSignature;
import org.drinklink.app.utils.Logger;

import butterknife.ButterKnife;

/**
 *
 */

public class ViewModelBaseHolder<T> extends RecyclerView.ViewHolder {

    private static final String TAG = "ViewModelBaseHolder";

    private int position;
    protected T item;
    protected Context ctx;

    public ViewModelBaseHolder(View itemView){
        super(itemView);
        try {
            ButterKnife.bind(this, itemView);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            throw e;
        }
    }

    public void onViewRecycled() {

    }

    protected RequestBuilder<Drawable> load(String url) {
        return DependencyResolver.getComponent().getGlideLoader().with().load(url).signature(new TimeSignature());
    }

    public void bind(Context ctx, int position, T item) {
        this.position = position;
        //static view, has no data to bind to
        bind(ctx, item);
    }

    public void bind(Context ctx, T item) {
        unBind();
        this.item = item;
        this.ctx = ctx;
        //static view, has no data to bind to
    }

    public void unBind() {
    }

    public void reBind() {
        bind(ctx, position, item);
    }

    public void comparePredecessorItem(T currentViewModelItem, T predecessorViewModelItem) {

    }

    public T getItem() {
        return item;
    }

    protected void setVisibility(View view, boolean isVisible) {
        if (view == null) {
            return;
        }
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    protected void setText(TextView view, String text) {
        if (view == null) {
            return;
        }
        view.setText(text);
    }

    protected void setVisibility(boolean isVisible, View... views) {
        for (View view : views) {
            setVisibility(view, isVisible);
        }
    }

    protected void setButtonLink(Button btn, String btnText, String btnLink, View.OnClickListener onClick) {
        setVisibility(btn, btnText != null);
        setClickListenerWithTag(btn, btnLink, onClick);
        btn.setTag(btnLink);
        btn.setText(btnText);
    }

    protected void setClickListenerWithTag(Object item, View.OnClickListener click) {
        setClickListenerWithTag(itemView, item, click);
    }

    protected void setClickListenerWithTag(View itemView, Object item, View.OnClickListener click) {
        itemView.setOnClickListener(click);
        itemView.setTag(item);
    }

    protected void setClickListenerWithHolder(View itemView, View.OnClickListener click) {
        itemView.setOnClickListener(click);
        itemView.setTag(this);
    }

    protected void setLongClickListenerWithTag(Object item, View.OnLongClickListener click) {
        setLongClickListenerWithTag(itemView, item, click);
    }

    protected void setLongClickListenerWithTag(View itemView, Object item, View.OnLongClickListener click) {
        itemView.setOnLongClickListener(click);
        itemView.setTag(item);
    }

//    protected void setLongClickListenerPositionedWithTag(Object item, View.OnLongClickListener click, int Position) {
//        setLongClickListenerPositionedWithTag(itemView, item, click, Position);
//    }

//    protected void setLongClickListenerPositionedWithTag(View itemView, Object item, View.OnLongClickListener click, int Position) {
//        itemView.setOnLongClickListener(click);
//        itemView.setTag(R.integer.tag_object, item);
//        itemView.setTag(R.integer.tag_position, Position);
//    }

//    protected void setClickListenerPositionedWithTag(Object item, View.OnClickListener click, int Position) {
//        setClickListenerPositionedWithTag(itemView, item, click, Position);
//    }

//    protected void setClickListenerPositionedWithTag(View itemView, Object item, View.OnClickListener click, int Position) {
//        itemView.setOnClickListener(click);
//        itemView.setTag(R.integer.tag_object, item);
//        itemView.setTag(R.integer.tag_position, Position);
//    }

    public static int sizeToDp(Context ctx, int height) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, ctx.getResources().getDisplayMetrics());
    }

    public interface MessageShow {
        void show(String show);
    }

    protected void setBackground(View layout, @DrawableRes int drawableRes) {
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackgroundDrawable(ContextCompat.getDrawable(ctx, drawableRes));
        } else {
            layout.setBackground(ContextCompat.getDrawable(ctx, drawableRes));
        }
    }
}
