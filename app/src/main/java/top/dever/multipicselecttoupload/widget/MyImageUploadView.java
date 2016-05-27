package top.dever.multipicselecttoupload.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import top.dever.multipicselecttoupload.R;

public class MyImageUploadView extends RelativeLayout {
	private ImageView main_img,cancel_img;
	private Context context;
	
	private OnCancelClickListener cancelClickListener;
	private OnShowClickListner showClickListner;

	public void setCancelClickListener(OnCancelClickListener cancelClickListener) {
		this.cancelClickListener = cancelClickListener;
	}

	public void setShowClickListner(OnShowClickListner showClickListner) {
		this.showClickListner = showClickListner;
	}

	public MyImageUploadView(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	public MyImageUploadView(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.context = context;
		init();
	}

	public ImageView getImgView(){
		return main_img;
	}

	private void init() {
		// TODO Auto-generated method stub
		View v = LayoutInflater.from(context).inflate(R.layout.view_image_show_cancel,this);
		main_img = (ImageView)v.findViewById(R.id.main_img);
		cancel_img = (ImageView)v.findViewById(R.id.cancel_img);
		cancel_img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancelClickListener.cancelClick();
			}
		});
		main_img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showClickListner.showClick();
			}
		});
	}

	public void setScaleType(ImageView.ScaleType scaleType){
		main_img.setScaleType(scaleType);
	}

	public void setImageDrawable(Drawable drawable){
		main_img.setImageDrawable(drawable);
	}
	
	public void setImageBitmap(Bitmap bitmap){
		main_img.setImageBitmap(bitmap);
	}
	
	public boolean isGone(){
		if(MyImageUploadView.this.getVisibility() == View.GONE){
			return true;
		};
		return false;
	}
	
	
	public interface OnCancelClickListener{
		void cancelClick();
	} 
	public interface OnShowClickListner{
		void showClick();
	}
}
