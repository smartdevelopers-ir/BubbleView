package ir.smartdevelopers.popupview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class BubbleView extends AppCompatTextView {
    private Paint mRectanglePaint;
    //    private Paint mRectangleShadowPaint;
    private Paint mArrowPaint;
    //    private Paint mArrowShadowPaint;
    private Path mRectanglePath;
    //    private Path mRectangleShadowPath;
    private Path mArrowPath;
    //    private Path mArrowShadowPath;
    private int mArrowStartPointX;
    private RectF mRectangleRect;
    //    private RectF mRectangleShadowRect;
    private final int percent=20;
    private  float[] mRadius;
    private  float mTailHeight=10;
    private  float mTailWidth=10;
    private float mDefaultRadius=8;

    private boolean mShowing;
    private int mBackgroundColor=Color.BLUE;
    private View mTarget;
    private int mStartX;
    private static  int default8Padding;
    private static  int default16Padding;
    private OnLayoutChangeListener mOnLayoutChangeListener;

    public BubbleView(Context context) {
        this(context,null);
    }

    public BubbleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        default8Padding= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        default16Padding= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,16,getResources().getDisplayMetrics());
        mRectanglePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectanglePaint.setStyle(Paint.Style.FILL);

        mArrowPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Paint.Style.FILL);

        mRectanglePath=new Path();
//        mRectangleShadowPath=new Path();
        mArrowPath=new Path();
//        mArrowShadowPath=new Path();

        //setWillNotDraw(false);


        if (attrs!=null){
            TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.BubbleView);
            mBackgroundColor=typedArray.getColor(R.styleable.BubbleView_backgroundColor, Color.BLACK);
            mDefaultRadius=typedArray.getDimension(R.styleable.BubbleView_radius,10);
            mShowing=typedArray.getBoolean(R.styleable.BubbleView_show,true);
            mTailWidth=typedArray.getDimension(R.styleable.BubbleView_tailWidth,10);
            mTailHeight=typedArray.getDimension(R.styleable.BubbleView_tailHeight,10);
            if (mShowing){
                setVisibility(VISIBLE);
            }else {
                setVisibility(INVISIBLE);
            }

            typedArray.recycle();
        }
        setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
        mRadius=new float[8];
        for (int i=0;i<8;i++){
            mRadius[i]=mDefaultRadius;
        }
        mRectanglePaint.setColor(mBackgroundColor);
        mArrowPaint.setColor(mBackgroundColor);

    }


    public void show(@NonNull View target,@Nullable ViewGroup parent){
        if (mTarget!=null){
            mTarget.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        }
        target.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        mShowing=true;
        mTarget=target;
        setScaleX(0);
        setScaleY(0);
        setVisibility(VISIBLE);

       target.post(()->{

           ViewGroup targetRoot;
           if (parent==null){
                targetRoot= (ViewGroup) (target.getRootView().findViewById(android.R.id.content));
               if (targetRoot==null){
                   targetRoot=(ViewGroup) (target.getRootView());
               }
           }else {
               targetRoot=parent;
           }


           ViewGroup bubbleParent= (ViewGroup) getParent();
           if (bubbleParent!=null){
               bubbleParent.removeView(this);
           }
           rePosition(target,targetRoot);
           targetRoot.addView(this);
           targetRoot.bringChildToFront(this);

           animate().setDuration(300).scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator()).start();
           ViewGroup finalTargetRoot = targetRoot;
           mOnLayoutChangeListener=new OnLayoutChangeListener() {
               @Override
               public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                   rePosition(v, finalTargetRoot);
               }
           };

           target.addOnLayoutChangeListener(mOnLayoutChangeListener);
       });
    }

    private void rePosition(View target,ViewGroup targetRoot){
        Rect offsetViewBound=new Rect();
        target.getDrawingRect(offsetViewBound);
        targetRoot.offsetDescendantRectToMyCoords(target,offsetViewBound);
        int y=offsetViewBound.top-getMeasuredHeight();
        int tCenter=target.getMeasuredWidth()/2+offsetViewBound.left;
        int halfPopWidth=getMeasuredWidth()/2;
        int x=tCenter-halfPopWidth;
        if (x<0){
            x=0;
        }else if (tCenter+halfPopWidth>getResources().getDisplayMetrics().widthPixels){
            x=getResources().getDisplayMetrics().widthPixels - getMeasuredWidth();
        }
        mArrowStartPointX=tCenter-x;
        setPivotX(mArrowStartPointX);
        setPivotY(getMeasuredHeight());
        //setTop(0);
        setY(y);
        //setLeft(0);
        setX(x);
    }
    public void hide(){
        if (!mShowing){
            return;
        }
        mShowing=false;
        if (mTarget!=null && mOnLayoutChangeListener!=null){
            mTarget.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        }
        animate().setDuration(200).scaleY(0)
                .scaleX(0).
                setInterpolator(new AnticipateInterpolator())
                .withEndAction(()->{
                    setVisibility(INVISIBLE);
                    ViewGroup popParent= (ViewGroup) getParent();
                    if (popParent!=null){
                        popParent.removeView(this);
                    }
                })
                .start();

    }
    public void setTailHeight(float tailHeight) {
        mTailHeight = tailHeight;
    }

    public void setTailWidth(float tailWidth) {
        mTailWidth = tailWidth;
    }
    public void setColor(int color){
        mRectanglePaint.setColor(color);
        mArrowPaint.setColor(color);
        invalidate();
    }


    public boolean isShowing() {
        return mShowing;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectangleRect=new RectF(0,0,w,h-mTailHeight);
        if (mArrowStartPointX==0){
            mArrowStartPointX=w/2;
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        left=Math.max(default16Padding,left);
        right=Math.max(default16Padding,right);
        top=Math.max(default8Padding,top);
       bottom= (int) (Math.max(default8Padding,bottom)+mTailHeight);
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mArrowPath.reset();
        mRectanglePath.reset();

        mRectanglePath.addRoundRect(mRectangleRect,mRadius, Path.Direction.CW);
//        mRectangleShadowPath.addRoundRect(mRectangleShadowRect,mRadius, Path.Direction.CW);
//        canvas.drawPath(mRectangleShadowPath,mRectangleShadowPaint);
        canvas.drawPath(mRectanglePath,mRectanglePaint);

        int width=getMeasuredWidth();
        float startPointX;
        float shadowStartPointX;
        float startPointY= mRectangleRect.bottom;
//        float shadowStartPointY= mRectangleShadowRect.bottom;
        int p=width*percent/100;
        float xMove;
        float xArrowShadowMove;
        if (mArrowStartPointX > width-p && mArrowStartPointX< width){ // ----P-----C-----(w-P)--X-
            // draw arrow left
            startPointX=mArrowStartPointX-mTailWidth-mRadius[2*2];

            xMove=0;

        }else if (mArrowStartPointX>0 && mArrowStartPointX < p){// --x--P-----C-----P----
            // draw arrow right
            startPointX=mArrowStartPointX+mRadius[3*2];


            xMove=-mTailWidth;
        }else {
            // arrow stright arrow
            startPointX=mArrowStartPointX-(mTailWidth/2);
            xMove=-(mTailWidth/2);
        }
        mArrowPath.moveTo(startPointX,startPointY);
        mArrowPath.rLineTo(mTailWidth,0);
        mArrowPath.rLineTo(xMove,mTailHeight);
        mArrowPath.close();
        canvas.drawPath(mArrowPath,mArrowPaint);
        super.onDraw(canvas);
    }
}
