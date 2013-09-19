package cn.emagsoftware.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Wendell on 13-8-28.
 */
public abstract class GenericFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!GenericActivity.IS_RUNNING)
        {
            onRestoreStaticState();
            GenericActivity.IS_RUNNING = true;
        }
        super.onCreate(savedInstanceState); // 可能会触发一些用户行为，故放在恢复静态状态之后
    }

    protected abstract void onRestoreStaticState();

}
