package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

/**
 * Tab��ʽ�Ĳ�����
 * 
 * @author Wendell
 * @version 4.1
 */
public class TabLayout extends ViewGroup
{

    public static final String     HEAD_POSITION_TOP     = "top";
    public static final String     HEAD_POSITION_BOTTOM  = "bottom";
    public static final String     HEAD_POSITION_LEFT    = "left";
    public static final String     HEAD_POSITION_RIGHT   = "right";

    protected Class<?>             tabClass              = Button.class;
    protected String               headPosition          = HEAD_POSITION_TOP;
    protected int                  selectedTabIndex      = -1;
    protected boolean              mIsLayout             = false;

    protected ViewGroup            head                  = null;
    /** ֮����contentʹ��FrameLayout������Ϊֻ��ʹ��setVisibility(View.INVISIBLE)��ʵ��Tab�л���ʹ��View.GONE��ʹ��������layout��Ӱ�����ܣ��ҿ��ܻ��UI���´������⣬��ͨ��notifyDataSetChanged()����ΪGONE��AdapterViewʱ�����ܻ�ʹAdapter��AdapterView��״̬��ͬ�� */
    protected FrameLayout          content               = null;
    protected List<View>           tabs                  = new ArrayList<View>();

    protected OnTabChangedListener mOnTabChangedListener = null;

    public TabLayout(Context context)
    {
        this(context, null, 0);
    }

    public TabLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (attrs != null)
        {
            // TabLayout֧�����¶�������
            String tabClassName = attrs.getAttributeValue(null, "tabClass");
            String headPosition = attrs.getAttributeValue(null, "headPosition");
            try
            {
                if (tabClassName != null)
                    setTabClass(Class.forName(tabClassName));
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            if (headPosition != null)
                setHeadPosition(headPosition);
        }
    }

    public void setTabClass(Class<?> tabClass)
    {
        if (tabClass == null)
            throw new NullPointerException();
        this.tabClass = tabClass;
    }

    public void setHeadPosition(String headPosition)
    {
        if (headPosition == null)
            throw new NullPointerException();
        if (!headPosition.equals(HEAD_POSITION_TOP) && !headPosition.equals(HEAD_POSITION_BOTTOM) && !headPosition.equals(HEAD_POSITION_LEFT) && !headPosition.equals(HEAD_POSITION_RIGHT))
            throw new IllegalArgumentException("headPosition is invalid!");
        this.headPosition = headPosition;
    }

