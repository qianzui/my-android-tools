package cn.emagsoftware.telephony.receiver;

import java.util.Arrays;

import cn.emagsoftware.telephony.SmsUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public abstract class SmsSendCallback extends BroadcastReceiver {
	
	public static final int ACTION_SENT = 0;
	public static final int ACTION_DELIVERED = 1;
	
	protected Context context = null;
	protected int token = -1;
	protected int[] autoUnregisterActions = new int[]{};
	protected boolean isUnregistered = true;
	
	public SmsSendCallback(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		Arrays.sort(autoUnregisterActions);
	}
	
	/**
	 * 若设为-1，将监听所有的短信发送
	 * @param token
	 */
	public void setToken(int token){
		this.token = token;
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if(isUnregistered) return;    //如果已经反注册，将直接返回
		String actionStr = arg1.getAction();
		int code = getResultCode();
		int srcToken = arg1.getIntExtra("SMS_TOKEN", -1);
		String to = arg1.getStringExtra("SMS_TO");
		String text = arg1.getStringExtra("SMS_TEXT");
		if(token == -1 || token == srcToken){   //验证token
			if(actionStr.equals(SmsUtils.SMS_SENT_ACTION)){
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_SENT) > -1) {
					if(!unregisterMe()) return;
				}
				if(code == Activity.RESULT_OK){
					onSendSuccess(to,text);
				}else{
					onSendFailure(to,text);
				}
			}else if(actionStr.equals(SmsUtils.SMS_DELIVERED_ACTION)){
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_DELIVERED) > -1) {
					if(!unregisterMe()) return;
				}
				if(code == Activity.RESULT_OK){
					onDeliverSuccess(to,text);
				}else{
					onDeliverFailure(to,text);
				}
			}
		}
	}
	
	public void onDeliverSuccess(String to,String text){}
	
	public void onDeliverFailure(String to,String text){}
	
	public void onSendSuccess(String to,String text){}
	
	public void onSendFailure(String to,String text){}
	
	public void registerMe(){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.addAction(SmsUtils.SMS_SENT_ACTION);
		smsIntentFilter.addAction(SmsUtils.SMS_DELIVERED_ACTION);
		isUnregistered = false;
        context.registerReceiver(this,smsIntentFilter);
	}
	
	public boolean unregisterMe(){
		isUnregistered = true;
		try{
			context.unregisterReceiver(this);
			return true;
		}catch(IllegalArgumentException e){
			//重复反注册会抛出该异常，如通过代码注册的receiver在当前activity销毁时会自动反注册，若再反注册，即会抛出该异常
			Log.e("SmsSendCallback", "unregister receiver failed.", e);
			return false;
		}
	}
	
	public void setAutoUnregisterActions(int[] actions){
		if(actions == null) throw new NullPointerException();
		Arrays.sort(actions);
		this.autoUnregisterActions = actions;
	}
	
}
