package com.zxs.yihaobeta.submitview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.zxs.yihaobeta.submitview.R;
import org.jetbrains.annotations.NotNull;

/**
 * Created by yihaobeta on 2017/10/26.
 */

public class SubmitWithProgressView extends View {

  //常量定义
  private static final int ASPECT_RATIO = 5;
  private static final int VIEW_HEIGHT = 150;
  private static final int VIEW_WIDTH = VIEW_HEIGHT * 5;
  private static final long CHANGE_TO_PROGRESSBAR_ANIM_DURATION = 500;
  private static final long PINCH_ANIM_DURATION = 500;
  private static final long COMPLETE_ANIM_DURATION = 500;
  private static final float DEFAULT_PROGRESSBAR_VIEW_ASPECT_RATIO = 8;
  private static final int DEFAULT_TEXT_SIZE = 50;
  private static final float DEFAULT_PROGRESS_MAX_VALUE = 100;
  private static final String DEFAULT_TEXT = "Submit";
  private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
  private static final String DEFAULT_COMPLETE_TEXT = "OK";
  private static final int DEFAULT_PROGRESS_COLOR = Color.RED;
  private static final int DEFAULT_BACKGROUND_COLOR = Color.BLUE;
  //默认的进度条的高度，先设为0，待view的高度获取后在根据比例取值
  private static final int DEFAULT_PROGRESS_HEIGHT = 0;
  private static final int DEFAULT_CIRCLE_COLOR = Color.BLUE;
  private static final int DEFAULT_COMPLETE_TEXT_SIZE = 50;
  private static final int DEFAULT_COMPLETE_TEXT_COLOR = Color.WHITE;

  //绘制文本
  private String mText;
  private String mCompleteText;

  //实测view的宽度
  private int mViewWidth;
  private int mViewHeight;

  private Paint mBgPaint;
  private int mBgColor;
  private Paint mTextPaint;
  //提交完成后圆内显示的字体画笔
  private Paint mCompleteTextPaint;
  private int mCompleteTextColor;
  private int mTextColor;
  private int mTextSize;
  private int mCompleteTextSize;
  //绘制进度条的画笔
  private Paint mProgressPaint;
  private int mProgressColor;
  //绘制最后的圆的画笔
  private Paint mCirclePaint;
  private int mCircleColor;

  //最终圆的半径和圆心坐标
  private float mCircleRadius;
  private Point mCircleCenter = new Point();

  //由按钮变成进度条的动画
  private ValueAnimator mChangeToProgressBarAnim;
  //压缩动画
  private ValueAnimator mPinchAnim;
  //完成后的圆的放大动画效果
  private ValueAnimator mCompletedAnim;

  //变换中的view高度
  private int mViewTransformingHeight;
  private int mProgressHeight;
  private float mCurProgressValue;
  private float mMaxProgressValue;
  //整个view的绘制矩形
  private RectF mViewRect = new RectF();
  //进度条的绘制矩形
  private RectF mProgressBarRect = new RectF();
  //进度条压缩过程中的绘制矩形
  private RectF mProgressBarTransformToCircleRect = new RectF();

  private Status mStatus = Status.NORMAL;
  //动画进行中的view长度值
  private float mCurViewLengthInAnim;
  //圆在动画过程中的半径值
  private float mCircleRadiusInAnim;

  public SubmitWithProgressView(Context context) {
    this(context, null);
  }

