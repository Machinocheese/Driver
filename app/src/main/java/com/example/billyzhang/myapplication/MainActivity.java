package com.example.billyzhang.myapplication;

import android.animation.ValueAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity{

    /* my variable declarations. If it seems messy, it's because
    I programmed as I went along.
     */
    int height, width;
    int driverHeight, driverWidth;
    int bgHeight, bgWidth;
    int bgX = 0, numCollisions = 0;
    int counter = 0, current_offset = 200, statusBarOffset;
    int rockHeight, rockWidth;
    float currentPosY;
    int originalPos[];

    private ImageView driver;
    private GestureDetector gestureDetector;
    private TextView tv;
    ImageView background;

    private Handler handler = new Handler();
    //this runnable controls the placement of the asteroids and starts moving them
    private Runnable runnable = new Runnable(){
        @Override
        public void run(){
            final Random rand = new Random();
            int diceRoll = rand.nextInt(height) + 1;
            ImageView lol = new ImageView(getApplicationContext());
            lol.setBackgroundResource(R.drawable.rock);
            lol.setX(width);
            lol.setY(diceRoll);
            moveHorizontal(lol, width, diceRoll);
            ConstraintLayout xd = (ConstraintLayout)findViewById(R.id.layout);
            xd.addView(lol);
            handler.postDelayed(runnable, 1000);
        }
    };
    //this runnable scrolls the background
    private Runnable controlbg = new Runnable(){
        @Override
        public void run(){
            updateBackground();
            handler.postDelayed(controlbg, 50);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.btn1);
        driver = (ImageView)findViewById(R.id.driver);
        driver.setBackgroundResource(R.drawable.rocket);

        AnimationDrawable frameAnimation = (AnimationDrawable) driver.getBackground();
        frameAnimation.start();

        tv = (TextView)findViewById(R.id.txt);
        tv.setX(500);

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        btn.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override public boolean onTouch(View v, MotionEvent event) {
                if(gestureDetector.onTouchEvent(event)){
                    //player goes in opposite direction after a single tap
                    current_offset = -1 * current_offset;
                }
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50); //this handler controls the startup speed
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        int position[] = getPoints(driver);
                        moveView(position[1], originalPos[1] - position[1], true);
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override public void run() {
                    moveView(getPoints(driver)[1], current_offset, false);
                    mHandler.postDelayed(this, 500); //this controls the 'update' rate
                }
            };

        });
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //this is here to ensure that gesture detector detects single taps
            return true;
        }
    }

    private int[] getPoints(ImageView view){
        int position[] = new int[2];
        view.getLocationOnScreen(position);
        return position;
    }

    //this method solely controls asteroid movement, as the player only moves up/down
    private void moveHorizontal(final ImageView view, final int positionX, final int positionY){
        final ValueAnimator animator = ValueAnimator.ofFloat(1.0f, -0.2f);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(5000L);

        //the image moves as the animation value updates
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();

                view.setX(progress * positionX);
                if(collisionTest(view) || progress <= -0.2f){
                    ConstraintLayout xd = (ConstraintLayout)findViewById(R.id.layout);
                    xd.removeView(view);
                }
            }
        });
        animator.start();

    }

    //checks if the driver and any asteroids are in collision
    private boolean collisionTest(ImageView view){
        int rockPos[] = getPoints(view);
        int driverPos[] = getPoints(driver);
        if(rockPos[0] <= driverPos[0] + driverWidth){
            int top = driverPos[1] - rockHeight;
            int bottom = driverPos[1] + driverHeight;
            if(rockPos[1] > top && rockPos[1] < bottom){
                numCollisions++;
                tv.setText("Collisions: " + numCollisions);
                return true;
            }
        }
        return false;
    }

    private void updateBackground(){
        bgX -= 10;
        if(bgX < -(bgWidth - width))
            bgX = 0;
        background.setX(bgX);
    }

    /*a makeshift method I used to get around the fact that onCreate doesn't fully load
    in the right values for certain elements */
    private void initialize(){
        originalPos = getPoints(driver);
        ConstraintLayout xd = (ConstraintLayout)findViewById(R.id.layout);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );
        statusBarOffset = dm.heightPixels - xd.getMeasuredHeight();
        height = dm.heightPixels;
        width = dm.widthPixels;
        driverHeight = driver.getMeasuredHeight();
        driverWidth = driver.getMeasuredWidth();
        Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.rock);
        rockHeight = d.getIntrinsicHeight();
        rockWidth = d.getIntrinsicWidth();

        background = new ImageView(getApplicationContext());
        background.setBackgroundResource(R.drawable.samespace);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(width * 2, height);
        background.setLayoutParams(layoutParams);
        d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.samespace);
        bgHeight = d.getIntrinsicHeight();
        bgWidth = d.getIntrinsicWidth();
        xd.addView(background);

        Button btn = (Button)findViewById(R.id.btn1);
        btn.setX(width - btn.getWidth());
        btn.setY(height - btn.getHeight() - statusBarOffset);
        handler.post(runnable);
        handler.post(controlbg);
    }

    //essentially a vertical moveHorizontal method
    private void moveView(final int origin, final int offset, boolean returning){
        if(counter == 0){
            initialize();
            counter = 1;
        }
        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        //animator.setRepeatCount(ValueAnimator.INFINITE);
        if(!returning){
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(500L);
        }
        else{
            animator.setDuration(1000L);
        }

        final int real = origin - statusBarOffset;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();

                float calculated = real + offset * progress;
                driver.setY(calculated);
                currentPosY = calculated + statusBarOffset;
                System.out.println(currentPosY);
            }
        });
        animator.start();

    }
}



