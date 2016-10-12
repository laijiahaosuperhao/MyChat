package com.example.laijiahao.mychat.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.laijiahao.mychat.R;


public class ChangeColorIconWithText extends View
{

	private int mColor = 0xFF45C01A;
	private Bitmap mIconBitmap;
	private String mText = "微信";
	//设置默认12sp COMPLEX_UNIT_SP
	private int mTextSize = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());

	//内存中绘图
	private Canvas mCanvas;
	private Bitmap mBitmap;
	private Paint mPaint;

	private float mAlpha;//透明度

	private Rect mIconRect;
	private Rect mTextBound;
	private Paint mTextPaint;

	//一个参数的构造方法调用两个参数的,两个参数的构造方法调用三个参数的。
	public ChangeColorIconWithText(Context context)
	{
		this(context, null);
	}

	public ChangeColorIconWithText(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}


	/**
	 * 获取自定义属性的值
	 *
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	//初始化操作放在三个参数的构造方法里面
	public ChangeColorIconWithText(Context context, AttributeSet attrs,
								   int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ChangeColorIconWithText);

		int n = a.getIndexCount();

		for (int i = 0; i < n; i++)
		{
			int attr = a.getIndex(i);
			switch (attr)
			{
				case R.styleable.ChangeColorIconWithText_icon1:
					BitmapDrawable drawable = (BitmapDrawable) a.getDrawable(attr);
					mIconBitmap = drawable.getBitmap();
					break;
				case R.styleable.ChangeColorIconWithText_color1:
					mColor = a.getColor(attr, 0xFF45C01A);
					break;
				case R.styleable.ChangeColorIconWithText_text1:
					mText = a.getString(attr);
					break;
				case R.styleable.ChangeColorIconWithText_text_size1:
					mTextSize = (int) a.getDimension(attr, TypedValue
							.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
									getResources().getDisplayMetrics()));
					break;
			}

		}

		//使用context.obtainStyledAttributes(attrs,R.styleable.ChangeColorIconWithText)
		// 记得recycle
		a.recycle();

		mTextBound = new Rect();
		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(0Xff555555);//灰色
		//构造方法中获取自定义属性（根据文字长度设置文字画笔Paint
		//，并获取文字区域大小）
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);

	}

	/**
	 * 在onMeasure拿到控件的宽和高以后决定icon的绘制位置,计算Icon区域大小(Rect mIconRect）
	 * View有两种可能宽大于高 宽小于高
	 * View的宽度-leftpadding-rightpadding
	 * View的高度-topPadding-mTextBound.height
	 * icon的边长为下列两个的最小值
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int iconWidth = Math.min(getMeasuredWidth() - getPaddingLeft()
				- getPaddingRight(), getMeasuredHeight() - getPaddingTop()
				- getPaddingBottom() - mTextBound.height());
		//居中
		int left = getMeasuredWidth() / 2 - iconWidth / 2;
		int top = getMeasuredHeight() / 2 - (mTextBound.height() + iconWidth)
				/ 2;
		mIconRect = new Rect(left, top, left + iconWidth, top + iconWidth);
	}

	/**
	 * 绘制原图,在指定位置绘制图标,使用从自定义属性中获取的Bitmap来绘制Icon 根据文字画笔 绘制原文字以及变化的文字
	 * @param canvas
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		//绘制原图
		canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
		//向上取整数
		int alpha = (int) Math.ceil(255 * mAlpha);

		// 内存去准备mBitmap , setAlpha , 纯色 ，xfermode ， 图标,显示区域是纯色且是图标
		setupTargetBitmap(alpha);
		// 1、绘制原文本 ； 2、绘制变色的文本
		drawSourceText(canvas, alpha);
		drawTargetText(canvas, alpha);
		//把内存中的Bitmap绘制出来,即把交集绘制出来,覆盖在原图的上面
		canvas.drawBitmap(mBitmap, 0, 0, null);

	}

	/**
	 * 绘制变色的文本
	 *
	 * @param canvas
	 * @param alpha
	 */
	private void drawTargetText(Canvas canvas, int alpha)
	{
		mTextPaint.setColor(mColor);
		mTextPaint.setAlpha(alpha);
		int x = getMeasuredWidth() / 2 - mTextBound.width() / 2;
		int y = mIconRect.bottom + mTextBound.height();
		canvas.drawText(mText, x, y, mTextPaint);

	}

	/**
	 * 绘制原文本
	 *
	 * @param canvas
	 * @param alpha
	 */
	private void drawSourceText(Canvas canvas, int alpha)
	{
		mTextPaint.setColor(0xff333333);
		mTextPaint.setAlpha(255 - alpha);
		//找到文本的范围
		int x = getMeasuredWidth() / 2 - mTextBound.width() / 2;
		int y = mIconRect.bottom + mTextBound.height();
		canvas.drawText(mText, x, y, mTextPaint);

	}

	/**
	 * 在内存中绘制可变色的Icon
	 */
	private void setupTargetBitmap(int alpha)
	{
		//首先得到一块mBitmap,就是view的大小
		mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
				Config.ARGB_8888);
		//根据Bitmap拿到Canvas
		mCanvas = new Canvas(mBitmap);
		//设置Paint的一些属性
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setAlpha(alpha);
		//绘制一个纯色
		mCanvas.drawRect(mIconRect, mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		//显示两者的交集且前者(mColor)的颜色
		mPaint.setAlpha(255);
		mCanvas.drawBitmap(mIconBitmap, null, mIconRect, mPaint);
	}

	private static final String INSTANCE_STATUS = "instance_status";
	private static final String STATUS_ALPHA = "status_alpha";

	//activity的alpha值当重建前可以保存
	@Override
	protected Parcelable onSaveInstanceState()
	{
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
		bundle.putFloat(STATUS_ALPHA, mAlpha);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (state instanceof Bundle)
		{
			Bundle bundle = (Bundle) state;
			mAlpha = bundle.getFloat(STATUS_ALPHA);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
			return;
		}
		super.onRestoreInstanceState(state);
	}

	/**
	 * 外界可以设置alpha
	 * @param alpha
	 */
	public void setIconAlpha(float alpha)
	{
		this.mAlpha = alpha;
		invalidateView();
	}

	/**
	 * 重绘view
	 */
	private void invalidateView()
	{
		//判断是否是UI线程
		if (Looper.getMainLooper() == Looper.myLooper())
		{
			invalidate();
		} else
		{
			postInvalidate();
		}
	}

}
