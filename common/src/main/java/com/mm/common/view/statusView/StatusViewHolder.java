package com.mm.common.view.statusView;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class StatusViewHolder {

    private final SparseArray<View> views;
    private final View convertView;

    private StatusViewHolder(View view) {
        convertView = view;
        views = new SparseArray<>();
    }

    public static StatusViewHolder create(View view) {
        return new StatusViewHolder(view);
    }

    public <T extends View> T getView(int viewId) {
        View view = views.get(viewId);
        if (view == null) {
            view = convertView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (T) view;
    }

    public View getConvertView() {
        return convertView;
    }

    public void setText(int viewId, CharSequence text) {
        TextView textView = getView(viewId);
        textView.setText(text);
    }

    public void setText(int viewId, int textId) {
        TextView textView = getView(viewId);
        textView.setText(textId);
    }

    public void setTextColor(int viewId, int colorId) {
        TextView textView = getView(viewId);
        textView.setTextColor(colorId);
    }

    public void setTextSize(int viewId, int size) {
        TextView textView = getView(viewId);
        textView.setTextSize(size);
    }

    public void setOnClickListener(int viewId, View.OnClickListener clickListener) {
        View view = getView(viewId);
        view.setOnClickListener(clickListener);
    }

    public void setImageResource(int viewId, int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
    }

    public void setImageLottie(int viewId, String fileName) {
        LottieAnimationView imageView = getView(viewId);
        imageView.setAnimation(fileName);
    }


    public void setBackgroundResource(int viewId, int resId) {
        View view = getView(viewId);
        view.setBackgroundResource(resId);
    }

    public void setBackgroundColor(int viewId, int colorId) {
        View view = getView(viewId);
        view.setBackgroundColor(colorId);
    }

    public void setBackgroundDrawable(int viewId, Drawable drawable){
        View view = getView(viewId);
        view.setBackground(drawable);
    }
}
