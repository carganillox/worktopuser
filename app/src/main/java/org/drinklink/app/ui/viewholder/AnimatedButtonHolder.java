/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;

import butterknife.BindView;

public class AnimatedButtonHolder extends ViewModelBaseHolder<Boolean> {

    private static final boolean SET_VISIBILITY = true;

    @BindView(R.id.animation_overlay)
    View overlay;

    @BindView(R.id.animation_background)
    View background;

    AlphaAnimation animation;

    public AnimatedButtonHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, Boolean animate) {
        super.bind(ctx, position, animate);
        setButtons(animate);
    }

    public void setButtons(boolean animate) {
        if (!animate) {
            overlay.setAlpha(0);
            setOverlayVisibility(false);
            if (animation != null) {
                animation.cancel();
                animation = null;
            }
            return;
        }
        // animation already started
        if (animation != null) {
            return;
        }
        setOverlayVisibility(true);
        overlay.setAlpha(1);
        createAnimation();
        overlay.startAnimation(animation);
    }

    private void setOverlayVisibility(boolean isVisible) {
        if (SET_VISIBILITY) {
            setVisibility(overlay, isVisible);
            setVisibility(background, !isVisible);
        }
    }

    private void createAnimation() {
        animation = new AlphaAnimation(1, 0.2f); // Change alpha from fully visible to invisible
        animation.setDuration(1000); // duration
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
    }
}
