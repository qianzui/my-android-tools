package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>该类的功能实现类似于android.os.AsyncTask类，但AsyncTask类内部采用了线程池实现，线程资源不会被立即释放。该类以快速释放的实现，提供给用户一个第二选择
 * <p>该类支持在非UI-Thread中创建并启动
 * @author Wendell
 * @version 2.5
 */
public class UIThread extends Thread {
	
	protected Context context = null;
	protected boolean isCancelled = false;
	protected Handler handler = new Handler(Looper.getMainLooper());
	
	public UIThread(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		if(isCancelled) return;
		handler.post(new Runnable() {    //在start中推送onBeginUI，而不是在当前线程真正开始后推送，可以保证UI线程的衔接一致，避免出现不同步的情况
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(isCancelled) return;
				onBeginUI(context);
				execSuperStart();    //必须在onBeginUI执行完成后再启动线程
			}
		});
	}
	
	private void execSuperStart(){
		super.start();
	}
	
	public final void run(){
		super.run();
		try{
			if(isCancelled) return;
			final Object result = onRunNoUI(context);
			handler.post(new Runnable(){
				@Override
				public void run() {
					if(isCancelled) return;
					onSuccessUI(context,result);
				}
			});
		}catch(final Exception e){
			handler.post(new Runnable(){
				@Override
				public void run() {
					if(isCancelled) return;
					onExceptionUI(context,e);
				}
			});
		}
	}
	
	public void cancel(){
		isCancelled = true;
	}
	
	public void postProgress(final Object progress){
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				onProgressUI(context,progress);
			}
		});
	}
	
	protected void onBeginUI(Context context){}
	
	protected Object onRunNoUI(Context context) throws Exception{return null;}
	
	protected void onProgressUI(Context context,Object progress){}
	
	protected void onSuccessUI(Context context,Object result){}
	
	protected void onExceptionUI(Context context,Exception e){}
	
}
