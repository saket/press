/*
 * Copyright 2018 Google LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package press.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import press.widgets.PorterDuffColorFilterWrapper;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static press.animation.InterpolatorsKt.linearOutSlowInInterpolator;

/**
 * Copied from Plaid.
 * https://github.com/android/plaid/blob/master/core/src/main/java/io/plaidapp/core/ui/transitions/FabTransform.java
 *
 * A transition between a FAB & another surface using a circular reveal moving along an arc.
 * See: https://www.google.com/design/spec/motion/transforming-material.html#transforming-material-radial-transformation
 */
public class FabTransform extends Transition {

  public static final String SHARED_ELEMENT_TRANSITION_NAME = "sharedElement:FabTransform";
  public static final long ANIM_DURATION_MILLIS = 240L;
  private static final String EXTRA_FAB_COLOR = "EXTRA_FAB_COLOR";
  private static final String EXTRA_FAB_ICON_RES_ID = "EXTRA_FAB_ICON_RES_ID";
  private static final String EXTRA_FAB_ICON_TINT = "EXTRA_FAB_ICON_TINT";
  private static final String PROP_BOUNDS = "plaid:fabTransform:bounds";
  private static final String[] TRANSITION_PROPERTIES = {
      PROP_BOUNDS
  };

  private final int color;
  private final int icon;
  private final int iconTint;

  private FabTransform(@ColorInt int fabColor, @DrawableRes int fabIconResId, int fabIconTint) {
    color = fabColor;
    icon = fabIconResId;
    iconTint = fabIconTint;
    setPathMotion(new GravityArcMotion());
    setDuration(ANIM_DURATION_MILLIS);
  }

  public static ActivityOptions createOptions(
      @NonNull Activity activity,
      @NonNull Intent intent,
      @NonNull FloatingActionButton fab,
      @DrawableRes int fabIconResId)
  {
    //noinspection ConstantConditions
    int fabColor = fab.getBackgroundTintList().getDefaultColor();
    int fabIconTint = ((PorterDuffColorFilterWrapper) fab.getColorFilter()).getColor();

    fab.setTransitionName(SHARED_ELEMENT_TRANSITION_NAME);

    intent.putExtra(EXTRA_FAB_COLOR, fabColor);
    intent.putExtra(EXTRA_FAB_ICON_RES_ID, fabIconResId);
    intent.putExtra(EXTRA_FAB_ICON_TINT, fabIconTint);

    return ActivityOptions.makeSceneTransitionAnimation(
        activity,
        fab,
        SHARED_ELEMENT_TRANSITION_NAME
    );
  }

  /**
   * Create a {@link FabTransform} from the supplied {@code activity} extras and set as its
   * shared element enter/return transition.
   */
  public static void applyActivityTransition(@NonNull Activity activity, @NonNull View target) {
    final Intent intent = activity.getIntent();
    if (!hasActivityTransition(activity)) {
      throw new AssertionError();
    }

    target.setTransitionName(SHARED_ELEMENT_TRANSITION_NAME);

    final int color = intent.getIntExtra(EXTRA_FAB_COLOR, Color.TRANSPARENT);
    final int icon = intent.getIntExtra(EXTRA_FAB_ICON_RES_ID, -1);
    final int iconTint = intent.getIntExtra(EXTRA_FAB_ICON_TINT, -1);
    final FabTransform sharedEnter = new FabTransform(color, icon, iconTint);
    sharedEnter.addTarget(target);
    activity.getWindow().setSharedElementEnterTransition(sharedEnter);
  }

    public static boolean hasActivityTransition(Activity activity) {
    return activity.getIntent().hasExtra(EXTRA_FAB_COLOR)
        && activity.getIntent().hasExtra(EXTRA_FAB_ICON_RES_ID)
        && activity.getIntent().hasExtra(EXTRA_FAB_ICON_TINT);
  }

  @Override
  public String[] getTransitionProperties() {
    return TRANSITION_PROPERTIES;
  }

  @Override
  public void captureStartValues(TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  @Override
  public void captureEndValues(TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  @Override
  public Animator createAnimator(final ViewGroup sceneRoot,
      final TransitionValues startValues,
      final TransitionValues endValues) {
    if (startValues == null || endValues == null)  return null;

    final Rect startBounds = (Rect) startValues.values.get(PROP_BOUNDS);
    final Rect endBounds = (Rect) endValues.values.get(PROP_BOUNDS);

    final boolean fromFab = endBounds.width() > startBounds.width();
    final View view = endValues.view;
    final Rect dialogBounds = fromFab ? endBounds : startBounds;
    final Rect fabBounds = fromFab ? startBounds : endBounds;
    final Interpolator fastOutSlowInInterpolator =
        new FastOutSlowInInterpolator();
    final long duration = getDuration();
    final long halfDuration = duration / 2;
    final long twoThirdsDuration = duration * 2 / 3;

    if (!fromFab) {
      // Force measure / layout the dialog back to it's original bounds
      view.measure(
          makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY),
          makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY));
      view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom);
    }

    final int translationX = startBounds.centerX() - endBounds.centerX();
    final int translationY = startBounds.centerY() - endBounds.centerY();
    if (fromFab) {
      view.setTranslationX(translationX);
      view.setTranslationY(translationY);
    }

