package com.example.sei.deeplinkserver.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class SaveIntentView extends View {
    public int width,height;
    private Paint mPaint;
    private float lastX,lastY;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    public SaveIntentView(Context context) {
        this(context,null);
    }

    public SaveIntentView(Context context,  AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public SaveIntentView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);

        width = 75;
        height = 75;
    }

    public void setLayoutParams(WindowManager.LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(width/2,height/2,width/2,mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(width/2,height/2,(float) (width*1/0.4),mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();

                break;
            case MotionEvent.ACTION_MOVE:
                float dx=0,dy=0;
                dx = event.getRawX() - lastX;
                dy = event.getRawY() - lastY;
                layoutParams.x+=dx;
                layoutParams.y+=dy;
                showView();
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
//        super.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void showView(){
        windowManager.updateViewLayout(this,layoutParams);
    }
}
