package com.gzn.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 瀑布流布局，仅供练习娱乐
 */
public class WaterFallFlowLayout extends ViewGroup {

    /**
     * 行高记录
     */
    private List<Integer> lineHeights = new ArrayList<>();
    /**
     * 用来保存每一行的视图
     */
    private List<List<View>> lineViews = new ArrayList<>();

    public WaterFallFlowLayout(Context context) {
        super(context);
    }

    public WaterFallFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaterFallFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //得到的是父布局的大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //记录当前布局的宽高
        int measureWidth = 0;
        int measureHeight = 0;

        //当前行宽，行高，因为存在多行，下一行数据要放在下方，行高需要保存
        int currLineWidth = 0;
        int currLineHeight = 0;

        //1.确认自己当前空间的宽高，这里因为会有两次OnMeasure,进行二级测量优化，所以采用IF_ELSE结构
        //二级优化原理在源码具体Draw时，第一次不会直接进行performDraw的调用反而是在下面重新进行了一次scheduleTraversals
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            measureHeight = heightSize;
            measureWidth = widthSize;
        } else {
            //当前View的宽高
            int childWidth = 0;
            int childHeight = 0;

            //获取所有的子View数量
            int childCount = getChildCount();

            //单行View的容器
            List<View> list = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                //测量子View
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                //获取xml资源
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

                //获取child的实际的宽高
                childWidth = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                childHeight = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

                //判断是否换行
                if (childWidth + currLineWidth > widthSize) {
                    //记录布局宽高信息
                    measureHeight += currLineHeight;
                    measureWidth = Math.max(measureWidth, currLineWidth);

                    //保存这行数据
                    lineHeights.add(currLineHeight);
                    lineViews.add(list);

                    //设置下一行的宽高
                    currLineHeight = childHeight;
                    currLineWidth = childWidth;

                    //将这个View添加到下一行中
                    list = new ArrayList<>();
                    list.add(child);
                } else {
                    currLineWidth += childWidth;
                    currLineHeight = Math.max(currLineHeight, childHeight);

                    list.add(child);
                }

                if (i == childCount - 1) {
                    measureHeight += currLineHeight;
                    measureWidth = Math.max(measureWidth, currLineWidth);

                    lineViews.add(list);
                    lineHeights.add(currLineHeight);
                }

            }
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, top, right, bottom;

        int currTop = 0;
        int currLeft = 0;

        int lineCount = lineHeights.size();
        for (int i = 0; i < lineCount; i++) {
            List<View> views = lineViews.get(i);

            int viewsCount = views.size();
            for (int j = 0; j < viewsCount; j++) {
                View view = views.get(j);
                MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();

                left=currLeft+layoutParams.leftMargin;
                top = currTop + layoutParams.topMargin;

                right = left + view.getMeasuredWidth();
                bottom = top + view.getMeasuredHeight();

                view.layout(left, top, right, bottom);
                currLeft += view.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }

            currLeft=0;
            currTop += lineHeights.get(i);
        }

        lineHeights.clear();
        lineViews.clear();
    }

    /**
     * 这个方法主要是用于父容器添加子View时调用。
     * 用于生成和此容器类型相匹配的布局参数类。
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
