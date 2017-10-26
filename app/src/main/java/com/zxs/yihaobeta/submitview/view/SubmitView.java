package com.zxs.yihaobeta.submitview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by yihaobeta on 2017/10/25.
 */

public class SubmitView extends View {

  private Context context;

  //默认高度
  private float mViewHeight = 150f;
  //默认宽度
  private float mViewWidth = mViewHeight * 5;

  //整个View的外接矩形
  private RectF mViewRect = new RectF();
  //动画进行过程中的外切矩形
  private RectF mAnimRect = new RectF();

  //背景画笔
  private Paint mBgPaint;
  //文本画笔
  private Paint mTextPaint;
  private float mTextSize = 50;
  //对勾画笔
  private Paint mTickPaint;
  private ValueAnimator mShrinkAnim;
  private long mShrinkDuration = 500;
  private Status mStatus = Status.NORMAL;
  private float mCurrLength = 0;
  private ValueAnimator mPrepareRotateAnim;
  private ValueAnimator mCompletedAnim;
  private int mCompletedAnimDuration = 1000;
  private long mPrepareRotateAnimDuration = 2000;
  private float mPrepareRotateAngle;
  private float mPrepareRotateAnimSpeed = 10;
  private float mCompleteAnimCircleRadio;

  //对勾的路径
  private Path mTickPath = new Path();
  private String mText = "Submit";

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

  private void initPaint() {
    mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBgPaint.setColor(Color.BLUE);
    mBgPaint.setStyle(Paint.Style.FILL);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(Color.WHITE);
    mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mTextPaint.setStrokeWidth(2);
    mTextPaint.setTextSize(mTextSize);
    mTextPaint.setTextAlign(Paint.Align.CENTER);

    mTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTickPaint.setColor(Color.WHITE);
    mTickPaint.setStyle(Paint.Style.STROKE);
    mTickPaint.setStrokeWidth(8);
  }

  private void initAttrs(Context context, AttributeSet attrs) {
    this.context = context;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    float width = getRealSize(mViewWidth, widthMeasureSpec, true);
    float height = getRealSize(mViewHeight, heightMeasureSpec, false);

    if (width <= height) {
      height = width/5;
    }
    setMeasuredDimension((int) width, (int) height);
    mViewRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingLeft(),
        getHeight() - getPaddingTop());
    mCurrLength = mViewRect.width();
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
    if (mStatus == Status.NORMAL) {
      canvas.drawRoundRect(mViewRect, mViewRect.height() / 2, mViewRect.height() / 2, mBgPaint);
      Paint.FontMetrics fm = mTextPaint.getFontMetrics();
      float y = getHeight() / 2 + (fm.descent - fm.ascent) / 2 - fm.descent;
      canvas.drawText(mText, getWidth() / 2, y, mTextPaint);
    } else if (mStatus == Status.START) {

      float mLeft = mViewRect.left + ((mViewRect.width() - mCurrLength) / 2);
      float mRight = mLeft + mCurrLength;
      mAnimRect.set(mLeft, mViewRect.top, mRight, mViewRect.bottom);
      //RectF temp = new RectF();
      canvas.drawRoundRect(mAnimRect, mViewRect.height() / 2, mViewRect.height() / 2, mBgPaint);
    } else if (mStatus == Status.SUBMITTING) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mAnimRect.height() / 2, mBgPaint);
      canvas.save();
      canvas.rotate(mPrepareRotateAngle, mAnimRect.centerX(), mAnimRect.centerY());
      float miniCircleX = mAnimRect.centerX();
      float minCircleR = mAnimRect.width() / 15;
      float minCircleY = mAnimRect.top + mAnimRect.height() / 5;
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.rotate(120, mAnimRect.centerX(), mAnimRect.centerY());
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.rotate(120, mAnimRect.centerX(), mAnimRect.centerY());
      canvas.drawCircle(miniCircleX, minCircleY, minCircleR, mTextPaint);
      canvas.restore();
    } else if (mStatus == Status.COMPLETE) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mCompleteAnimCircleRadio,
          mBgPaint);
    } else if (mStatus == Status.END) {
      canvas.drawCircle(mAnimRect.centerX(), mAnimRect.centerY(), mAnimRect.height() / 2, mBgPaint);
      canvas.drawPath(mTickPath, mTickPaint);
    }
  }

  private void initAnim() {
    mShrinkAnim = ValueAnimator.ofFloat(0, 1);
    mShrinkAnim.setDuration(mShrinkDuration);
    mShrinkAnim.setInterpolator(new AccelerateInterpolator());
    mShrinkAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        mCurrLength = mViewRect.width();
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mStatus = Status.SUBMITTING;
        mPrepareRotateAnim.start();
      }
    });
    mShrinkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mCurrLength = mViewRect.width() * (1 - animation.getAnimatedFraction());
        if (mCurrLength <= mViewRect.height()) {
          mCurrLength = mViewRect.height();
          mShrinkAnim.cancel();
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
        mPrepareRotateAngle += mPrepareRotateAnimSpeed;
        invalidate();
      }
    });

    mCompletedAnim = ValueAnimator.ofFloat(1, 0, 1);
    mCompletedAnim.setDuration(mCompletedAnimDuration);
    mCompletedAnim.setInterpolator(new AccelerateDecelerateInterpolator());
    mCompletedAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);

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
        System.out.println("mCompleteAnimCircleRadio = " + animation.getAnimatedValue());
      }
    });
  }

  public void submit() {
    cancelAllAnim();
    mStatus = Status.START;
    mShrinkAnim.start();
    this.setClickable(false);
  }

  public void submitCompleted() {
    mStatus = Status.COMPLETE;
    mCompletedAnim.start();
    this.setClickable(true);
  }

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
    mShrinkAnim.cancel();
    mPrepareRotateAnim.cancel();
  }

  public SubmitView setText(String text) {
    this.mText = text;
    return this;
  }

  public SubmitView setTextSize(float mTextSize) {

    this.mTextSize = TypedValue.applyDimension(Dimension.SP, mTextSize,
        context.getResources().getDisplayMetrics());
    mTextPaint.setTextSize(this.mTextSize);
    return this;
  }

  /**
   * 状态枚举
   */
  public enum Status {
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
