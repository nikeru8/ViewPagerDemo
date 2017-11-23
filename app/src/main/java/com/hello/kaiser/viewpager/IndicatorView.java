package com.hello.kaiser.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class IndicatorView extends View implements ViewPager.OnPageChangeListener{

    private Drawable mIndicator; //指示器的显示样式
    private int mIndicatorSize; //指示器的大小
    private boolean mSmooth; //是否显示滑动效果
    private int mMargin;  //指示器之间的间距

    private int mDefaultMargin; //默认的间距
    private int mSelectedItem; //选中的指示器

    private int mWidth; //测量后得到的控件的宽度
    private int mCount; //viewpager的页数，即指示器的个数
    private int mContextWidth; //实际计算得到的控件的宽度
    private float mOffset; //滑动偏移量


    private ViewPager.OnPageChangeListener mPageChangeListener;

    public IndicatorView(Context context) {
        this(context,null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        //设置指示器大小的默认值
        mIndicatorSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,16,getResources().getDisplayMetrics());
        //设置指示器间距的默认值
        mDefaultMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,getResources().getDisplayMetrics());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView,defStyleAttr,0);

        mIndicator = typedArray.getDrawable(R.styleable.IndicatorView_indicatorIcon);
        mMargin = (int) typedArray.getDimension(R.styleable.IndicatorView_indicatorMargin,mDefaultMargin);
        mSmooth = typedArray.getBoolean(R.styleable.IndicatorView_indicatorSmooth,true);
        mIndicatorSize = (int) typedArray.getDimension(R.styleable.IndicatorView_indicatorSize,mIndicatorSize);

        typedArray.recycle();

        mIndicator.setBounds(0,0,mIndicatorSize,mIndicatorSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),measureHeight(heightMeasureSpec));
    }

    /**
     * 测量宽
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        final int mode = MeasureSpec.getMode(widthMeasureSpec);
        final int size = MeasureSpec.getSize(widthMeasureSpec);

        int width;
        //计算整个控件的宽度，其中 mCount 是获取到的ViewPager的页数
        int desired = getPaddingLeft() + mIndicatorSize * mCount + mMargin * (mCount - 1) + getPaddingRight();
        mContextWidth = desired;

        if (mode == MeasureSpec.EXACTLY) {
            //如果是match_parent,则选取屏幕宽度和计算得到的宽度中较大的那个作为控件的宽度
            width = Math.max(desired,size);
        }else {
            if (mode == MeasureSpec.AT_MOST) {
                //如果是wrap_parent,则选取屏幕宽度和计算得到的宽度中较小的那个作为控件的宽度
                width = Math.min(desired,size);
            }else {
                //还有一种情况是 UNSPECIFIED ，一般是系统内部测量的过程中使用，这种情况也可以忽略掉
                width = desired;
            }
        }

        mWidth = width;
        return width;
    }

    /**
     * 测量高
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        final int mode = MeasureSpec.getMode(heightMeasureSpec);
        final int size = MeasureSpec.getSize(heightMeasureSpec);

        int height;
        int desired = getPaddingTop() + mIndicatorSize + getPaddingBottom();

        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        }else {
            if (mode == MeasureSpec.AT_MOST) {
                height = Math.min(desired,size);
            }else {
                height = desired;
            }
        }

        return height;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();
        /**
         * 这里是计算绘制控件的x轴上的起点位置
         * 当wrap_content并且控件宽度小于屏幕宽度时，计算得到的宽度和实际宽度相同，mWidth/2 - mContextWidth/2 = 0，这种情况很好理解
         * 再看当match_content的情况，控件的计算宽度小于屏幕宽度，则mWidth > mContextWidth,则计算得到的left是计算得到的控件距离屏幕左端的距离，
         * 控件将绘制在屏幕水平方向的中间，不清楚可以在纸上画下，其他情况也类似
         */
        int left = mWidth/2 - mContextWidth/2 + getPaddingLeft();
        canvas.translate(left,getPaddingTop());
        //依次绘制指示器
        for (int i = 0 ; i < mCount ; i++) {
            mIndicator.setState(EMPTY_STATE_SET);
            mIndicator.draw(canvas);
            canvas.translate(mIndicatorSize + mMargin,0);
        }
        canvas.restore();

        /**
         * 下面是绘制选中的指示器的样式
         */
        float leftDraw = ( mIndicatorSize + mMargin ) * ( mSelectedItem + mOffset );
        canvas.translate(left,getPaddingTop());
        canvas.translate(leftDraw,0);
        mIndicator.setState(SELECTED_STATE_SET);
        mIndicator.draw(canvas);

    }

    public void setViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            return;
        }
        PagerAdapter pagerAdapter = viewPager.getAdapter();
        if (pagerAdapter == null) {
            throw new RuntimeException("请先设置viewpager的adaper");
        }
        mCount = pagerAdapter.getCount();
        viewPager.addOnPageChangeListener(this);
        mSelectedItem = viewPager.getCurrentItem();
        invalidate();
    }

    /**
     * 因为自定义控件注册了viewpager的滑动监听，
     * 为了外部能够同样监听到滑动事件，所以提供该接口
     * @param pageChangeListener
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener pageChangeListener) {
        this.mPageChangeListener = pageChangeListener;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mSmooth){
            //允许显示滑动效果
            mSelectedItem = position;
            mOffset = positionOffset;
            //通知view重绘
            invalidate();
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageScrolled(position,positionOffset,positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        //当有page被选中后通知view重绘
        mSelectedItem = position ;
        invalidate();

        if(mPageChangeListener != null){
            mPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if(mPageChangeListener != null){
            mPageChangeListener.onPageScrollStateChanged(state);
        }
    }
}
