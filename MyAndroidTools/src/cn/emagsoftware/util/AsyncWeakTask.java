package cn.emagsoftware.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * <p>����������ִ���������ض�����(����Ϊ������)���첽�������������ݱ����պ������ȡ��������������ڲ�����������ʹ���������ã��������䱻����
 * 
 * @author Wendell
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncWeakTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{

    private List<WeakReference<Object>> mObjReferences = null;
    private Handler                     mHandler       = new Handler();
    private boolean                     mIsRecycled    = false;

    public AsyncWeakTask(Object... objs)
    {
        mObjReferences = new ArrayList<WeakReference<Object>>(objs.length);
        for (Object obj : objs)
        {
            addToWeakReference(obj);
        }
    }

    public final void addToWeakReference(Object obj)
    {
        if (obj == null)
            throw new NullPointerException();
        mObjReferences.add(new WeakReference<Object>(obj));
    }

    private boolean cancelWhenRecycled(boolean mayInterruptIfRunning)
    {
        mIsRecycled = true;
        return cancel(mayInterruptIfRunning);
    }

    protected void onPreExecute(Object[] objs)
    {
    }

    protected abstract Result doInBackgroundImpl(Params... params) throws Exception;

    protected void onProgressUpdate(Object[] objs, Progress... values)
    {
    }

    protected void onCancelled(Object[] objs)
    {
    }

    protected void onPostExecute(Object[] objs, Result result)
    {
    }

    protected void onException(Object[] objs, Exception e)
    {
    }

    @Override
    protected final void onPreExecute()
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancelWhenRecycled(true);
        else
            onPreExecute(objs);
    }

    @Override
    protected final Result doInBackground(Params... params)
    {
        try
        {
            return doInBackgroundImpl(params);
        } catch (final Exception e)
        {
            if (mIsRecycled)
            { // �ڲ�cancelWhenRecycled(true)���µ��쳣���ܻص�onException���˺�������쳣���������������ѻ��գ�ͬ������ִ��onException�����������ͳһ������Ȼ�����������
                return null; // �������������ѻ��գ�onCancelled��onPostExecute�����ᱻʵ��ִ�У������������ֱ�ӷ���
            }
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Object[] objs = getObjects();
                    if (objs != null)
                        onException(objs, e);
                }
            });
            cancelWhenRecycled(false); // ���ڲ����ò���trueʱ���ӡ��InterruptedException�����ﴫfalse����֮�����ܸ��쳣��Ӱ����
            return null;
        }
    }

    @Override
    protected final void onProgressUpdate(Progress... values)
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancelWhenRecycled(true);
        else
            onProgressUpdate(objs, values);
    }

    @Override
    protected final void onCancelled()
    {
        if (mIsRecycled)
            return;
        Object[] objs = getObjects();
        if (objs != null)
            onCancelled(objs);
    }

    @Override
    protected final void onPostExecute(Result result)
    {
        Object[] objs = getObjects();
        if (objs != null)
            onPostExecute(objs, result);
    }

    private Object[] getObjects()
    {
        Object[] objs = new Object[mObjReferences.size()];
        Iterator<WeakReference<Object>> objIterator = mObjReferences.iterator();
        for (int i = 0; i < objs.length; i++)
        {
            objs[i] = objIterator.next().get();
            if (objs[i] == null)
                return null; // ֻҪ��һ��Object�����գ��ͷ���null����Ϊ������Object�������ʹ���Ĳ�һ�£����ܴ���һϵ������
        }
        return objs;
    }

}