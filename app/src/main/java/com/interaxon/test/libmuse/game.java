package com.interaxon.test.libmuse;

import android.os.Bundle;
import android.app.Activity;
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

public class game extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_game);
        setContentView(new Draw(this.getApplicationContext()));
    }

    public class Draw extends View{

        private int x = 0, y = 0;

        public Draw(Context context)
        {
            super(context);
            this.setBackgroundColor(Color.WHITE);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            Bitmap tank = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.tank);
            canvas.drawBitmap(tank,x,y,null);
        }
    }
}
