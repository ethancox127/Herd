package com.example.herd.ui.comments_viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.herd.R;

public class PostView extends LinearLayout {

    public PostView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.post_view, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path rect = new Path();
        rect.addRect(0, 0,250, 150, Path.Direction.CW);
        Paint paint = new Paint();
        canvas.drawPath(rect, paint);
    }
}
