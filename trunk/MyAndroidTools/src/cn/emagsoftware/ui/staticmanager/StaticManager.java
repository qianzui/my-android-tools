package cn.emagsoftware.ui.staticmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public final class StaticManager
{

    private static final String EXTRA_QUIT_SIGN      = "android.intent.extra.QUIT_SIGN";
    private static final String EXTRAVALUE_QUIT_SIGN = "EXTRAVALUE_QUIT_SIGN";

    private StaticManager()
    {
    }

    /**
     * <p>���������ϵͳ���պ���ô˷���������յ�ǰ�����Activityջ������Ӧ�� <p>���ô˷���Ҫ��֤����ǰ�᣺ 1.һ��Ҫ���ж���������ѱ����յ�����µ��ã�����ͨ��ʹ��һ����̬�������жϻ������ 2.���Activity��������ֻ��һ��ʵ��λ��Activityջ�У��ұ���λ��ջ��
     * 
     * @param context
     * @param launchActivity
     */
    public static void restartAppWhenDalvikRecycled(Context context, Class<? extends Activity> launchActivity)
    {
        Intent intent = new Intent(context, launchActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * <p>�������Activityջ�к��ж��Activity����Ҫһ�����˳�ʱ���Ե��ô˷�������м��Activity�������Activity����һ�������˳���Ϣ��Intent <p>��Activityһ����onCreate��onNewIntent��ͨ��StaticManager.isQuitIntent(intent)�ж��Ƿ�Ϊ�˳�Intent�������˳�����
     * <p>���ô˷���Ҫ��֤һ��ǰ�᣺ 1.��Activity������Activityջ���м䲿λ����������ʵ��
     * 
     * @param context
     * @param rootActivity
     */
    public static void sendQuitIntentToRootActivity(Context context, Class<? extends Activity> rootActivity)
    {
        Intent intent = new Intent(context, rootActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_QUIT_SIGN, EXTRAVALUE_QUIT_SIGN);
        context.startActivity(intent);
    }

    /**
     * <p>�ж��Ƿ�Ϊ�˳�Intent
     * 
     * @param intent
     * @return
     */
    public static boolean isQuitIntent(Intent intent)
    {
        return EXTRAVALUE_QUIT_SIGN.equals(intent.getStringExtra(EXTRA_QUIT_SIGN));
    }

}