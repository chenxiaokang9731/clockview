package com.bright.com.train1010;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Calendar;

/**
 * Created by chenxiaokang on 2016/10/10.
 */
public class ClockView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    /**
     * 默认半径长度
     */
    private final static int DEFAULT_LENGTH = 200;
    /**
     * 控制线程是否循环标志
     */
    private boolean flag;
    /**
     * 分钟，秒钟刻度长度
     */
    private int mMinuteCalibrationLength;
    private int mHourCalibrationLength;
    /**
     *  时分秒刻度长度
     */
    private int mSecondPointLength;
    private int mMinutePointLength;
    private int mHourPointLength;

    private SurfaceHolder mHolder;
    /**
     * 半径长度
     */
    private int mRadius = DEFAULT_LENGTH;
    /**
     * 画布宽高
     */
    private int mCanvasWidth, mCanvasHeight;
    /**
     * 存放时间的值
     */
    private int mHour, mMinute, mSecond;
    /**
     * 圆画笔
     */
    private Paint mPaint;
    /**
     * 文字画笔
     */
    private Paint mTextPaint;
    /**
     * 指针画笔
     */
    private Paint mPointPaint;
    /**
     * 画布
     */
    private Canvas mCanvas;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHolder = getHolder();
        mHolder.addCallback(this);

        mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        mMinute = Calendar.getInstance().get(Calendar.MINUTE);
        mSecond = Calendar.getInstance().get(Calendar.SECOND) - 1;

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setColor(Color.BLACK);
    }

    /**
     * surfaceView创建
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        flag = true;
        new Thread(this).start();
    }

    /**
     * surfaceView改变
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * surfaceView销毁
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

    @Override
    public void run() {
        while (flag){
            long startTime = System.currentTimeMillis();
            calculateTime();    //计算时间
            draw();             //画布画钟表
            long endTime = System.currentTimeMillis();

            //休眠足够1s钟
            if(endTime-startTime<1000){
                SystemClock.sleep(1000 - (endTime - startTime));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int svWidthLength =  MeasureSpec.getSize(widthMeasureSpec);
        int svWidthMode  =   MeasureSpec.getMode(widthMeasureSpec);
        int svHeightLength = MeasureSpec.getSize(heightMeasureSpec);
        int svHeightMode =   MeasureSpec.getMode(heightMeasureSpec);

        int desireWidth, desireHeight;

        //宽度测量
        if(svWidthMode == MeasureSpec.EXACTLY){
            desireWidth = svWidthLength;
        }else {
            desireWidth = mRadius*2 + getPaddingLeft() + getPaddingRight();
            if(svWidthMode == MeasureSpec.AT_MOST){
                desireWidth = Math.min(desireWidth, svWidthLength);
            }
        }

        //高度测量
        if(svHeightMode == MeasureSpec.EXACTLY){
            desireHeight = svHeightLength;
        }else {
            desireHeight = mRadius*2 + getPaddingTop() + getPaddingBottom();
            if(svHeightMode == MeasureSpec.AT_MOST){
                desireHeight = Math.min(desireHeight, svHeightLength);
            }
        }

        setMeasuredDimension(mCanvasWidth = desireWidth+4, mCanvasHeight = desireHeight+4);

        //计算测量后的半径长度
        mRadius = (int) (Math.min(desireWidth - getPaddingLeft() - getPaddingRight(), desireHeight - getPaddingTop() - getPaddingBottom())*1.0f/2);
        //计算刻度
        calculateLength();
    }

    /**
     *  画钟表
     */
    private void draw(){

        try {
            mCanvas = mHolder.lockCanvas();
            if(mCanvas != null){
                mCanvas.drawColor(Color.WHITE);
                //移动画布到控件中心
                mCanvas.translate(mCanvasWidth*1.0f/2+getPaddingLeft()-getPaddingRight(),
                                  mCanvasHeight*1.0f/2+getPaddingTop()-getPaddingBottom());
                //绘制圆盘
                mPaint.setStrokeWidth(2f);
                mCanvas.drawCircle(0,0,mRadius, mPaint);
                //绘制时刻度
                for(int i = 0; i<12; i++){
                    mCanvas.drawLine(0, mRadius, 0, mRadius-mHourCalibrationLength, mPaint);
                    mCanvas.rotate(30);
                }
                //绘制分刻度
                for(int i = 0; i<60; i++){
                    if( i%5 != 0 ){
                        mCanvas.drawLine(0, mRadius, 0, mRadius-mMinuteCalibrationLength, mPaint);
                    }
                    mCanvas.rotate(6);
                }
                //绘制数字
                for(int i = 0; i<12; i++){
                    String number = "";
                    if(i<=6){
                        number = String.valueOf(i+6);
                    }else if(i > 6){
                        number = String.valueOf(i-6);
                    }
                    mCanvas.drawText(number, 0, mRadius*5.8f*1.0f/7, mTextPaint);
                    mCanvas.rotate(30);
                }
                //绘制上下午
                mCanvas.drawText(mHour<12?"AM":"PM", 0, mRadius*1.0f*2/4, mTextPaint);
                //绘制秒针
                mPointPaint.setColor(Color.CYAN);
                Path path = new Path();
                path.moveTo(0,0);
                int[] secondPointerCor = getPointerCoordinates(mSecondPointLength);
                path.lineTo(secondPointerCor[0], secondPointerCor[1]);
                path.lineTo(secondPointerCor[2], secondPointerCor[3]);
                path.lineTo(secondPointerCor[4], secondPointerCor[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mSecond * 6);
                mCanvas.drawPath(path, mPointPaint);
                mCanvas.restore();
                //绘制分针
                mPointPaint.setColor(Color.GRAY);
                path.reset();
                path.moveTo(0,0);
                int[] minutePointerCor = getPointerCoordinates(mMinutePointLength);
                path.lineTo(minutePointerCor[0], minutePointerCor[1]);
                path.lineTo(minutePointerCor[2], minutePointerCor[3]);
                path.lineTo(minutePointerCor[4], minutePointerCor[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mMinute*6+mSecond*1.0f/60 * 6);
                mCanvas.drawPath(path, mPointPaint);
                mCanvas.restore();
                //绘制时针
                mPointPaint.setColor(Color.RED);
                path.reset();
                path.moveTo(0,0);
                int[] hourPointerCor = getPointerCoordinates(mHourPointLength);
                path.lineTo(hourPointerCor[0], hourPointerCor[1]);
                path.lineTo(hourPointerCor[2], hourPointerCor[3]);
                path.lineTo(hourPointerCor[4], hourPointerCor[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mHour*30 + mMinute*1.0f/60*30);
                mCanvas.drawPath(path, mPointPaint);
                mCanvas.restore();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     *  获取指针三个点的坐标
     */
    private int[] getPointerCoordinates(int pointerLength) {

        int pointerAngle = 5;

        int y = (int) (pointerLength*3.0f / 4);
        int x = (int) (pointerLength * Math.tan(Math.PI/180*pointerAngle));

        return new int[]{-x, y, 0, pointerLength, x, y};
    }

    /**
     * 计算时间
     */
    private void calculateTime(){

        mSecond++;
        if(mSecond == 60){
            mSecond = 0;
            mMinute++;
            if(mMinute == 60){
                mMinute = 0;
                mHour++;
                if(mHour == 24){
                    mHour= 0;
                }
            }
        }

    }

    /**
     * 计算指针和刻度长度
     */
    private void calculateLength() {

        // hour:min 刻度 2:1
        mHourCalibrationLength = (int) (mRadius*1.0f / 7);
        mMinuteCalibrationLength = (int) (mHourCalibrationLength*1.0f / 2);

        // hour:min:second 指针长度  1.5:1.25:1
        mHourPointLength = (int) (mRadius*1.0 / 2);
        mMinutePointLength = (int) (mHourPointLength * 1.25f);
        mSecondPointLength = (int) (mHourPointLength * 1.5f);
    }

    /**
     * 设置时间
     * @param time
     */
    public void setTime(int ...time){
        if(time.length>0){
            mHour = time[0];
        }
        if(time.length>1){
            mMinute = time[1];
        }
        if (time.length>2){
            mSecond = time[2]-1;
        }
    }
}
