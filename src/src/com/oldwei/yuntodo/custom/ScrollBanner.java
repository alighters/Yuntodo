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
 * ScrollBanner ֧�ֻ���Ч����FrameLayout���࣬��������Ļ�������ߴ硣<br/>
 * ���͵��÷���<br/>
 * ScrollBanner scrollBanner = new ScrollBanner(this, mScreenWidth, 100, this);<br/>
 * linearLayout.addView(scrollBanner);<br/>
 * ע�����<br/>
 * 1.�����������ScrollBanner��LayoutParams��������еĿ�͸����Խ������ԣ���Ȼ���ö���ʵ�����Ŀ�͸�<br/>
 * 2.����¼��Ļص������Ϊnull�������Ĭ�ϵ��¼��ص�<br/>
 * 3.ͨ��setOverScrollMode������ banner�Ƿ��ܹ�������Ļ�ı߽�<br/>
 * 4ͨ��xml��ʽ����banner����Ҫ�������µ��ã�<br/>
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

	// ScrollBanner����view
	private LinearLayout linearLayoutScrolLayout;

	// linearLayoutScrolLayout����view�����ڷ������ɸ�СԲ��
	private LinearLayout linearLayoutForDot;

	private Scroller mScroller;
	private Context mContext;
	private OnBannerClickListener mBannerClickListener;

	// ��Ļ����bitmap
	private List<View> mLinearLayoutScreens = new ArrayList<View>();
	private List<Bitmap> mBannerBitmaps = new ArrayList<Bitmap>();

	// banner��Ϣ
	private List<BannerItem> mBannerItemsList = new ArrayList<BannerItem>();
	// СԲ��
	private List<ImageView> mImageViewList = new ArrayList<ImageView>();
	private Drawable mPageIndicator;
	private Drawable mPageIndicatorFocused;

	// bannerĬ��ͼƬ
	private Bitmap mDefaultBitmap;

	private int mScreenWidth;
	private int mScreenHeight;
	private int mScrollX;

	// current screen index
	private int mWhich = 0;

	public static final int MESSAGE_AUTO_SCROLL = 1;

	public static final int MESSAGE_FETCH_BANNER_SUCCESS = 2;

	public static final int MARGIN_BOTTOM = 2;

	// 480*150 banner��ͼƬ�ߴ� 150.0/480=0.3125f
	public static final float ratio = 0.3125f;

	// banner��λ��
	private int mLocation = -1;

	// banner��Ϊ����
	private int PAGE_COUNT = 4;

	// �������� �Ƿ����һ���
	private boolean mScrollToRight = true;

	// �Ƿ��Զ�����
	private boolean mTimerResume = true;

	// ��־�û��Ƿ��ֶ���������Ļ
	private boolean mByUserAction = false;

	// ��־banner�Ƿ���Ի����߽�
	private boolean mOverScrollMode = false;
	// ��־banner���Ի����߽��������
	private int mOverScrollDistance = 0;

	// ��ʱ�� ����banner���Զ�����
	final Timer timer = new Timer();

	// ��ʱ����ʱ���� ��λ��ms
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

	// ScrollBanner˽��handler ���ڴ����ڲ��߼�
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// ��ʾ�Ѿ�ִ����onDetachedFromWindow��banner�Ѿ���������
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
					// �����banner ��ʾ��
					ScrollBanner.this.show(true);
				}
				// �����̨���ص�banneritem����������Ԥ��ֵ4
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

	// ���ڻ�ȡbitmap
	private Handler mBitmapHandler = new Handler() {

		public void handleMessage(Message msg) {
			// ��ʾ�Ѿ�ִ����onDetachedFromWindow��banner�Ѿ���������
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
	 *            activityʵ��
	 * @param width
	 *            banner�Ŀ�� ��λpx
	 * @param height
	 *            banner�ĸ߶� ��λdip,-1��ʾ����ͼƬ��������Ӧ�߶�
	 * @param bannerClickListener
	 *            ����banner�Ļص��ӿ�
	 */
	public ScrollBanner(Context context, final int width, final int height,
			OnBannerClickListener bannerClickListener) {
		this(context, null);

		int activityId = ((BaseActivity) context).activityId();
		if (activityId == BaseActivity.ACCOUNT_ID)// λ��3
			mLocation = 3;
		else if (activityId == BaseActivity.GAMEZONE_ID)// λ��2
		{
			mLocation = 2;
		}

		// ��ʼ��ʱ����ʾbanner
		this.show(false);
		setResolution(width, height);
		setOnBannerClickListener(bannerClickListener);
		setDefaultBannerImages();
		fetchBannerInfo();
	}

	/**
	 * ͨ��xml��ʽ����banner��������ô˷���������ʾ
	 */
	public void showBanner() {
		int activityId = ((BaseActivity) mContext).activityId();
		if (activityId == BaseActivity.ACCOUNT_ID)// λ��3
			mLocation = 3;
		else if (activityId == BaseActivity.GAMEZONE_ID)// λ��2
		{
			mLocation = 2;
		}

		setDefaultBannerImages();
		fetchBannerInfo();
	}

	/**
	 * ��ͣ����
	 */
	public void pauseScroll() {
		mTimerResume = false;
	}

	/**
	 * �ָ�����
	 */
	public void resumeScroll() {
		mTimerResume = true;
	}

	/**
	 * ���ûص��ӿ�
	 * 
	 * @param callBack
	 *            ����banner�Ļص��ӿ�
	 */
	public void setOnBannerClickListener(
			OnBannerClickListener bannerClickListener) {
		mBannerClickListener = (bannerClickListener != null ? bannerClickListener
				: ScrollBanner.this);
	}

	/**
	 * ����banner�Ľ�����
	 * 
	 * @param width
	 *            banner�Ŀ��
	 * @param height
	 *            banner�ĸ߶�
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
	 * ��ȡbanner�ĸ߶�
	 * 
	 * @return banner�ĸ߶� ��λ��px
	 */
	public int getHeightPixels() {
		return mScreenHeight;
	}

	/**
	 * ����banner�Ƿ���Ե��Ի����߽�
	 * 
	 * @param canOverScroll
	 *            true��ʾ���Ի����߽磬false����
	 */
	public void setOverScrollMode(boolean canOverScroll) {
		mOverScrollMode = canOverScroll;
		if (canOverScroll == false)
			mOverScrollDistance = 0;
	}

	/**
	 * ���̨��ȡbanner�ĸ�����Ϣ
	 */
	private void fetchBannerInfo() {
		NetworkManager netManager = (NetworkManager) AppEngine.getInstance()
				.getManager(IManager.NETWORK_ID);
		netManager.getBannerInfo(String.valueOf(mLocation), ScrollBanner.this);
	}

	/**
	 * ��ȡbanner�Ļ���ͼ��
	 */
	private void setDefaultBannerImages() {
		// Ϊbanner����Ĭ��bitmap
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inJustDecodeBounds = false;
		bitmapFactoryOptions.inSampleSize = 2;

		Resources res = mContext.getResources();
		mDefaultBitmap = BitmapFactory.decodeResource(res,
				R.drawable.banner_image_default, bitmapFactoryOptions);

		for (int i = 0; i < PAGE_COUNT; i++)
			mBannerBitmaps.add(i, mDefaultBitmap);

		// ��ʼ��BannerItem����
		for (int i = 0; i < PAGE_COUNT; i++)
			mBannerItemsList.add(i, null);

		setBannerImages(-1);
	}

	private void fetchBannerImages() {
		// ��ʾ�Ѿ�ִ����onDetachedFromWindow��banner�Ѿ���������
		if (mBannerItemsList == null)
			return;

		// ImageManager ����url�����ȡbitmap
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
			// ImageManagerΪ���̣߳����ó���������cache���ԣ��ڴ桢�ļ������磩
			if (item != null && item.imgUrl != null)
				imageManager.loadBitmap(item.imgUrl, mBitmapHandler);
		}
	}

	/**
	 * ����banner�Ļ���ͼ��
	 * 
	 * @param position
	 *            ���position=-1�����ʾ����ȫ��bitmap
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
	 * �Ƿ���ʾbanner
	 * 
	 * @param isShow
	 *            true��ʾ false����ʾ
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
	 * �л���ָ����Ļ
	 * 
	 * @param which
	 *            ��Ļ����
	 */
	public void switchToScreen(final int which) {
		mHorizontalScrollViewEx.switchView(which);
	}

	/**
	 * ������Ļ������ ���˺����ݲ����ţ�
	 * 
	 * @param count
	 *            ��Ļ����
	 */
	protected void setScreenCount(final int count) {
		PAGE_COUNT = count;
	}

	/**
	 * ����ƫ�Ƶľ��� ���mOverScrollModeΪfalse�����������Ч ���˺����ݲ����ţ�
	 * 
	 * @param distance
	 */
	protected void setOverScrollDistance(int distance) {
		if (distance < 0)
			distance = 0;

		mOverScrollDistance = mOverScrollMode ? distance : 0;
	}

	/**
	 * �л�СԲ��
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
	 * ��ʼ������FrameLayout��ͼ��
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
		// ���android1.6�����µ����⴦�� ��Ϊandroid�ĵͰ汾bug
		if (mVersionCode <= 5) {
			linearLayoutScrolLayout.setBaselineAligned(false);
		}

		// ��ʼ���ĸ�����view
		for (int i = 0; i < PAGE_COUNT; i++) {
			LinearLayout linearLayoutScreen = new LinearLayout(mContext);
			linearLayoutScreen.setOrientation(LinearLayout.VERTICAL);
			linearLayoutScrolLayout.addView(linearLayoutScreen,
					new LayoutParams(mScreenWidth, LayoutParams.FILL_PARENT));

			mLinearLayoutScreens.add(i, linearLayoutScreen);
		}

		// ��ʼ��СԲ����ͼ
		RelativeLayout relativeLayout = new RelativeLayout(mContext);
		relativeLayout.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		// linearLayoutForDotΪСԲ����ͼ
		linearLayoutForDot = new LinearLayout(mContext);
		android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// СԲ���ײ��ľ��� ��λ:px
		layoutParams.bottomMargin = MARGIN_BOTTOM;
		layoutParams.rightMargin = MARGIN_BOTTOM;
		layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
		linearLayoutForDot.setLayoutParams(layoutParams);
		linearLayoutForDot.setOrientation(LinearLayout.HORIZONTAL);
		linearLayoutForDot.setHorizontalGravity(Gravity.CENTER);
		linearLayoutForDot.setVerticalGravity(Gravity.CENTER);
		// ��������ʵ��Բ�ǰ�͸��Ч�� ������
		// linearLayoutForDot.setBackgroundResource(R.drawable.round_corner_bg);
		// linearLayoutForDot.getBackground().setAlpha(100);

		// ��ʼ��4��СԲ��
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

		// �Զ����� 5��һ��
		timer.schedule(mTimerTask, 5000, TIMER_DURATION);
	}

	/**
	 * ��һ��bannerҳ�� TODO�˺���д�Ĳ���
	 */
	private void addBannerItem() {
		// ��ʾ�Ѿ�ִ����onDetachedFromWindow��banner�Ѿ���������
		if (mBannerBitmaps == null || mLinearLayoutScreens == null
				|| mImageViewList == null || mContext == null)
			return;

		// ������Ļ�������ܿ��
		PAGE_COUNT += 1;
		mHorizontalScrollViewEx.getLayoutParams().width = mScreenWidth
				* PAGE_COUNT;

		// ����Ĭ��ͼƬ��Դ
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
		// ��һ����Ļ
		LinearLayout linearLayoutScreen = new LinearLayout(mContext);
		linearLayoutScreen.setOrientation(LinearLayout.VERTICAL);
		linearLayoutScreen.setBackgroundDrawable(new BitmapDrawable(
				mBannerBitmaps.get(PAGE_COUNT - 1)));
		linearLayoutScrolLayout.addView(linearLayoutScreen, new LayoutParams(
				mScreenWidth, LayoutParams.FILL_PARENT));
		mLinearLayoutScreens.add(linearLayoutScreen);

		// ��һ��СԲ��
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
			// ���������Ļ���޷��϶�������
			mGestureDetector.setIsLongpressEnabled(false);

			// ���쵯�Ի�������
			mScroller = new Scroller(context);
		}

		/**
		 * �л���ָ����Ļ
		 * 
		 * @param whichScreen
		 *            ��Ļindex
		 */
		public void switchView(int whichScreen) {
			if (mLinearLayoutScreens == null)
				return;

			// ��ֹ�Ƿ�����
			if (whichScreen < 0)
				whichScreen = 0;
			else if (whichScreen >= PAGE_COUNT)
				whichScreen = PAGE_COUNT - 1;

			Log.i(TAG, "switch view to " + whichScreen);

			int delta = whichScreen * mScreenWidth
					- HorizontalScrollViewEx.this.getScrollX();

			// ����������ָ��λ��
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
			// �л�СԲ��
			switchScreenPosition(mWhichScreen);
		}

		/**
		 * �û��ᴥ����������1��MotionEvent ACTION_DOWN����
		 */
		@Override
		public boolean onDown(MotionEvent e) {
			Log.i("MyGesture", "onDown");

			mScrollX = HorizontalScrollViewEx.this.getScrollX();

			return true;
		}

		/**
		 * �û��ᴥ����������δ�ɿ����϶�����һ��1��MotionEvent ACTION_DOWN����
		 * ע���onDown()������ǿ������û���ɿ������϶���״̬
		 */
		public void onShowPress(MotionEvent e) {
			Log.i("MyGesture", "onShowPress");
		}

		/**
		 * �û����ᴥ���������ɿ�����һ��1��MotionEvent ACTION_UP����
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
		 * �û����´������������ƶ����ɿ�����1��MotionEvent ACTION_DOWN, ���ACTION_MOVE,
		 * 1��ACTION_UP����
		 */
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i("MyGesture", "onFling velocityX=" + velocityX);

			mWhichScreen = velocityX > 0 ? mWhichScreen - 1 : mWhichScreen + 1;
			switchView(mWhichScreen);

			return true;
		}

		/**
		 * �û����´����������϶�����1��MotionEvent ACTION_DOWN, ���ACTION_MOVE����
		 */
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.i("MyGesture", "onScroll");

			// ��ֹ���Թ���
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
		 * �û��������������ɶ��MotionEvent ACTION_DOWN����
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
				// ��ʼ�Զ�����
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
	 * override�˺�������ֹ���޸Ĺ��췽��������Ŀ�͸�<br/>
	 * ע�⣺��������ÿ�͸߽���������
	 */
	@Override
	public void setLayoutParams(android.view.ViewGroup.LayoutParams params) {
		params.width = mScreenWidth;
		params.height = mScreenHeight;

		super.setLayoutParams(params);
	}

	// ��־view AttachedToWindow
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mTimerResume = true;
	}

	// ��־view�Ѿ�����window�����͵�������view�������ˣ���ʱȡ��timer
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.d(TAG, "onDetachedFromWindow");

		mTimerResume = false;
		int activityId = ((BaseActivity) mContext).activityId();
		// ������˺Ź���ҳ�� ���ͷ��ڴ�
		if (activityId == BaseActivity.ACCOUNT_ID) {
			destroy();
		}
	}

	/**
	 * ����banner
	 */
	public void destroy() {
		mTimerTask.cancel();
		timer.cancel();
		// ȥ������bitmap��activity�����ù�ϵ
		destoryBitmaps();
		System.gc();
	}

	/**
	 * ȥ������bitmap��activity�����ù�ϵ
	 */
	private void destoryBitmaps() {
		for (View view : mLinearLayoutScreens) {
			Drawable drawable = view.getBackground();
			BitmapDrawable bitmapDrawable = null;
			if (drawable instanceof BitmapDrawable)
				bitmapDrawable = (BitmapDrawable) drawable;

			if (bitmapDrawable != null) {
				// ���drawable��view������
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

	// �����¼�
	@Override
	public void onBannerClick(BannerMotionEvent bannerMotionEvent) {
		final int position = bannerMotionEvent.index;
		if (mContext == null)
			return;

		NotificationInfo notificationInfo = new NotificationInfo();
		notificationInfo.msgType = bannerMotionEvent.getAction();
		int action = bannerMotionEvent.getAction();
		if (action == NotificationInfo.NOTIFICATION_SINGLEGAME_MSG) // ������Ϸ��Ϣ��ֱ����������Ϸ
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
		} else if (action == NotificationInfo.NOTIFICATION_GAMEPAGE_MSG) // ��Ϸ��ҳ��Ϣ��ͨ���ͻ���չʾ��Ϸ��ҳ
		{
			try {
				notificationInfo.gameId = Integer.parseInt(bannerMotionEvent
						.getGameId());
			} catch (NumberFormatException e) {
				Log.e(TAG, e.toString());
				return;
			}
			notificationInfo.issueTitle = bannerMotionEvent.getTitle();
		} else if (action == NotificationInfo.NOTIFICATION_SHOW_WEBVIEW_MSG) // �����ƹ���Ϣ��ͨ��һ��webviewչʾ
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
	 * ScrollBanner��������banner�� ����Ϊ��� һ��Ϊһ��
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
	 * BannerMotionEvent������banner���������¼�����<br/>
	 * getAction()����ȡ�������<br/>
	 * getResponseUrl()����ȡ��Ӧurl<br/>
	 * ...
	 */
	public static class BannerMotionEvent extends Object {
		/**
		 * ACTION_PLAY_FLASH�� ������Ϸ
		 */
		public static final int ACTION_PLAY = 2;
		/**
		 * ACTION_HOMEPAGE���򿪹���
		 */
		public static final int ACTION_HOMEPAGE = 3;
		/**
		 * ACTION_OPEN_URL����ָ��url
		 */
		public static final int ACTION_OPEN_URL = 4;

		// banner����Ļ��index
		private int index = -1;
		// ��Ӧurl
		private String responseUrl = "";
		// ��������
		private int action = -1;
		// gameid
		private String gameId = "";
		// gametype flash��Ϸ(0) or h5��Ϸ(1)
		private String gameType = "";
		// webview�ı���
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
		 * ��ȡ��ǰBannerMotionEvent�¼�����Ķ�������
		 * 
		 * @return �������ࣺACTION_PLAY��
		 */
		public int getAction() {
			return action;
		}

		/**
		 * ��ȡ��ǰBannerMotionEvent�¼������title
		 * 
		 * @return title webview�ı���
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * ��ȡ��ǰBannerMotionEvent�¼������gameId
		 * 
		 * @return gameId
		 */
		public String getGameId() {
			return gameId;
		}

		/**
		 * ��ȡ��ǰBannerMotionEvent�¼������gameType
		 * 
		 * @return gameType 0 or 1
		 */
		public String getGameType() {
			return gameType;
		}

		/**
		 * ��ȡ��ǰBannerMotionEvent�¼��������Ӧurl
		 * 
		 * @return ��Ӧurl
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