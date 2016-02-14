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

public class rocket_fast extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_game);
        setContentView(new Draw(this.getApplicationContext()));
        Log.i("Muse", "onCreate: ");

    }

    public class Draw extends View{

        private int x = 0, y = 0;
        private final int NORTH = 0,
                EAST = 1, SOUTH = 2, WEST = 3;
        private int facing = 0;
        private Bitmap rocket;
        private int score = 0;
        private int timeLeft = 15;
        private boolean over = false;
        private int sample = 0;

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
                    if(timeLeft == 0)
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
                    int i = 0;
                    while(!over) {
                        Log.i("GEY","AFD");
                        Draw.this.postInvalidate();
                        if(i % 200 == 0)
                            sample = MainActivity.x_axis;
                        ++i;
                        try {
                            this.wait(200);
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

            rocket = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.rocket);
            Log.i("Muse","Now: "+Math.abs(MainActivity.x_axis-sample) + " " + MainActivity.x_axis + " " + sample);
            if(Math.abs(MainActivity.x_axis-sample) > 1)
            {
                int w = canvas.getWidth(), h = canvas.getHeight();

                int turning = (int) (Math.random() * 2);
                if (turning == 0) {
                    facing = (int) (Math.random() * 4);
                }

                if (facing == EAST)
                    x += rocket.getWidth();
                else if (facing == WEST)
                    x -= rocket.getWidth();
                else if (facing == SOUTH)
                    y += rocket.getWidth();
                else if (facing == NORTH)
                    y -= rocket.getWidth();
                //headed east and hit wall
                if (x + rocket.getWidth() > w) {
                    x -= rocket.getWidth();
                    facing = SOUTH;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    rocket = Bitmap.createBitmap(rocket, 0, 0, rocket.getWidth(), rocket.getHeight());
                }
                //headed west and hit wall
                else if (x - rocket.getWidth() < 0) {
                    x += rocket.getWidth();
                    facing = NORTH;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    rocket = Bitmap.createBitmap(rocket, 0, 0, rocket.getWidth(), rocket.getHeight());
                }

                //heading south and hit wall
                if (y + rocket.getHeight() > h) {
                    y -= rocket.getHeight();
                    facing = WEST;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    rocket = Bitmap.createBitmap(rocket, 0, 0, rocket.getWidth(), rocket.getHeight());
                } else if (y - rocket.getHeight() < 0) {
                    y += rocket.getHeight();
                    facing = EAST;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    rocket = Bitmap.createBitmap(rocket, 0, 0, rocket.getWidth(), rocket.getHeight());
                }
                score++;
            }
//            Log.i("Muse","Canvas: "+Math.abs(MainActivity.x_axis-sample));

            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStrokeWidth(5);
            p.setTextSize(50f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            Rect rect = new Rect();
            p.getTextBounds("Time Left: " + timeLeft,0,("Time Left: " + timeLeft).length(),rect);
            canvas.drawText("Time Left: " + timeLeft, (canvas.getWidth() / 2) - (rect.width() / 2), canvas.getHeight() / 3, p);
            p.getTextBounds("Score: " + score, 0, ("Score: " + score).length(), rect);
            canvas.drawText("Score: " + score,(canvas.getWidth()/2)-(rect.width()/2),canvas.getHeight()/4, p);
            canvas.drawBitmap(rocket,x,y,null);
        }
    }
}