    /**
     * <p>ˢ�²���
     */
    protected void refreshLayout()
    {
        if (getChildCount() != 2)
            throw new IllegalStateException("TabLayout can only contains two children(head and content)!");
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        if (headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_LEFT))
        {
            if (!(child1 instanceof ViewGroup))
                throw new IllegalStateException("TabLayout��s head child should be a ViewGroup!");
            if (!(child2 instanceof FrameLayout))
                throw new IllegalStateException("TabLayout��s content child should be a FrameLayout!");
            head = (ViewGroup) child1;
            content = (FrameLayout) child2;
        } else if (headPosition.equals(HEAD_POSITION_BOTTOM) || headPosition.equals(HEAD_POSITION_RIGHT))
        {
            if (!(child2 instanceof ViewGroup))
                throw new IllegalStateException("TabLayout��s head child should be a ViewGroup!");
            if (!(child1 instanceof FrameLayout))
                throw new IllegalStateException("TabLayout��s content child should be a FrameLayout!");
            head = (ViewGroup) child2;
            content = (FrameLayout) child1;
        }
        tabs.clear();
        refreshTabs(head);
        int tabSize = tabs.size();
        int contentSize = content.getChildCount();
        while (tabSize > contentSize)
        {
            tabs.remove(tabSize - 1).setOnClickListener(null);
            tabSize = tabs.size();
        }
    }

    /**
     * <p>���õݹ鷽ʽˢ�²����а���������Tab����Ҫ���ⲿ�����ԭ��tabs�е���������
     * 
     * @param view
     */
    protected void refreshTabs(View view)
    {
        if (view.getClass().equals(tabClass))
        {
            tabs.add(view);
            final int index = tabs.size() - 1;
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO Auto-generated method stub
                    setSelectedTab(index);
                }
            });
        } else if (view instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++)
            {
                refreshTabs(vg.getChildAt(i));
            }
        }
    }

    public void setSelectedTab(int index)
    {
        if (!mIsLayout)
            refreshLayout();
        if (index < 0 || index >= tabs.size())
            throw new IllegalArgumentException("index is out of range:" + index + "!");
        if (index == selectedTabIndex)
            return;
        for (int i = 0; i < tabs.size(); i++)
        {
            View tabView = tabs.get(i);
            View contentView = content.getChildAt(i);
            if (index == i)
            {
                if (tabView instanceof CompoundButton)
                    ((CompoundButton) tabView).setChecked(true);
                contentView.setVisibility(View.VISIBLE);
            } else
            {
                if (tabView instanceof CompoundButton)
                    ((CompoundButton) tabView).setChecked(false);
                contentView.setVisibility(View.INVISIBLE);
            }
        }
        this.selectedTabIndex = index;
        if (mOnTabChangedListener != null)
        {
            mOnTabChangedListener.onTabChanged(tabs.get(index), content.getChildAt(index), index);
        }
    }

    public int getSelectedTabIndex()
    {
        return selectedTabIndex;
    }

    public ViewGroup getHead()
    {
        return head;
    }

    public FrameLayout getContent()
    {
        return content;
    }

    public List<View> getTabs()
    {
        List<View> returnCopy = new ArrayList<View>(tabs.size());
        for (View view : tabs)
        {
            returnCopy.add(view);
        }
        return returnCopy;
    }

    public int getTabCount()
    {
        return getTabs().size();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_BOTTOM))
        {
            int firstHeight = 0;
            if (child1.getVisibility() != View.GONE)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstHeight = child1Height;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.layout(paddingLeft, paddingTop + firstHeight, paddingLeft + child2.getMeasuredWidth(), paddingTop + firstHeight + child2.getMeasuredHeight());
            }
        } else if (headPosition.equals(HEAD_POSITION_LEFT) || headPosition.equals(HEAD_POSITION_RIGHT))
        {
            int firstWidth = 0;
            if (child1.getVisibility() != View.GONE)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstWidth = child1Width;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.layout(paddingLeft + firstWidth, paddingTop, paddingLeft + firstWidth + child2.getMeasuredWidth(), paddingTop + child2.getMeasuredHeight());
            }
        }

        final int tabSize = tabs.size();
        if (mIsLayout)
        {
            if (selectedTabIndex == -1 && tabSize > 0)
            {
                // setSelectedTab��������������¼����ڷǵ�һ�β��ֵ�����¿��ܻ�Ӱ�쵱ǰ�ĵݹ鲼�֣�ʹ״̬��һ�£���POST����
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        setSelectedTab(0);
                    }
                });
            } else if (selectedTabIndex >= tabSize)
            {
                // setSelectedTab��������������¼����ڷǵ�һ�β��ֵ�����¿��ܻ�Ӱ�쵱ǰ�ĵݹ鲼�֣�ʹ״̬��һ�£���POST����
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        if (tabSize == 0)
                        {
                            selectedTabIndex = -1;
                            if (mOnTabChangedListener != null)
                                mOnTabChangedListener.onTabChanged(null, null, -1);
                        } else
                        {
                            setSelectedTab(0);
                        }
                    }
                });
            }
        } else
        {
            mIsLayout = true;
            if (selectedTabIndex == -1 && tabSize > 0)
            {
                setSelectedTab(0);
            } else if (selectedTabIndex >= tabSize)
            {
                throw new IllegalStateException("selectedTabIndex is out of range:" + selectedTabIndex + "!");
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // ��EXACTLYģʽ��Ҫ���������������㸸�����Ĵ�С����TabLayout���ԱȽϸ�����ʵ��Ӧ������ȫ���Ա��⣬���ݲ�֧��
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("TabLayout only can run at EXACTLY mode!");
        int wrapWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int wrapHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = wrapWidthSize - getPaddingLeft() - getPaddingRight();
        int heightSize = wrapHeightSize - getPaddingTop() - getPaddingBottom();

        // ˢ�²���
        refreshLayout();
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);

        if (headPosition.equals(HEAD_POSITION_TOP))
        {
            int remainHeight = heightSize;
            if (child1.getVisibility() != View.GONE)
            {
                LayoutParams lp = child1.getLayoutParams();
                if (lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT)
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                } else
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
                }
                remainHeight = heightSize - child1.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_BOTTOM))
        {
            int remainHeight = heightSize;
            if (child2.getVisibility() != View.GONE)
            {
                LayoutParams lp = child2.getLayoutParams();
                if (lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT)
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                } else
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
                }
                remainHeight = heightSize - child2.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child1.getVisibility() != View.GONE)
            {
                child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_LEFT))
        {
            int remainWidth = widthSize;
            if (child1.getVisibility() != View.GONE)
            {
                LayoutParams lp = child1.getLayoutParams();
                if (lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT)
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                } else
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                }
                remainWidth = widthSize - child1.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_RIGHT))
        {
            int remainWidth = widthSize;
            if (child2.getVisibility() != View.GONE)
            {
                LayoutParams lp = child2.getLayoutParams();
                if (lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT)
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                } else
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                }
                remainWidth = widthSize - child2.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child1.getVisibility() != View.GONE)
            {
                child1.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        }
        setMeasuredDimension(wrapWidthSize, wrapHeightSize);
    }

    public void setOnTabChangedListener(OnTabChangedListener mOnTabChangedListener)
    {
        this.mOnTabChangedListener = mOnTabChangedListener;
    }

    public interface OnTabChangedListener
    {
        public void onTabChanged(View tab, View contentItem, int index);
    }

}