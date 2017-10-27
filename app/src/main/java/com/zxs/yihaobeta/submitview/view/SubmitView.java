package com.zxs.yihaobeta.submitview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import com.zxs.yihaobeta.submitview.R;
import org.jetbrains.annotations.NotNull;

/**
 * Created by yihaobeta on 2017/10/25.
 */

public class SubmitView extends View {

  private Context mContext;

  //实际测量高度
  private float mViewHeight;
  //实际测量宽度
  private float mViewWidth;

  //整个绘制View的外接矩形
  private RectF mViewRect = new RectF();
  //动画进行过程中的外切矩形
  private RectF mAnimRect = new RectF();

  //背景画笔
  private Paint mBgPaint;
  private int mBgColor;
  //文本画笔
  private Paint mTextPaint;
  //文本颜色
  private int mTextColor;
  //字体大小
  private float mTextSize;
  //对勾画笔
  private Paint mTickPaint;
  private int mTickColor;
  //收缩动画
  private ValueAnimator mPinchAnim;
  private long mPinchAnimDuration = 500;
  //当前的状态
  private Status mStatus = Status.NORMAL;
  //收缩动画过程中的view长度值
  private float mCurViewLengthInAnim = 0;
  //提交过程动画
  private ValueAnimator mPrepareRotateAnim;
  //提交成功动画
  private ValueAnimator mCompletedAnim;
  private int mCompletedAnimDuration = 1000;
  private long mPrepareRotateAnimDuration = 2000;
  //画布旋转角度
  private float mRotateAngle;
  //画布旋转步进值
  private float mRotateAnimSpeed = 10;
  //提交成功动画的圆半径值
  private float mCompleteAnimCircleRadio;

  //对勾的路径
  private Path mTickPath = new Path();
  private String mText = "Submit";

  //常量定义
  //view的默认长宽比
  private static final int ASPECT_RATIO = 5;
  private static final float VIEW_HEIGHT = 150f;
  private static final float VIEW_WIDTH = VIEW_HEIGHT * ASPECT_RATIO;


  public SubmitView(Context context) {
    this(context, null);
  }

