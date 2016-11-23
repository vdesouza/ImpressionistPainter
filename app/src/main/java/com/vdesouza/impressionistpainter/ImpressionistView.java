package com.vdesouza.impressionistpainter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;


/**
 * Skeleton code created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private String TAG = "ImpressionistView";

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    public int _defaultRadius = 25;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private BrushEffect _brushEffect = BrushEffect.Defualt;
    private BrushColor _brushColor = BrushColor.Defualt;

    private double _maxSpeed = 3000.0;
    public float _maxBrushRadius = 50;
    public float _minBrushRadius = 5;

    private Random _random = new Random();

    // used for calculating brush size based on speed
    // modified from https://developer.android.com/training/gestures/movement.html
    private VelocityTracker mVelocityTracker = null;


    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);
        // set saved image background color
        this.setDrawingCacheBackgroundColor(Color.WHITE);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    // sets the brush effect
    public void setBrushEffect(BrushEffect brushEffect){
        _brushEffect = brushEffect;
    }

    // get the brush effect
    public BrushEffect getBrushEffect() {return _brushEffect;}

    // sets the brush color
    public void setBrushColor(BrushColor brushColor) {_brushColor = brushColor;}

    public String getBrushShapeName() {
        switch (_brushType) {
            case Circle:
                return "Circle";
            case Square:
                return "Square";
            case Spray:
                return "Spray";
            default:
                return "";
        }
    }
    public String getBrushColorName() {
        switch (_brushColor) {
            case Defualt:
                return "Normal";
            case BlackAndWhite:
                return "Grayscale";
            case Complementary:
                return "Complementary";
            default:
                return "";
        }
    }
    public String getBrushEffectName() {
        switch (_brushEffect) {
            case Defualt:
                return "None";
            case Velocity:
                return "Velocity";
            case Accelerometer:
                return "Accelerometer";
            default:
                return "";
        }
    }

    // sets _paint from outside of class
    public void setPaint(int color) {
        _paint.setColor(color);
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
            this.destroyDrawingCache();
            this.setDrawingCacheEnabled(true);
        }

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        // get current touch area and set color to color of matching pixel
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();
        int color = getColorAtTouch(Math.round(touchX), Math.round(touchY));
        _paint.setColor(color);

        // draw actions
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                switch (_brushEffect) {
                    case Defualt:
                        invalidate();
                        break;
                    case Velocity:
                        if (mVelocityTracker == null) {
                            // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                            mVelocityTracker = VelocityTracker.obtain();
                        } else {
                            // Reset the velocity tracker back to its initial state.
                            mVelocityTracker.clear();
                        }
                        // Add a user's movement to the tracker.
                        mVelocityTracker.addMovement(motionEvent);
                        invalidate();
                        break;
                    default:
                        break;
                }

            case MotionEvent.ACTION_MOVE:
                switch (_brushEffect) {
                    case Defualt:
                        // paints history motion so that space between movements is also filled in
                        // adapted from // modified from https://developer.android.com/training/gestures/movement.html
                        int historySize = motionEvent.getHistorySize();
                        for (int i = 0; i < historySize; i++) {
                            float touchHistoryX = motionEvent.getHistoricalX(i);
                            float touchHistoryY = motionEvent.getHistoricalY(i);

                            _paint.setColor(getColorAtTouch(Math.round(touchHistoryX), Math.round(touchHistoryY)));
                            drawImpressionistPainting(touchHistoryX, touchHistoryY, _defaultRadius);
                        }
                        drawImpressionistPainting(motionEvent.getX(), motionEvent.getY(), _defaultRadius);
                        break;
                    case Velocity:
                        mVelocityTracker.addMovement(motionEvent);
                        // determine the velocity
                        mVelocityTracker.computeCurrentVelocity(1000);
                        // retrieve the velocity
                        double xVelocity = mVelocityTracker.getXVelocity();
                        double yVelocity = mVelocityTracker.getYVelocity();
                        double velocity = Math.sqrt((Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2)));
                        if (velocity > _maxSpeed) {
                            velocity = _maxSpeed;
                        }
                        // set brush size based on speed, within limits
                        float velocityBrushSize = Math.round((_maxBrushRadius / 2 * velocity) / _maxSpeed);
                        if (velocityBrushSize > _maxBrushRadius) {
                            velocityBrushSize = _maxBrushRadius;
                        } else if (velocityBrushSize < _minBrushRadius) {
                            velocityBrushSize = _minBrushRadius;
                        }
                        // paints history motion so that space between movements is also filled in
                        // adapted from // modified from https://developer.android.com/training/gestures/movement.html
                        historySize = motionEvent.getHistorySize();
                        for (int i = 0; i < historySize; i++) {
                            float touchHistoryX = motionEvent.getHistoricalX(i);
                            float touchHistoryY = motionEvent.getHistoricalY(i);

                            _paint.setColor(getColorAtTouch(Math.round(touchHistoryX), Math.round(touchHistoryY)));
                            drawImpressionistPainting(touchHistoryX, touchHistoryY, velocityBrushSize);
                        }

                        Log.i(TAG, "(" + _maxBrushRadius + "*" + velocity + ") / " + _maxSpeed + " = " + Math.round((_maxBrushRadius * velocity) / _maxSpeed) + " ---- Brush size: " + velocityBrushSize);

                        drawImpressionistPainting(motionEvent.getX(), motionEvent.getY(), velocityBrushSize);
                        break;
                    default:
                        break;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                switch (_brushEffect) {
                    case Velocity:
                        // Return a VelocityTracker object back to be re-used by others.
                        mVelocityTracker.recycle();
                        break;
                    default:
                        break;
                }
        }

        return true;
    }

    public void drawImpressionistPainting(float x, float y, float brushSize) {
        switch (_brushType) {
            case Square:
                Log.i(TAG, "Canvas Width: " + _offScreenCanvas.getWidth() + " Canvas Height: " + _offScreenCanvas.getHeight() + "\n"
                + "position x: " + x + " position y: " + y);
                _offScreenCanvas.drawRect(x-brushSize, y-brushSize, x+brushSize, y+brushSize, _paint);
                break;
            case Circle:
                _offScreenCanvas.drawCircle(x, y, brushSize, _paint);
                break;
            case Spray:
                _offScreenCanvas.drawPoints(createSprayBrushPoints(x, y, brushSize), _paint);
                break;
        }

    }

    // gets color at pixel being touched, checks which color mode is set
    public int getColorAtTouch(int x, int y) {
        // modified from http://stackoverflow.com/a/14920800
        int pixel;
        try {
            pixel = ((BitmapDrawable)_imageView.getDrawable()).getBitmap().getPixel(x,y);
        } catch (Exception e) {
             pixel = Color.WHITE;
        }
        switch (_brushColor) {
            case Defualt:
                // return true color
                return pixel;
            case Complementary:
                // calculate the complementary color
                // adapted from http://serennu.com/colour/rgbtohsl.php
                // convert RGB to HSL
                float[] hsl = new float[3];
                ColorUtils.RGBToHSL(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsl);
                // get the inverse hue
                hsl[0] = (hsl[0] + 180) % 360;
                pixel = ColorUtils.HSLToColor(hsl);
                return pixel;
            case BlackAndWhite:
                hsl = new float[3];
                ColorUtils.RGBToHSL(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsl);
                // set saturation to 0
                hsl[1] = 0;
                pixel = ColorUtils.HSLToColor(hsl);
                return pixel;
            default:
                return pixel;
        }
    }

    // returns an array of points within a brush size to be drawn
    private float[] createSprayBrushPoints(float x, float y, float brushSize) {
        // get area of total brush
        double area = Math.pow(brushSize, 2.0) * Math.PI;
        // get 20% of area
        double percentage = (area/100) * 20;

        // fill an array with random points within 20% of the brush area
        float[] floatArray = new float[(int)percentage];
        for (int i = 0; i < (int)percentage - 1; i = i + 2) {
            Point point = CalculatePoint(x, y, brushSize);
            floatArray[i] = point.x;
            floatArray[i+1] = point.y;
        }
        return floatArray;
    }

    // gets a random point from within a circle, over time tends to center of circle
    private Point CalculatePoint(float x, float y, float brushSize) {
        // adapted from http://gamedev.stackexchange.com/a/26714
        double angle = _random.nextDouble() * Math.PI * 2;
        double radius = _random.nextDouble() * brushSize;
        double newX = x + radius * Math.cos(angle);
        double newY = y + radius * Math.sin(angle);
        return new Point((int)newX,(int)newY);
    }

    // saves the offscreenbitmap to the device's storage
    public void savePainting() {
        // saves image and displays toast confirmation
        String imgSaved = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), _offScreenBitmap,
                UUID.randomUUID().toString()+".png", "drawing");
        if(imgSaved!=null){
            Toast savedToast = Toast.makeText(getContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();
        }
        else{
            Toast unsavedToast = Toast.makeText(getContext(),
                    "Image could not be saved.", Toast.LENGTH_SHORT);
            unsavedToast.show();
        }
        this.destroyDrawingCache();
        this.setDrawingCacheEnabled(true);
    }

    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}


