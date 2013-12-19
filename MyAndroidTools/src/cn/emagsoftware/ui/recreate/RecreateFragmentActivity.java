package cn.emagsoftware.ui.recreate;

import android.content.Intent;
import android.os.Bundle;

import cn.emagsoftware.ui.GenericFragmentActivity;

public abstract class RecreateFragmentActivity extends GenericFragmentActivity
{

    private static final String EXTRA_RECREATEFRAGMENTACTIVITY_OUTSTATE = "android.intent.extra.RECREATEFRAGMENTACTIVITY_OUTSTATE";

    private Bundle              outState                                = null;
    private boolean             isAtFront                               = false;
    private boolean             shouldRecreateNextTime                  = false;

    @Override
    protected final void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        RecreateManager.addRecreateActivity(this);
        Intent intent = getIntent();
        outState = intent.getBundleExtra(EXTRA_RECREATEFRAGMENTACTIVITY_OUTSTATE);
        if (outState == null)
        {
            onCreateImpl(savedInstanceState);
        } else
        {
            intent.removeExtra(EXTRA_RECREATEFRAGMENTACTIVITY_OUTSTATE);
            onCreateImpl(outState);
        }
    }

    protected void onCreateImpl(Bundle savedInstanceState)
    {
    }

    @Override
    protected final void onStart()
    {
        super.onStart();
        onStartImpl();
        if (outState != null)
        {
            onRestoreInstanceState(outState);
            outState = null;
        }
    }

    protected void onStartImpl()
    {
    }

    @Override
    protected final void onResume()
    {
        super.onResume();
        isAtFront = true;
        if (shouldRecreateNextTime)
        {
            shouldRecreateNextTime = false;
            recreateMeImmediately();
            return;
        }
        onResumeImpl();
    }

    protected void onResumeImpl()
    {
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isAtFront = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        RecreateManager.removeRecreateActivity(this);
    }

    public boolean isAtFront()
    {
        return isAtFront;
    }

    public void recreateMe()
    {
        if (isAtFront())
            recreateMeImmediately();
        else
            shouldRecreateNextTime = true;
    }

    private void recreateMeImmediately()
    {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        finish();
        overridePendingTransition(0, 0);
        intent.putExtra(EXTRA_RECREATEFRAGMENTACTIVITY_OUTSTATE, outState);
        startActivity(intent);
    }

}
