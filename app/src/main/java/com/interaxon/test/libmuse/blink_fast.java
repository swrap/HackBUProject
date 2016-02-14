package com.interaxon.test.libmuse;

import android.graphics.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;

public class blink_fast extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_game);
        setContentView(new Draw(this.getApplicationContext()));
    }

    public class Draw extends View{

        private int x = 0, y = 0;
        private final int NORTH = 0,
        EAST = 1, SOUTH = 2, WEST = 3;
        private int facing = 0;
        private Bitmap tank;
        private int score = 0;
        private int timeLeft = 15;
        private boolean over = false;

        public Draw(Context context)
        {
            super(context);
            this.setBackgroundColor(Color.WHITE);
            CountDownTimer countDownTimer = new CountDownTimer(20000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    score++;
                    if(timeLeft > 0)
                        timeLeft--;
                    else
                        over = true;
                }

                @Override
                public void onFinish() {
                    over = true;
                }
            };
            countDownTimer.start();
            new Thread(new Runnable()
            {
                public void run()
                {
                    while(!over) {
                        Draw.this.postInvalidate();
                        try {
                            this.wait(20);
                        } catch (Exception e) {

                        }
                    }
                }
            }).start();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            tank = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.tank);

            if(MainActivity.eyesOpen)// || MainActivity.x_axis < threashold)
            {
                int w = canvas.getWidth(), h = canvas.getHeight();

                int turning = (int) (Math.random() * 2);
                if (turning == 0) {
                    facing = (int) (Math.random() * 4);
                }

                if (facing == EAST)
                    x += tank.getWidth();
                else if (facing == WEST)
                    x -= tank.getWidth();
                else if (facing == SOUTH)
                    y += tank.getWidth();
                else if (facing == NORTH)
                    y -= tank.getWidth();
                //headed east and hit wall
                if (x + tank.getWidth() > w) {
                    x -= tank.getWidth();
                    facing = SOUTH;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    tank = Bitmap.createBitmap(tank, 0, 0, tank.getWidth(), tank.getHeight());
                }
                //headed west and hit wall
                else if (x - tank.getWidth() < 0) {
                    x += tank.getWidth();
                    facing = NORTH;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    tank = Bitmap.createBitmap(tank, 0, 0, tank.getWidth(), tank.getHeight());
                }

                //heading south and hit wall
                if (y + tank.getHeight() > h) {
                    y -= tank.getHeight();
                    facing = WEST;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    tank = Bitmap.createBitmap(tank, 0, 0, tank.getWidth(), tank.getHeight());
                } else if (y - tank.getHeight() < 0) {
                    y += tank.getHeight();
                    facing = EAST;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    tank = Bitmap.createBitmap(tank, 0, 0, tank.getWidth(), tank.getHeight());
                }
                score++;
            }
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStrokeWidth(5);
            p.setTextSize(50f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            Rect rect = new Rect();
            p.getTextBounds("Time Left: " + timeLeft,0,("Time Left: " + timeLeft).length(),rect);
            canvas.drawText("Time Left: " + timeLeft,(canvas.getWidth()/2)-(rect.width()/2), canvas.getHeight() / 3, p);
            p.getTextBounds("Score: " + score, 0, ("Score: " + score).length(), rect);
            canvas.drawText("Score: " + score,(canvas.getWidth()/2)-(rect.width()/2),canvas.getHeight()/4, p);
            canvas.drawBitmap(tank,x,y,null);
        }
    }
}
