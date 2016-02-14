package com.interaxon.test.libmuse;

import android.os.Bundle;
import android.app.Activity;
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

public class game extends Activity {

    GameView gameView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        gameView = new GameView(this);
//        setContentView(gameView);
    }
 
    class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder holder;
        volatile boolean playing;
 
        Canvas canvas;
        Paint paint;
 
        long fps;
        // This is used to help calculate the fps
        private long timeThisFrame;
 
        Bitmap tank;
        boolean isMoving = false;
        float walkSpeedPerSecond = 250;
        // Start position
        float tankXPosition = 10;
 
        public GameView(Context context) {
            super(context);
 
            holder = getHolder();
            paint = new Paint();
 
            // Load Bob from his .png file
            tank = BitmapFactory.decodeFile('Tank.png');
        }
 
        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                update();
                draw();
 
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
 
            }
 
        }
 
        // Everything that needs to be updated goes in here
        // In later projects we will have dozens (arrays) of objects.
        // We will also do other things like collision detection.
        public void update() {
            if(isMoving){
                tankXPosition = MainActivity.DataListener.x_pos;
            }
        }
 
        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
 
                canvas.drawColor(Color.argb(255, 26, 128, 182));
 
                paint.setColor(Color.argb(255,  249, 129, 0));

                paint.setTextSize(45);
 
                canvas.drawText("FPS:" + fps, 20, 40, paint);
 
                whereToDraw.set((int)tankXPosition, 0,
                        (int)tankXPosition + frameWidth,
                        frameHeight);
                getCurrentFrame();

                canvas.drawBitmap(tank,
                        frameToDraw,
                        whereToDraw, paint);
                holder.unlockCanvasAndPost(canvas);
            }
 
        }
 
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
 
        }
 
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
 
        @Override
//        public boolean onTouchEvent(MotionEvent motionEvent) {
// 
//            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
// 
//                // Player has touched the screen
//                case MotionEvent.ACTION_DOWN:
// 
//                    // Set isMoving so Bob is moved in the update method
//                    isMoving = true;
// 
//                    break;
// 
//                // Player has removed finger from screen
//                case MotionEvent.ACTION_UP:
// 
//                    // Set isMoving so Bob does not move
//                    isMoving = false;
// 
//                    break;
//            }
//            return true;
//        }
 
    }
    
    @Override
    protected void onResume() {
        super.onResume();
 
        gameView.resume();
    }
 
    @Override
    protected void onPause() {
        super.onPause();
 
        gameView.pause();
    }
 
}
