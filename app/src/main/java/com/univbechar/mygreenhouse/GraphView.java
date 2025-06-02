package com.univbechar.mygreenhouse;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {

    private Paint paint;
    private Paint textPaint;
    private Path path;
    private float[] percentages = {20, 40, 30, 50, 60, 80, 70}; // Example data
    private String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Days of the week

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float lineWidth = (float) width / (percentages.length - 1);

        path.reset(); // Clear previous path
        path.moveTo(0, height - (height * (percentages[0] / 100))); // Move to the first point

        // Draw the path for the graph
        for (int i = 1; i < percentages.length; i++) {
            float x = i * lineWidth;
            float y = height - (height * (percentages[i] / 100));
            path.lineTo(x, y);
        }

        // Draw the path
        canvas.drawPath(path, paint);

        // Draw labels for the x-axis (days of the week)
        for (int i = 0; i < days.length; i++) {
            float x = i * lineWidth;
            canvas.drawText(days[i], x, height - 10, textPaint);
        }
    }
}
