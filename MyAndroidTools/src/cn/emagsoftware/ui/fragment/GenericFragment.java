package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GenericFragment extends Fragment
{

    Bundle                       mSavedInstanceState = null;
    OnCreateViewCallbackListener mListener           = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        mSavedInstanceState = savedInstanceState;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        if (mListener != null)
            mListener.onCreateViewCallback(getActivity(), view, savedInstanceState);
    }

    void execSuperOnViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * <p>��Ҫע����ǣ����View�Ѿ����ڣ���ô�ڵ��ø÷���ʱonCreateViewCallback�����ͻ��������ص�
     * 
     * @param listener
     */
    public void setOnCreateViewCallbackListener(OnCreateViewCallbackListener listener)
    {
        mListener = listener;
        if (listener != null)
        {
            View view = getView();
            if (view != null)
                listener.onCreateViewCallback(getActivity(), view, mSavedInstanceState);
        }
    }

}