package com.oldwei.yuntodo.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.oldwei.yuntodo.R;

/**
 * ScrollBanner 支持滑屏效果的FrameLayout子类，可设置屏幕数量，尺寸。<br/>
 * 典型的用法：<br/>
 * ScrollBanner scrollBanner = new ScrollBanner(this, mScreenWidth, 100, this);<br/>
 * linearLayout.addView(scrollBanner);<br/>
 * 注意事项：<br/>
 * 1.如果重新设置ScrollBanner的LayoutParams，则参数中的宽和高属性将被忽略，仍然采用对象实例化的宽和高<br/>
 * 2.点击事件的回调如果设为null，则采用默认的事件回调<br/>
 * 3.通过setOverScrollMode来设置 banner是否能够滑出屏幕的边界<br/>
 * 4通过xml方式加载banner，需要进行如下调用：<br/>
 * setResolution(width, height);<br/>
 * setOnBannerClickListener(bannerClickListener);<br/>
 * showBanner()<br/>
 * 
 * @author singwhatiwanna
 * @version 2013.3.4
 * 
 */
public class ScrollBanner extends FrameLayout implements
		ComponentCallBack.OnBannerClickListener,
		ResponseHandler.BannerInfoHandler {

	private static final String TAG = "ScrollBanner";

	private HorizontalScrollViewEx mHorizontalScrollViewEx;

	// ScrollBanner的子view
	private LinearLayout linearLayoutScrolLayout;

	// linearLayoutScrolLayout的子view，用于放置若干个小圆点
	private LinearLayout linearLayoutForDot;

	private Scroller mScroller;
	private Context mContext;
	private OnBannerClickListener mBannerClickListener;

	// 屏幕及其bitmap
	private List<View> mLinearLayoutScreens = new ArrayList<View>();
	private List<Bitmap> mBannerBitmaps = new ArrayList<Bitmap>();

	// banner信息
	private List<BannerItem> mBannerItemsList = new ArrayList<BannerItem>();
	// 小圆点
	private List<ImageView> mImageViewList = new ArrayList<ImageView>();
	private Drawable mPageIndicator;
	private Drawable mPageIndicatorFocused;

	// banner默认图片
	private Bitmap mDefaultBitmap;

	private int mScreenWidth;
	private int mScreenHeight;
	private int mScrollX;

	// current screen index
	private int mWhich = 0;

	public static final int MESSAGE_AUTO_SCROLL = 1;

	public static final int MESSAGE_FETCH_BANNER_SUCCESS = 2;

	public static final int MARGIN_BOTTOM = 2;

	// 480*150 banner的图片尺寸 150.0/480=0.3125f
	public static final float ratio = 0.3125f;

	// banner的位置
	private int mLocation = -1;

	// banner分为几屏
	private int PAGE_COUNT = 4;

	// 滑动方向 是否向右滑动
	private boolean mScrollToRight = true;

	// 是否自动滑屏
	private boolean mTimerResume = true;

	// 标志用户是否手动滑动了屏幕
	private boolean mByUserAction = false;

	// 标志banner是否可以滑出边界
	private boolean mOverScrollMode = false;
	// 标志banner可以滑出边界多少像素
	private int mOverScrollDistance = 0;

	// 定时器 用于banner的自动播放
	final Timer timer = new Timer();

	// 定时器的时间间隔 单位：ms
	public static final int TIMER_DURATION = 5000;

	private TimerTask mTimerTask = new TimerTask() {
		@Override
		public void run() {
			if (mTimerResume && !mByUserAction) {
				mHandler.sendEmptyMessage(MESSAGE_AUTO_SCROLL);
			}
			mByUserAction = false;
		}
	};

	// ScrollBanner私有handler 用于处理内部逻辑
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// 表示已经执行了onDetachedFromWindow，banner已经被销毁了
			if (mBannerBitmaps == null || mLinearLayoutScreens == null
					|| mImageViewList == null || mBannerItemsList == null
					|| mContext == null)
				return;

			switch (msg.what) {
			case MESSAGE_AUTO_SCROLL:
				if (mWhich == PAGE_COUNT - 1)
					mScrollToRight = false;
				else if (mWhich == 0) {
					mScrollToRight = true;
				}

				if (mScrollToRight)
					mWhich++;
				else {
					mWhich--;
				}

				mHorizontalScrollViewEx.switchView(mWhich);
				break;
			case MESSAGE_FETCH_BANNER_SUCCESS:
				int more = 0;
				if (mBannerItemsList != null)
					more = mBannerItemsList.size() - PAGE_COUNT;
				if (mBannerItemsList.size() > 0) {
					// 如果有banner 显示它
					ScrollBanner.this.show(true);
				}
				// 如果后台返回的banneritem的数量大于预设值4
				if (more > 0) {
					for (int i = 0; i < more; i++)
						addBannerItem();
				}
				fetchBannerImages();
				break;

			default:
				break;
			}
		};
	};

	// 用于获取bitmap
	private Handler mBitmapHandler = new Handler() {

		public void handleMessage(Message msg) {
			// 表示已经执行了onDetachedFromWindow，banner已经被销毁了
			if (mBannerBitmaps == null || mLinearLayoutScreens == null
					|| mImageViewList == null || mBannerItemsList == null
					|| mContext == null)
				return;

			Bitmap bitmap = (Bitmap) msg.obj;
			String urlString = msg.getData().getString("url");
			Log.d(TAG, "url=" + urlString);
			if (urlString == null || bitmap == null || mBannerItemsList == null) {
				Log.w(TAG, "bitmap=null imgurl=" + urlString);
				return;
			}

			for (int i = 0; i < mBannerItemsList.size(); i++) {
				BannerItem item = mBannerItemsList.get(i);
				if (item != null && urlString.equals(item.imgUrl)) {
					Log.d(TAG, "find " + i + urlString);
					if (mBannerBitmaps != null) {
						mBannerBitmaps.set(i, bitmap);
						setBannerImages(i);
					}
					break;
				}
			}

		};

	};

	public ScrollBanner(Context context) {
		this(context, null);
	}

	public ScrollBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public ScrollBanner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	/**
	 * 
	 * @param context
	 *            activity实例
	 * @param width
	 *            banner的宽度 单位px
	 * @param height
	 *            banner的高度 单位dip,-1表示根据图片比例自适应高度
	 * @param bannerClickListener
	 *            单击banner的回调接口
	 */
	public ScrollBanner(Context context, final int width, final int height,
			OnBannerClickListener bannerClickListener) {
		this(context, null);

		int activityId = ((BaseActivity) context).activityId();
		if (activityId == BaseActivity.ACCOUNT_ID)// 位置3
			mLocation = 3;
		else if (activityId == BaseActivity.GAMEZONE_ID)// 位置2
		{
			mLocation = 2;
		}

		// 初始化时不显示banner
		this.show(false);
		setResolution(width, height);
		setOnBannerClickListener(bannerClickListener);
		setDefaultBannerImages();
		fetchBannerInfo();
	}

	/**
	 * 通过xml方式加载banner，必须调用此方法才能显示
	 */
	public void showBanner() {
		int activityId = ((BaseActivity) mContext).activityId();
		if (activityId == BaseActivity.ACCOUNT_ID)// 位置3
			mLocation = 3;
		else if (activityId == BaseActivity.GAMEZONE_ID)// 位置2
		{
			mLocation = 2;
		}

		setDefaultBannerImages();
		fetchBannerInfo();
	}

	/**
	 * 暂停滚动
	 */
	public void pauseScroll() {
		mTimerResume = false;
	}

	/**
	 * 恢复滚动
	 */
	public void resumeScroll() {
		mTimerResume = true;
	}

	/**
	 * 设置回调接口
	 * 
	 * @param callBack
	 *            单击banner的回调接口
	 */
	public void setOnBannerClickListener(
			OnBannerClickListener bannerClickListener) {
		mBannerClickListener = (bannerClickListener != null ? bannerClickListener
				: ScrollBanner.this);
	}

	/**
	 * 设置banner的解析度
	 * 
	 * @param width
	 *            banner的宽度
	 * @param height
	 *            banner的高度
	 */
	public void setResolution(final int width, final int height) {
		int heightInPx = height;

		if (height == -1)
			heightInPx = (int) (ratio * width);
		else {
			Resources resources = getResources();
			heightInPx = Math.round(TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, height,
					resources.getDisplayMetrics()));
		}

		mScreenWidth = width;
		mScreenHeight = heightInPx;
		setLayoutParams(new LayoutParams(width, heightInPx));

		initScrollView();
	}

	/**
	 * 获取banner的高度
	 * 
	 * @return banner的高度 单位：px
	 */
	public int getHeightPixels() {
		return mScreenHeight;
	}

	/**
	 * 设置banner是否可以弹性滑出边界
	 * 
	 * @param canOverScroll
	 *            true表示可以滑出边界，false不能
	 */
	public void setOverScrollMode(boolean canOverScroll) {
		mOverScrollMode = canOverScroll;
		if (canOverScroll == false)
			mOverScrollDistance = 0;
	}

	/**
	 * 向后台获取banner的各种信息
	 */
	private void fetchBannerInfo() {
		NetworkManager netManager = (NetworkManager) AppEngine.getInstance()
				.getManager(IManager.NETWORK_ID);
		netManager.getBannerInfo(String.valueOf(mLocation), ScrollBanner.this);
	}

	/**
	 * 获取banner的滑屏图像
	 */
	private void setDefaultBannerImages() {
		// 为banner设置默认bitmap
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inJustDecodeBounds = false;
		bitmapFactoryOptions.inSampleSize = 2;

		Resources res = mContext.getResources();
		mDefaultBitmap = BitmapFactory.decodeResource(res,
				R.drawable.banner_image_default, bitmapFactoryOptions);

		for (int i = 0; i < PAGE_COUNT; i++)
			mBannerBitmaps.add(i, mDefaultBitmap);

		// 初始化BannerItem对象
		for (int i = 0; i < PAGE_COUNT; i++)
			mBannerItemsList.add(i, null);

		setBannerImages(-1);
	}

	private void fetchBannerImages() {
		// 表示已经执行了onDetachedFromWindow，banner已经被销毁了
		if (mBannerItemsList == null)
			return;

		// ImageManager 根据url向其获取bitmap
		ImageManager imageManager = (ImageManager) AppEngine.getInstance()
				.getManager(IManager.IMAGE_ID);

		BannerItem item = null;
		for (int i = 0; i < PAGE_COUNT; i++) {
			try {
				item = mBannerItemsList.get(i);
			} catch (IndexOutOfBoundsException e) {
				Log.e(TAG, "fetchBannerImages error: " + e);
			} catch (Exception e) {
				Log.e(TAG, "fetchBannerImages error: " + e);
			}
			// ImageManager为多线程，采用常见的三级cache策略（内存、文件、网络）
			if (item != null && item.imgUrl != null)
				imageManager.loadBitmap(item.imgUrl, mBitmapHandler);
		}
	}

	/**
	 * 设置banner的滑屏图像
	 * 
	 * @param position
	 *            如果position=-1，则表示设置全部bitmap
	 */
	private void setBannerImages(final int position) {
		int size = mBannerBitmaps.size();
		if (size < PAGE_COUNT || mLinearLayoutScreens == null) {
			return;
		}
		if (position >= 0 && position < PAGE_COUNT) {
			Drawable drawable = mLinearLayoutScreens.get(position)
					.getBackground();
			mLinearLayoutScreens.get(position).setBackgroundDrawable(
					new BitmapDrawable(mBannerBitmaps.get(position)));
			drawable.setCallback(null);
			drawable = null;

			return;
		}

		for (int i = 0; i < PAGE_COUNT; i++) {
			mLinearLayoutScreens.get(i).setBackgroundDrawable(
					new BitmapDrawable(mBannerBitmaps.get(i)));
		}
	}

	/**
	 * 是否显示banner
	 * 
	 * @param isShow
	 *            true显示 false不显示
	 */
	public void show(boolean isShow) {
		if (isShow) {
			this.setVisibility(View.VISIBLE);
			mTimerResume = true;
		} else {
			this.setVisibility(View.GONE);
			mTimerResume = false;
		}
	}

	/**
	 * 切换到指定屏幕
	 * 
	 * @param which
	 *            屏幕索引
	 */
	public void switchToScreen(final int which) {
		mHorizontalScrollViewEx.switchView(which);
	}

	/**
	 * 设置屏幕的数量 （此函数暂不开放）
	 * 
	 * @param count
	 *            屏幕数量
	 */
	protected void setScreenCount(final int count) {
		PAGE_COUNT = count;
	}

	/**
	 * 设置偏移的距离 如果mOverScrollMode为false，则此设置无效 （此函数暂不开放）
	 * 
	 * @param distance
	 */
	protected void setOverScrollDistance(int distance) {
		if (distance < 0)
			distance = 0;

		mOverScrollDistance = mOverScrollMode ? distance : 0;
	}

	/**
	 * 切换小圆点
	 * 
	 * @param position
	 *            current screen index
	 */
	private void switchScreenPosition(final int position) {
		if (mPageIndicator == null || mPageIndicatorFocused == null)
			return;

		int length = 0;
		if (mImageViewList != null)
			length = mImageViewList.size();
		if (position >= length || position < 0 || length <= 0) {
			return;
		}

		for (int i = 0; i < length; i++) {
			mImageViewList.get(i).setImageDrawable(mPageIndicator);
		}

		mImageViewList.get(position).setImageDrawable(mPageIndicatorFocused);
	}

	/**
	 * 初始化整个FrameLayout视图组
	 */
	private void initScrollView() {
		setLayoutParams(new LayoutParams(mScreenWidth, mScreenHeight));

		linearLayoutScrolLayout = new LinearLayout(mContext);
		linearLayoutScrolLayout.setBackgroundColor(Color.WHITE);
		linearLayoutScrolLayout.setOrientation(LinearLayout.HORIZONTAL);

		int mVersionCode = 8;
		try {
			mVersionCode = Integer.valueOf(android.os.Build.VERSION.SDK);
			Log.d(TAG, "sdk version=" + mVersionCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 针对android1.6及以下的特殊处理 此为android的低版本bug
		if (mVersionCode <= 5) {
			linearLayoutScrolLayout.setBaselineAligned(false);
		}

		// 初始化四个滑动view
		for (int i = 0; i < PAGE_COUNT; i++) {
			LinearLayout linearLayoutScreen = new LinearLayout(mContext);
			linearLayoutScreen.setOrientation(LinearLayout.VERTICAL);
			linearLayoutScrolLayout.addView(linearLayoutScreen,
					new LayoutParams(mScreenWidth, LayoutParams.FILL_PARENT));

			mLinearLayoutScreens.add(i, linearLayoutScreen);
		}

		// 初始化小圆点视图
		RelativeLayout relativeLayout = new RelativeLayout(mContext);
		relativeLayout.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		// linearLayoutForDot为小圆点视图
		linearLayoutForDot = new LinearLayout(mContext);
		android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// 小圆点距底部的距离 单位:px
		layoutParams.bottomMargin = MARGIN_BOTTOM;
		layoutParams.rightMargin = MARGIN_BOTTOM;
		layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
		linearLayoutForDot.setLayoutParams(layoutParams);
		linearLayoutForDot.setOrientation(LinearLayout.HORIZONTAL);
		linearLayoutForDot.setHorizontalGravity(Gravity.CENTER);
		linearLayoutForDot.setVerticalGravity(Gravity.CENTER);
		// 下面两句实现圆角半透明效果 不采用
		// linearLayoutForDot.setBackgroundResource(R.drawable.round_corner_bg);
		// linearLayoutForDot.getBackground().setAlpha(100);

		// 初始化4个小圆点
		mPageIndicator = getResources().getDrawable(R.drawable.page_indicator);
		mPageIndicatorFocused = getResources().getDrawable(
				R.drawable.page_indicator_focused);
		for (int i = 0; i < PAGE_COUNT; i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setImageDrawable(mPageIndicator);
			mImageViewList.add(i, imageView);
			LinearLayout.LayoutParams layoutParamsForDot = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParamsForDot.rightMargin = 5;

			linearLayoutForDot.addView(imageView, layoutParamsForDot);
		}
		mImageViewList.get(0).setImageDrawable(mPageIndicatorFocused);
		relativeLayout.addView(linearLayoutForDot);

		mHorizontalScrollViewEx = new HorizontalScrollViewEx(mContext, null,
				mBannerClickListener);
		mHorizontalScrollViewEx.setLayoutParams(new LayoutParams(mScreenWidth
				* PAGE_COUNT, LayoutParams.FILL_PARENT));
		mHorizontalScrollViewEx.addView(linearLayoutScrolLayout,
				new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));

		mHorizontalScrollViewEx.setHorizontalScrollBarEnabled(false);
		mHorizontalScrollViewEx.setHorizontalFadingEdgeEnabled(false);

		addView(mHorizontalScrollViewEx);
		addView(relativeLayout);

		// 自动滑屏 5秒一次
		timer.schedule(mTimerTask, 5000, TIMER_DURATION);
	}

	/**
	 * 加一个banner页面 TODO此函数写的不好
	 */
	private void addBannerItem() {
		// 表示已经执行了onDetachedFromWindow，banner已经被销毁了
		if (mBannerBitmaps == null || mLinearLayoutScreens == null
				|| mImageViewList == null || mContext == null)
			return;

		// 调整屏幕数量和总宽度
		PAGE_COUNT += 1;
		mHorizontalScrollViewEx.getLayoutParams().width = mScreenWidth
				* PAGE_COUNT;

		// 加载默认图片资源
		if (mDefaultBitmap == null) {
			BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
			bitmapFactoryOptions.inJustDecodeBounds = false;
			bitmapFactoryOptions.inSampleSize = 2;
			Resources res = mContext.getResources();
			mDefaultBitmap = BitmapFactory.decodeResource(res,
					R.drawable.banner_image_default, bitmapFactoryOptions);
		}
		mBannerBitmaps.add(mDefaultBitmap);
		mBannerItemsList.add(null);
		// 加一个屏幕
		LinearLayout linearLayoutScreen = new LinearLayout(mContext);
		linearLayoutScreen.setOrientation(LinearLayout.VERTICAL);
		linearLayoutScreen.setBackgroundDrawable(new BitmapDrawable(
				mBannerBitmaps.get(PAGE_COUNT - 1)));
		linearLayoutScrolLayout.addView(linearLayoutScreen, new LayoutParams(
				mScreenWidth, LayoutParams.FILL_PARENT));
		mLinearLayoutScreens.add(linearLayoutScreen);

		// 加一个小圆点
		ImageView imageView = new ImageView(mContext);
		imageView.setImageDrawable(mPageIndicator);
		mImageViewList.add(imageView);
		LinearLayout.LayoutParams layoutParamsForDot = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParamsForDot.rightMargin = 5;
		linearLayoutForDot.addView(imageView, layoutParamsForDot);
	}

	private class HorizontalScrollViewEx extends ViewGroup implements
			OnGestureListener {

		private GestureDetector mGestureDetector;
		private int mWhichScreen;

		public HorizontalScrollViewEx(Context context, AttributeSet attrs,
				OnBannerClickListener bannerClickListener) {
			super(context, attrs);

			mGestureDetector = new GestureDetector(this);
			// 解决长按屏幕后无法拖动的现象
			mGestureDetector.setIsLongpressEnabled(false);

			// 构造弹性滑动对象
			mScroller = new Scroller(context);
		}

		/**
		 * 切换到指定屏幕
		 * 
		 * @param whichScreen
		 *            屏幕index
		 */
		public void switchView(int whichScreen) {
			if (mLinearLayoutScreens == null)
				return;

			// 防止非法参数
			if (whichScreen < 0)
				whichScreen = 0;
			else if (whichScreen >= PAGE_COUNT)
				whichScreen = PAGE_COUNT - 1;

			Log.i(TAG, "switch view to " + whichScreen);

			int delta = whichScreen * mScreenWidth
					- HorizontalScrollViewEx.this.getScrollX();

			// 缓慢滚动到指定位置
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					Math.abs(delta) * 3);

			// refresh
			invalidate();

			// delta>0 stands for user scroll view to right
			if (delta > 0)
				mScrollToRight = true;
			else {
				mScrollToRight = false;
			}

			mWhichScreen = whichScreen;
			mWhich = whichScreen;
			// 切换小圆点
			switchScreenPosition(mWhichScreen);
		}

		/**
		 * 用户轻触触摸屏，由1个MotionEvent ACTION_DOWN触发
		 */
		@Override
		public boolean onDown(MotionEvent e) {
			Log.i("MyGesture", "onDown");

			mScrollX = HorizontalScrollViewEx.this.getScrollX();

			return true;
		}

		/**
		 * 用户轻触触摸屏，尚未松开或拖动，由一个1个MotionEvent ACTION_DOWN触发
		 * 注意和onDown()的区别，强调的是没有松开或者拖动的状态
		 */
		public void onShowPress(MotionEvent e) {
			Log.i("MyGesture", "onShowPress");
		}

		/**
		 * 用户（轻触触摸屏后）松开，由一个1个MotionEvent ACTION_UP触发
		 */
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i("MyGesture", "onSingleTapUp");
			if (mBannerItemsList == null
					|| mBannerItemsList.size() <= mWhichScreen)
				return false;

			BannerItem bannerItem = mBannerItemsList.get(mWhichScreen);

			if (bannerItem != null) {
				BannerMotionEvent bannerMotionEvent = new BannerMotionEvent(
						mWhichScreen, bannerItem.action, bannerItem.url,
						bannerItem.gameId, bannerItem.gameType,
						bannerItem.title);
				mBannerClickListener.onBannerClick(bannerMotionEvent);
			}

			return false;
		}

		/**
		 * 用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE,
		 * 1个ACTION_UP触发
		 */
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i("MyGesture", "onFling velocityX=" + velocityX);

			mWhichScreen = velocityX > 0 ? mWhichScreen - 1 : mWhichScreen + 1;
			switchView(mWhichScreen);

			return true;
		}

		/**
		 * 用户按下触摸屏，并拖动，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE触发
		 */
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.i("MyGesture", "onScroll");

			// 禁止弹性滚动
			if (mOverScrollMode == false) {
				float x1 = e1.getX();
				float x2 = e2.getX();
				if (mWhichScreen == 0 && x1 < x2)
					return false;
				else if (mWhichScreen == PAGE_COUNT - 1 && x1 > x2)
					return false;
			}

			// int distance = Math.abs(getScrollX() - mWhichScreen *
			// mScreenWidth);
			// if ((mWhichScreen ==0 || mWhichScreen == PAGE_COUNT -1) &&
			// distance > mOverScrollDistance)
			// return false;

			this.scrollBy((int) distanceX, 0);

			return true;
		}

		/**
		 * 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发
		 */
		public void onLongPress(MotionEvent e) {
			Log.i("MyGesture", "onLongPress");
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mTimerResume = false;
				if (!mScroller.isFinished())
					mScroller.abortAnimation();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// 开始自动滑屏
				mTimerResume = true;
				mByUserAction = true;
			}

			boolean consume = mGestureDetector.onTouchEvent(event);

			if (consume == false && event.getAction() == MotionEvent.ACTION_UP) {
				int curScrollX = HorizontalScrollViewEx.this.getScrollX();
				int mWhichScreen = (curScrollX + mScreenWidth / 2)
						/ mScreenWidth;

				switchView(mWhichScreen);
			}

			return consume;
		}

		@Override
		public void computeScroll() {
			if (mScroller.computeScrollOffset()) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
				postInvalidate();
			}
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			if (changed) {
				int childLeft = 0;
				final int childCount = getChildCount();

				for (int i = 0; i < childCount; i++) {
					final View childView = getChildAt(i);
					if (childView.getVisibility() != View.GONE) {
						final int childWidth = childView.getMeasuredWidth();
						childView.layout(childLeft, 0, childLeft + childWidth,
								childView.getMeasuredHeight());
						childLeft += childWidth;
					}
				}
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			final int width = MeasureSpec.getSize(widthMeasureSpec);
			final int count = getChildCount();

			for (int i = 0; i < count; i++) {
				getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
			}
			scrollTo(mWhich * mScreenWidth, 0);
		}

	}

	/**
	 * override此函数，防止其修改构造方法所定义的宽和高<br/>
	 * 注意：在这里，设置宽和高将不起作用
	 */
	@Override
	public void setLayoutParams(android.view.ViewGroup.LayoutParams params) {
		params.width = mScreenWidth;
		params.height = mScreenHeight;

		super.setLayoutParams(params);
	}

	// 标志view AttachedToWindow
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mTimerResume = true;
	}

	// 标志view已经脱离window，典型的情形是view被销毁了，此时取消timer
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.d(TAG, "onDetachedFromWindow");

		mTimerResume = false;
		int activityId = ((BaseActivity) mContext).activityId();
		// 如果是账号管理页面 则释放内存
		if (activityId == BaseActivity.ACCOUNT_ID) {
			destroy();
		}
	}

	/**
	 * 销毁banner
	 */
	public void destroy() {
		mTimerTask.cancel();
		timer.cancel();
		// 去除各种bitmap对activity的引用关系
		destoryBitmaps();
		System.gc();
	}

	/**
	 * 去除各种bitmap对activity的引用关系
	 */
	private void destoryBitmaps() {
		for (View view : mLinearLayoutScreens) {
			Drawable drawable = view.getBackground();
			BitmapDrawable bitmapDrawable = null;
			if (drawable instanceof BitmapDrawable)
				bitmapDrawable = (BitmapDrawable) drawable;

			if (bitmapDrawable != null) {
				// 解除drawable对view的引用
				bitmapDrawable.setCallback(null);
				bitmapDrawable = null;
			}
		}

		for (ImageView imageView : mImageViewList) {
			Drawable drawable = imageView.getDrawable();
			if (drawable != null) {
				drawable.setCallback(null);
				drawable = null;
			}
		}

		mPageIndicator.setCallback(null);
		mPageIndicator = null;
		mPageIndicatorFocused.setCallback(null);
		mPageIndicatorFocused = null;

		mLinearLayoutScreens.clear();
		mLinearLayoutScreens = null;

		mBannerBitmaps.clear();
		mBannerBitmaps = null;

		mImageViewList.clear();
		mImageViewList = null;

		mBannerItemsList.clear();
		mBannerItemsList = null;
	}

	// 单击事件
	@Override
	public void onBannerClick(BannerMotionEvent bannerMotionEvent) {
		final int position = bannerMotionEvent.index;
		if (mContext == null)
			return;

		NotificationInfo notificationInfo = new NotificationInfo();
		notificationInfo.msgType = bannerMotionEvent.getAction();
		int action = bannerMotionEvent.getAction();
		if (action == NotificationInfo.NOTIFICATION_SINGLEGAME_MSG) // 单个游戏消息，直接启动该游戏
		{
			try {
				notificationInfo.gameId = Integer.parseInt(bannerMotionEvent
						.getGameId());
				notificationInfo.gameType = Integer.parseInt(bannerMotionEvent
						.getGameType());
			} catch (NumberFormatException e) {
				Log.e(TAG, e.toString());
				return;
			}
		} else if (action == NotificationInfo.NOTIFICATION_GAMEPAGE_MSG) // 游戏主页消息，通过客户端展示游戏主页
		{
			try {
				notificationInfo.gameId = Integer.parseInt(bannerMotionEvent
						.getGameId());
			} catch (NumberFormatException e) {
				Log.e(TAG, e.toString());
				return;
			}
			notificationInfo.issueTitle = bannerMotionEvent.getTitle();
		} else if (action == NotificationInfo.NOTIFICATION_SHOW_WEBVIEW_MSG) // 交叉推广消息，通过一个webview展示
		{
			notificationInfo.issueTitle = bannerMotionEvent.getTitle();
			notificationInfo.openUrl = bannerMotionEvent.getResponseUrl();
		} else // reserved
		{
			return;
		}

		Intent intent = notificationInfo.generateIntent(mContext);
		if (intent != null)
			mContext.startActivity(intent);
	}

	/**
	 * ScrollBanner所关联的banner项 可以为多个 一个为一屏
	 */
	public static class BannerItem extends Object {
		public static final String ACTION = "action";
		public static final String URL = "url";
		public static final String IMGURL = "imgurl";
		public static final String GAMEID = "gameid";
		public static final String GAMETYPE = "gametype";
		public static final String TITLE = "title";

		public int index = -1;
		public int action = -1;
		public String url = "";
		public String imgUrl = "";
		public String gameId = "";
		public String gameType = "";
		public String title = "";

		public BannerItem() {
		}
	}

	/**
	 * BannerMotionEvent：单击banner所产生的事件对象<br/>
	 * getAction()来获取动作类别<br/>
	 * getResponseUrl()来获取响应url<br/>
	 * ...
	 */
	public static class BannerMotionEvent extends Object {
		/**
		 * ACTION_PLAY_FLASH： 播放游戏
		 */
		public static final int ACTION_PLAY = 2;
		/**
		 * ACTION_HOMEPAGE：打开官网
		 */
		public static final int ACTION_HOMEPAGE = 3;
		/**
		 * ACTION_OPEN_URL：打开指定url
		 */
		public static final int ACTION_OPEN_URL = 4;

		// banner中屏幕的index
		private int index = -1;
		// 响应url
		private String responseUrl = "";
		// 动作种类
		private int action = -1;
		// gameid
		private String gameId = "";
		// gametype flash游戏(0) or h5游戏(1)
		private String gameType = "";
		// webview的标题
		private String title = "";

		public BannerMotionEvent(int index, int action, String responseUrl,
				String gameId, String gameType, String title) {
			BannerMotionEvent.this.index = index;
			BannerMotionEvent.this.action = action;
			BannerMotionEvent.this.responseUrl = responseUrl;
			BannerMotionEvent.this.gameId = gameId;
			BannerMotionEvent.this.gameType = gameType;
			BannerMotionEvent.this.title = title;
		}

		/**
		 * 获取当前BannerMotionEvent事件对象的动作种类
		 * 
		 * @return 动作种类：ACTION_PLAY等
		 */
		public int getAction() {
			return action;
		}

		/**
		 * 获取当前BannerMotionEvent事件对象的title
		 * 
		 * @return title webview的标题
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * 获取当前BannerMotionEvent事件对象的gameId
		 * 
		 * @return gameId
		 */
		public String getGameId() {
			return gameId;
		}

		/**
		 * 获取当前BannerMotionEvent事件对象的gameType
		 * 
		 * @return gameType 0 or 1
		 */
		public String getGameType() {
			return gameType;
		}

		/**
		 * 获取当前BannerMotionEvent事件对象的响应url
		 * 
		 * @return 响应url
		 */
		public String getResponseUrl() {
			return responseUrl;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public String toString() {
			return String
					.format("BannerMotionEvent { index=%d, action=%d, responseUrl=%s, gameId=%s, gameType=%s, title=%s }",
							index, action, responseUrl, gameId, gameType, title);
		}
	}

	@Override
	public void onBannerInfoSuccess(List<BannerItem> items) {
		Log.d(TAG, "onBannerInfoSuccess");
		mBannerItemsList = items;
		mHandler.sendEmptyMessage(MESSAGE_FETCH_BANNER_SUCCESS);
	}

	@Override
	public void onBannerInfoFailed() {
		Log.e(TAG, "onBannerInfoFailed");
	}

}