  public SubmitView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubmitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initAttrs(context, attrs);
    initPaint();
    initAnim();
  }

  /**
   * 初始化画笔
   */
  private void initPaint() {
    mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBgPaint.setColor(mBgColor);
    mBgPaint.setStyle(Paint.Style.FILL);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mTextColor);
    mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mTextPaint.setStrokeWidth(2);
    mTextPaint.setTextSize(mTextSize);
    mTextPaint.setTextAlign(Paint.Align.CENTER);

    mTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTickPaint.setColor(mTickColor);
    mTickPaint.setStyle(Paint.Style.STROKE);
    mTickPaint.setStrokeWidth(8);
  }

  /**
   * 获取自定义属性
   * @param context
   * @param attrs
   */
  private void initAttrs(Context context, AttributeSet attrs) {
    this.mContext = context;
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SubmitView);
    mTextSize = ta.getInteger(R.styleable.SubmitView_textSize,50);
    mBgColor = ta.getColor(R.styleable.SubmitView_backgroundColor,Color.BLUE);
    mText = ta.getString(R.styleable.SubmitView_text);
    if(mText == null || mText.length()<=0){
      mText = "Submit";
    }

    mTextColor = ta.getColor(R.styleable.SubmitView_textColor,Color.WHITE);
    mTickColor = ta.getColor(R.styleable.SubmitView_tickColor, Color.WHITE);
    ta.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    float width = getRealSize(VIEW_WIDTH, widthMeasureSpec, true);
    float height = getRealSize(VIEW_HEIGHT, heightMeasureSpec, false);

    if (width <= height) {
      height = width/ASPECT_RATIO;
    }
    setMeasuredDimension((int) width, (int) height);
    mViewRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingLeft(),
        getHeight() - getPaddingTop());
    mCurViewLengthInAnim = mViewRect.width();
    mViewHeight = getMeasuredHeight();
    mViewWidth = getMeasuredWidth();
  }

  private float getRealSize(float defaultSize, int measureSpec, boolean isWidth) {
    int mode = MeasureSpec.getMode(measureSpec);
    int size = MeasureSpec.getSize(measureSpec);

    float realSize = 0;
    switch (mode) {
      case MeasureSpec.UNSPECIFIED:
      case MeasureSpec.AT_MOST:
        if (isWidth) {
          realSize = defaultSize + getPaddingLeft() + getPaddingRight();
        } else {
          realSize = defaultSize + getPaddingBottom() + getPaddingTop();
        }
        break;
      case MeasureSpec.EXACTLY:
        realSize = size;
        break;
    }

    return realSize;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //绘制正常状态
    if (mStatus == Status.NORMAL) {
      //画初始的圆角矩形
      canvas.drawRoundRect(mViewRect, mViewRect.height() / 2, mViewRect.height() / 2, mBgPaint);
      Paint.FontMetrics fm = mTextPaint.getFontMetrics();
      float y = mViewHeight / 2 + (fm.descent - fm.ascent) / 2 - fm.descent;
      canvas.drawText(mText, mViewWidth / 2, y, mTextPaint);
    }

    //绘制开始submit作业时的收缩动画
    else if (mStatus == Status.START) {

      float left = mViewRect.left + ((mViewRect.width() - mCurViewLengthInAnim) / 2);
      float right = left + mCurViewLengthInAnim;
      mAnimRect.set(left, mViewRect.top, right, mViewRect.bottom);
      canvas.drawRoundRect(mAnimRect, mViewRect.height() / 2, mViewRect.height() / 2, mBgPaint);
    }

    //绘制正在submit时的加载动画
    else if (mStatus == Status.SUBMITTING) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mAnimRect.height() / 2, mBgPaint);
      canvas.save();
      canvas.rotate(mRotateAngle, mAnimRect.centerX(), mAnimRect.centerY());
      float miniCircleX = mAnimRect.centerX();
      float minCircleR = mAnimRect.width() / 15;
      float minCircleY = mAnimRect.top + mAnimRect.height() / 5;
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.rotate(120, mAnimRect.centerX(), mAnimRect.centerY());
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.rotate(120, mAnimRect.centerX(), mAnimRect.centerY());
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.restore();
    }

    //绘制submit完成时的圆缩放动画
    else if (mStatus == Status.COMPLETE) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mCompleteAnimCircleRadio,
          mBgPaint);
    }

    //绘制最后的对勾
    else if (mStatus == Status.END) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mAnimRect.height() / 2, mBgPaint);
      canvas.drawPath(mTickPath, mTickPaint);
    }
  }

  /**
   * 初始化动画
   */
  private void initAnim() {
    mPinchAnim = ValueAnimator.ofFloat(0, 1);
    mPinchAnim.setDuration(mPinchAnimDuration);
    mPinchAnim.setInterpolator(new AccelerateInterpolator());
    mPinchAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        //动画开始前的初始view宽度为整个view的宽度
        mCurViewLengthInAnim = mViewRect.width();
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mStatus = Status.SUBMITTING;
        mPrepareRotateAnim.start();
      }
    });
    mPinchAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mCurViewLengthInAnim = mViewRect.width() * (1 - animation.getAnimatedFraction());
        if (mCurViewLengthInAnim <= mViewRect.height()) {
          mCurViewLengthInAnim = mViewRect.height();
          mPinchAnim.cancel();
        }
        invalidate();
      }
    });

    // 旋转动画
    mPrepareRotateAnim = ValueAnimator.ofFloat(0, 1);
    mPrepareRotateAnim.setDuration(mPrepareRotateAnimDuration);
    mPrepareRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
    mPrepareRotateAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        mPrepareRotateAnim.cancel();
      }
    });
    mPrepareRotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        //旋转角度递进
        mRotateAngle += mRotateAnimSpeed;
        invalidate();
      }
    });

    //submit完成时的动画
    mCompletedAnim = ValueAnimator.ofFloat(1, 0, 1);
    mCompletedAnim.setDuration(mCompletedAnimDuration);
    mCompletedAnim.setInterpolator(new AccelerateDecelerateInterpolator());
    mCompletedAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);

        //确定对勾在圆内的关键点坐标
        int centerX = (int) mAnimRect.centerX();
        int centerY = (int) mAnimRect.centerY();
        int radio = (int) (mAnimRect.height() / 2);
        mTickPath.moveTo(centerX - radio / 2, centerY);
        mTickPath.lineTo(centerX - radio / 8, centerY + radio / 2);
        mTickPath.lineTo(centerX + radio * 2 / 3, centerY - radio / 3);
        mCompletedAnim.cancel();
        mStatus = Status.END;
      }
    });

    mCompletedAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mCompleteAnimCircleRadio = mAnimRect.height() / 2 * (float) animation.getAnimatedValue();
      }
    });
  }

  /**
   * 开始submit操作时调用
   */
  public void submit() {
    cancelAllAnim();
    mStatus = Status.START;
    mPinchAnim.start();
    this.setClickable(false);
  }

  /**
   * submit完成时调用
   */
  public void submitCompleted() {
    mStatus = Status.COMPLETE;
    mCompletedAnim.start();
    this.setClickable(true);
  }

  /**
   * 恢复初始状态
   */
  public void cancel() {
    mStatus = Status.NORMAL;
    this.setClickable(true);
    reset();
    invalidate();
  }

  private void reset() {
    cancelAllAnim();
  }

  private void cancelAllAnim() {
    mCompletedAnim.cancel();
    mPinchAnim.cancel();
    mPrepareRotateAnim.cancel();
  }

  public SubmitView setText(@NonNull String text) {
    this.mText = text;
    invalidate();
    return this;
  }

  public SubmitView setTextSize(@NonNull float textSize) {

    this.mTextSize = TypedValue.applyDimension(Dimension.SP, textSize,
        mContext.getResources().getDisplayMetrics());
    mTextPaint.setTextSize(this.mTextSize);
    invalidate();
    return this;
  }

  public SubmitView setTextColor(@NotNull int textColor){
    this.mTextColor = textColor;
    mTextPaint.setColor(this.mTextColor);
    invalidate();
    return this;
  }

  public SubmitView setBgColor(@NotNull int bgColor){
    this.mBgColor = bgColor;
    this.mBgPaint.setColor(this.mBgColor);
    invalidate();
    return this;
  }

  public SubmitView setTickColor(@NotNull int tickColor){
    this.mTickColor = tickColor;
    this.mTickPaint.setColor(this.mTickColor);
    invalidate();
    return this;
  }

  /**
   * 状态枚举
   */
  private enum Status {
    /** 未开始，正常 */
    NORMAL,
    /** 开始，收缩 */
    START,
    /** 圆圈加载 */
    SUBMITTING,
    /** 完成 */
    COMPLETE,
    /** 结束 */
    END
  }
}
