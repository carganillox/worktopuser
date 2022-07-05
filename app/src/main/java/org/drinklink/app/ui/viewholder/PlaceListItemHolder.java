package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.model.Place;

import butterknife.BindView;



public class PlaceListItemHolder extends ViewModelClickableHolder<Place> {

    private static final RequestOptions GLIDE_OPTIONS = new RequestOptions().centerCrop()
            .placeholder(R.drawable.place_placeholder);

    @BindView(R.id.list_item)
    FrameLayout container;
    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.lbl_city)
    TextView city;
    @BindView(R.id.logo_place)
    ImageView logoPlace;

    public PlaceListItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, Place item) {
        super.bind(ctx, position, item);

        setClickListenerWithTag(container, item, onClick);
        name.setText(item.getName());
        String address = nonNull(item.getAddress());
        city.setText(address);
        load(item.getCoverImageUrl()).apply(GLIDE_OPTIONS).into(logoPlace);
    }

    private String nonNull(String s) {
        return s == null ? "" : s;
    }

    public static int getLayout() {
        return R.layout.list_item_place;
    }
}
