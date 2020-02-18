package com.example.herd.ui.comments_viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PostView extends View {

    public PostView(Context context) {
        super(context);
    }

    public PostView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path rect = new Path();
        rect.addRect(0, 0,250, 150, Path.Direction.CW);
        Paint paint = new Paint();
        canvas.drawPath(rect, paint);
    }
}
