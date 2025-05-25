package com.hospital.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;

public class ViewUtils {

    public static void rippleRoundStroke(final View _view, final String _focus, final  String _pressed, final double _round, final double _stroke, final String _strokeColor){
        GradientDrawable GG = new GradientDrawable();
        GG.setColor(Color.parseColor(_focus));
        GG.setCornerRadius((float) _round);
        GG.setStroke((int) _stroke,
                Color.parseColor("#" + _strokeColor.replace("#", "")));
        RippleDrawable RE = new RippleDrawable(new ColorStateList(new int[][]{
                new int[]{

                }
        }, new int[]{
                Color.parseColor(_pressed)
        }), GG, null);
        _view.setBackground(RE);
    }

}