    // Add a color overlay to fake appearance of the FAB
    final ColorDrawable fabColor = new ColorDrawable(color);
    fabColor.setBounds(0, 0, dialogBounds.width(), dialogBounds.height());
    if (!fromFab) fabColor.setAlpha(0);
    view.getOverlay().add(fabColor);

    // Add an icon overlay again to fake the appearance of the FAB
    final Drawable fabIcon =
        AppCompatResources.getDrawable(sceneRoot.getContext(), icon).mutate();
    fabIcon.setTint(iconTint);
    final int iconLeft = (dialogBounds.width() - fabIcon.getIntrinsicWidth()) / 2;
    final int iconTop = (dialogBounds.height() - fabIcon.getIntrinsicHeight()) / 2;
    fabIcon.setBounds(iconLeft, iconTop,
        iconLeft + fabIcon.getIntrinsicWidth(),
        iconTop + fabIcon.getIntrinsicHeight());
    if (!fromFab) fabIcon.setAlpha(0);
    view.getOverlay().add(fabIcon);

    // Circular clip from/to the FAB size
    final Animator circularReveal;
    if (fromFab) {
      circularReveal = ViewAnimationUtils.createCircularReveal(view,
          view.getWidth() / 2,
          view.getHeight() / 2,
          startBounds.width() / 2,
          (float) Math.hypot(endBounds.width() / 2, endBounds.height() / 2));
      circularReveal.setInterpolator(
          new FastOutLinearInInterpolator());
    } else {
      circularReveal = ViewAnimationUtils.createCircularReveal(view,
          view.getWidth() / 2,
          view.getHeight() / 2,
          (float) Math.hypot(startBounds.width() / 2, startBounds.height() / 2),
          endBounds.width() / 2);
      circularReveal.setInterpolator(
          linearOutSlowInInterpolator(sceneRoot.getContext()));

      // Persist the end clip i.e. stay at FAB size after the reveal has run
      circularReveal.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              final int left = (view.getWidth() - fabBounds.width()) / 2;
              final int top = (view.getHeight() - fabBounds.height()) / 2;
              outline.setOval(
                  left, top, left + fabBounds.width(), top + fabBounds.height());
              view.setClipToOutline(true);
            }
          });
        }
      });
    }
    circularReveal.setDuration(duration);

    // Translate to end position along an arc
    final Animator translate = ObjectAnimator.ofFloat(
        view,
        View.TRANSLATION_X,
        View.TRANSLATION_Y,
        fromFab ? getPathMotion().getPath(translationX, translationY, 0, 0)
            : getPathMotion().getPath(0, 0, -translationX, -translationY));
    translate.setDuration(duration);
    translate.setInterpolator(fastOutSlowInInterpolator);

    // Fade contents of non-FAB view in/out
    List<Animator> fadeContents = null;
    if (view instanceof ViewGroup) {
      final ViewGroup vg = ((ViewGroup) view);
      fadeContents = new ArrayList<>(vg.getChildCount());
      for (int i = vg.getChildCount() - 1; i >= 0; i--) {
        final View child = vg.getChildAt(i);
        final Animator fade =
            ObjectAnimator.ofFloat(child, View.ALPHA, fromFab ? 1f : 0f);
        if (fromFab) {
          child.setAlpha(0f);
        }
        fade.setDuration(twoThirdsDuration);
        fade.setInterpolator(fastOutSlowInInterpolator);
        fadeContents.add(fade);
      }
    }

    // Fade in/out the fab color & icon overlays
    final Animator colorFade = ObjectAnimator.ofInt(fabColor, "alpha", fromFab ? 0 : 255);
    final Animator iconFade = ObjectAnimator.ofInt(fabIcon, "alpha", fromFab ? 0 : 255);
    if (!fromFab) {
      colorFade.setStartDelay(halfDuration);
      iconFade.setStartDelay(halfDuration);
    }
    colorFade.setDuration(halfDuration);
    iconFade.setDuration(halfDuration);
    colorFade.setInterpolator(fastOutSlowInInterpolator);
    iconFade.setInterpolator(fastOutSlowInInterpolator);

    // Work around issue with elevation shadows. At the end of the return transition the shared
    // element's shadow is drawn twice (by each activity) which is jarring. This workaround
    // still causes the shadow to snap, but it's better than seeing it double drawn.
    Animator elevation = null;
    if (!fromFab) {
      elevation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, -view.getElevation());
      elevation.setDuration(duration);
      elevation.setInterpolator(fastOutSlowInInterpolator);
    }

    // Run all animations together
    final AnimatorSet transition = new AnimatorSet();
    transition.playTogether(circularReveal, translate, colorFade, iconFade);
    transition.playTogether(fadeContents);
    if (elevation != null) transition.play(elevation);
    if (fromFab) {
      transition.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          // Clean up
          view.getOverlay().clear();
        }
      });
    }
    return new NoPauseAnimator(transition);
  }

  private void captureValues(TransitionValues transitionValues) {
    final View view = transitionValues.view;
    if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) return;

    transitionValues.values.put(PROP_BOUNDS, new Rect(view.getLeft(), view.getTop(),
        view.getRight(), view.getBottom()));
  }
}