  public SubmitWithProgressView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubmitWithProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initAttrs(context, attrs);
    initPaints();
    initAnim();
  }

  private void initAnim() {

    //初始化由按钮向进度条变化的动画
    mChangeToProgressBarAnim = ValueAnimator.ofFloat(1, 0);
    mChangeToProgressBarAnim.setInterpolator(new LinearInterpolator());
    mChangeToProgressBarAnim.setDuration(CHANGE_TO_PROGRESSBAR_ANIM_DURATION);
    mChangeToProgressBarAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
        if (mProgressHeight <= 0) {
          mProgressHeight = (int) (mViewRect.height() / DEFAULT_PROGRESSBAR_VIEW_ASPECT_RATIO);
        }
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mStatus = Status.SUBMITTING;
      }
    });

    mChangeToProgressBarAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mViewTransformingHeight = (int) (mViewRect.height() * (Float) animation.getAnimatedValue());
        if (mViewTransformingHeight <= mProgressHeight) {
          mViewTransformingHeight = mProgressHeight;
          mChangeToProgressBarAnim.cancel();
        }
        invalidate();
      }
    });

    //初始化进度条压缩的动画
    mPinchAnim = ValueAnimator.ofFloat(1, 0);
    mPinchAnim.setDuration(PINCH_ANIM_DURATION);
    mPinchAnim.setInterpolator(new AccelerateInterpolator());
    mPinchAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        super.onAnimationEnd(animation);
        mCurViewLengthInAnim = mProgressBarRect.width();
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mStatus = Status.END;
        mCircleCenter.x = (int) (mProgressBarTransformToCircleRect.left
            + mProgressBarTransformToCircleRect.width() / 2);
        mCircleCenter.y = (int) (mProgressBarTransformToCircleRect.top
            + mProgressBarTransformToCircleRect.height() / 2);
        mCircleRadius = mViewRect.height() / 2;
        mCompletedAnim.start();
      }
    });

    mPinchAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mCurViewLengthInAnim = mProgressBarRect.width() * (Float) animation.getAnimatedValue();
        if (mCurViewLengthInAnim <= mProgressBarRect.height()) {
          mCurViewLengthInAnim = mProgressBarRect.height();
          mPinchAnim.cancel();
        }
        invalidate();
      }
    });

    //submit完成时的动画
    mCompletedAnim = ValueAnimator.ofFloat(0, 1);
    mCompletedAnim.setDuration(COMPLETE_ANIM_DURATION);
    mCompletedAnim.setInterpolator(new AccelerateInterpolator());
    mCompletedAnim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
      }
    });

    mCompletedAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        //TODO
        mCircleRadiusInAnim = mProgressBarTransformToCircleRect.height() / 2
            + mCircleRadius * (float) animation.getAnimatedValue();
        if (mCircleRadiusInAnim >= mCircleRadius) {
          mCircleRadiusInAnim = mCircleRadius;
          mCompletedAnim.cancel();
        }
        invalidate();
      }
    });
  }

  /**
   * 初始化画笔
   */
  private void initPaints() {
    mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBgPaint.setColor(mBgColor);
    mBgPaint.setStyle(Paint.Style.FILL);
    mBgPaint.setDither(true);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mTextColor);
    mTextPaint.setTextSize(mTextSize);
    mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mTextPaint.setStrokeWidth(2);
    mTextPaint.setTextAlign(Paint.Align.CENTER);

    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setColor(mCircleColor);
    mCirclePaint.setStyle(Paint.Style.FILL);

    mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mProgressPaint.setColor(mProgressColor);
    mProgressPaint.setStyle(Paint.Style.FILL);

    mCompleteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCompleteTextPaint.setColor(mCompleteTextColor);
    mCompleteTextPaint.setTextSize(mCompleteTextSize);
    mCompleteTextPaint.setTextAlign(Paint.Align.CENTER);
    mCompleteTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mCompleteTextPaint.setStrokeWidth(2);
  }

  /**
   * 自定义属性的获取与初始化
   */
  private void initAttrs(Context context, AttributeSet attrs) {

    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SubmitWithProgressView);
    mText = ta.getString(R.styleable.SubmitWithProgressView_text);
    mCompleteText = ta.getString(R.styleable.SubmitWithProgressView_completeText);
    if (mText == null) mText = DEFAULT_TEXT;
    if (mCompleteText == null) mCompleteText = DEFAULT_COMPLETE_TEXT;
    mTextSize = ta.getInt(R.styleable.SubmitWithProgressView_textSize, DEFAULT_TEXT_SIZE);
    mTextColor = ta.getColor(R.styleable.SubmitWithProgressView_textColor, DEFAULT_TEXT_COLOR);
    mProgressColor =
        ta.getColor(R.styleable.SubmitWithProgressView_progressColor, DEFAULT_PROGRESS_COLOR);
    mBgColor =
        ta.getColor(R.styleable.SubmitWithProgressView_backgroundColor, DEFAULT_BACKGROUND_COLOR);
    mMaxProgressValue = ta.getFloat(R.styleable.SubmitWithProgressView_maxProgressValue,
        DEFAULT_PROGRESS_MAX_VALUE);
    mProgressHeight =
        ta.getInt(R.styleable.SubmitWithProgressView_progressbarHeight, DEFAULT_PROGRESS_HEIGHT);
    mCircleColor =
        ta.getColor(R.styleable.SubmitWithProgressView_circleColor, DEFAULT_CIRCLE_COLOR);

    mCompleteTextColor = ta.getColor(R.styleable.SubmitWithProgressView_completeTextColor,
        DEFAULT_COMPLETE_TEXT_COLOR);
    mCompleteTextSize =
        ta.getInt(R.styleable.SubmitWithProgressView_completeTextSize, DEFAULT_COMPLETE_TEXT_SIZE);
    ta.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    float width = getRealSize(VIEW_WIDTH, widthMeasureSpec, true);
    float height = getRealSize(VIEW_HEIGHT, heightMeasureSpec, false);

    if (width <= height) {
      height = width / ASPECT_RATIO;
    }
    setMeasuredDimension((int) width, (int) height);
    mViewRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingLeft(),
        getHeight() - getPaddingTop());

    mViewHeight = getMeasuredHeight();
    mViewWidth = getMeasuredWidth();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    switch (mStatus) {

      //一般状态的绘制
      case NORMAL:
        canvas.drawRoundRect(mViewRect, mViewRect.height() / 2, mViewRect.height() / 2, mBgPaint);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float y = mViewHeight / 2 + (fm.descent - fm.ascent) / 2 - fm.descent;
        canvas.drawText(mText, mViewWidth / 2, y, mTextPaint);
        break;

      //开始提交状态的绘制，主要是根据高度变化来绘制圆角矩形
      case START:
        float top = mViewRect.top + (mViewRect.height() - mViewTransformingHeight) / 2;
        float bottom = top + mViewTransformingHeight;

        mProgressBarRect.set(mViewRect.left, top, mViewRect.right, bottom);
        canvas.drawRoundRect(mProgressBarRect, mProgressBarRect.height() / 2,
            mProgressBarRect.height() / 2, mBgPaint);
        break;

      //进度条的绘制过程
      case SUBMITTING:
        canvas.drawRoundRect(mProgressBarRect, mProgressBarRect.height() / 2,
            mProgressBarRect.height() / 2, mBgPaint);
        canvas.save();
        float progressLength = (mProgressBarRect.right * (mCurProgressValue / mMaxProgressValue));
        canvas.clipRect(mProgressBarRect.left, mProgressBarRect.top, progressLength,
            mProgressBarRect.bottom);
        canvas.drawRoundRect(mProgressBarRect, mProgressBarRect.height() / 2,
            mProgressBarRect.height() / 2, mProgressPaint);
        canvas.restore();
        if (mCurProgressValue >= mMaxProgressValue) {
          this.mStatus = Status.COMPLETE;
          mPinchAnim.start();
        }
        break;

      //进度条完成后圆的绘制，主要是根据半径的变化来画圆
      case COMPLETE:
        float left =
            mProgressBarRect.left + ((mProgressBarRect.width() - mCurViewLengthInAnim) / 2);
        float right = left + mCurViewLengthInAnim;
        mProgressBarTransformToCircleRect.set(left, mProgressBarRect.top, right,
            mProgressBarRect.bottom);
        canvas.drawRoundRect(mProgressBarTransformToCircleRect, mProgressBarRect.height() / 2,
            mProgressBarRect.height() / 2, mProgressPaint);
        break;

      //最后的文本绘制
      case END:
        canvas.drawCircle(mCircleCenter.x, mCircleCenter.y, mCircleRadiusInAnim, mCirclePaint);
        if (mCircleRadiusInAnim >= mCircleRadius) {
          Paint.FontMetrics fmCircle = mCompleteTextPaint.getFontMetrics();
          float y1 = mViewHeight / 2 + (fmCircle.descent - fmCircle.ascent) / 2 - fmCircle.descent;
          canvas.drawText(mCompleteText, mCircleCenter.x, y1, mCompleteTextPaint);
        }
    }
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

  public SubmitWithProgressView submit() {
    mStatus = Status.START;
    mChangeToProgressBarAnim.start();
    this.setClickable(false);
    return this;
  }

  public void setProgressValue(@NotNull int progressValue) {
    if (mStatus != Status.SUBMITTING) {
      return;
    }
    this.mCurProgressValue = progressValue;
    if (mCurProgressValue > mMaxProgressValue) {
      mCurProgressValue = mMaxProgressValue;
      return;
    }
    postInvalidate();
  }

  public SubmitWithProgressView setMaxProgressValue(@NotNull int maxProgressValue) {
    this.mMaxProgressValue = maxProgressValue;
    return this;
  }

  public SubmitWithProgressView reset() {
    return cancel();
  }

  public SubmitWithProgressView cancel() {

    cancelAllAnim();
    mStatus = Status.NORMAL;
    mCurProgressValue = 0;
    postInvalidate();
    this.setClickable(true);
    return this;
  }

  private void cancelAllAnim() {
    mPinchAnim.cancel();
    mChangeToProgressBarAnim.cancel();
    mCompletedAnim.cancel();
  }

  public Status getCurrentStatus() {
    return mStatus;
  }

  public SubmitWithProgressView setCircleColor(int color) {
    this.mCircleColor = color;
    mCirclePaint.setColor(this.mCircleColor);
    return this;
  }

  public SubmitWithProgressView setText(String text) {
    this.mText = text;
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setCompleteText(String completeText) {
    this.mCompleteText = completeText;
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setBgColor(int bgColor) {
    this.mBgColor = bgColor;
    mBgPaint.setColor(this.mBgColor);
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setTextColor(int textColor) {
    this.mTextColor = textColor;
    this.mTextPaint.setColor(this.mTextColor);
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setTextSize(int textSize) {

    this.mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize,
        getContext().getResources().getDisplayMetrics());
    mTextPaint.setTextSize(this.mTextSize);
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setProgressColor(int progressColor) {
    this.mProgressColor = progressColor;
    mProgressPaint.setColor(this.mProgressColor);
    return this;
  }

  public SubmitWithProgressView setProgressHeight(int progressHeight) {
    this.mProgressHeight = progressHeight;
    return this;
  }

  public SubmitWithProgressView setCompleteTextSize(int textSize) {
    this.mCompleteTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize,
        getContext().getResources().getDisplayMetrics());
    mCompleteTextPaint.setTextSize(this.mCompleteTextSize);
    postInvalidate();
    return this;
  }

  public SubmitWithProgressView setCompleteTextColor(int color) {
    this.mCompleteTextColor = color;
    mCompleteTextPaint.setColor(this.mCompleteTextColor);
    postInvalidate();
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
