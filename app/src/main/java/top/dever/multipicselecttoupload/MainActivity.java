package top.dever.multipicselecttoupload;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.dever.multipicselecttoupload.helper.LocalImageHelper;
import top.dever.multipicselecttoupload.moudle.MultipartRequest;
import top.dever.multipicselecttoupload.widget.FlowLayout;
import top.dever.multipicselecttoupload.widget.MyImageUploadView;
import top.dever.multipicselecttoupload.widget.SelectViewPager;
import uk.co.senab.photoview.PhotoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TAKE_PICTURE = 0; //定义拍照的常量标识
    private static final int CHOOSE_PICTURE = 1;    //定义相册选择的常量表示

    private FlowLayout flowLayout;  //定义流式布局
    private ImageView imageLast;
    private ArrayList<LocalImageHelper.LocalFile> mLocalFiles = new ArrayList<>();
    private Uri mImageUri;
    private String camerPath;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(false)
            .showImageForEmptyUri(R.drawable.dangkr_no_picture_small)
            .showImageOnFail(R.drawable.dangkr_no_picture_small)
            .showImageOnLoading(R.drawable.dangkr_no_picture_small)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new SimpleBitmapDisplayer())
            .build();
    private SelectViewPager mPhotoViewPager;
    private LinearLayout mShowOriginalPic;
    private ImageView mBack;
    private Button mSubmit;
    private RequestQueue mSingleQueue;
    private TextView mShowIndex;
    //private ImageView mDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flowLayout = (FlowLayout) findViewById(R.id.flowLayout);
        imageLast = ((ImageView) findViewById(R.id.imageLast));
        mPhotoViewPager = ((SelectViewPager) findViewById(R.id.photo_viewPager));
        mShowOriginalPic = ((LinearLayout) findViewById(R.id.showOriginalPic));
        mBack = ((ImageView) findViewById(R.id.back));
        mShowIndex = ((TextView) findViewById(R.id.showIndex));
        mSubmit = ((Button) findViewById(R.id.submit));

        mSubmit.setOnClickListener(this);
        mBack.setOnClickListener(this);

        mSingleQueue = Volley.newRequestQueue(getApplicationContext());

        registerForContextMenu(imageLast);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("选择照片来源");
        menu.add(0,0,0,"相册");
        menu.add(0,1,0,"相机");
        menu.add(0,2,0,"取消");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case 0:
                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setType("image/*");
                startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                break;
            case 1:
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String fileStr = getCachePath()+"/pic/";
                File savedir = new File(fileStr);
                if (!savedir.exists()) {
                    savedir.mkdirs();
                }
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                // 照片命名
                String picName =  timeStamp + ".jpg";
                camerPath = savedir+picName;
                //Log.d("camerPath", "onContextItemSelected: "+camerPath);
                mImageUri = Uri.fromFile(new File(camerPath));
                //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(openCameraIntent, TAKE_PICTURE);
                break;
            case 2:

                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        mShowOriginalPic.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            Uri originalUri = null;
            switch (requestCode){
                case CHOOSE_PICTURE: //从相册获取照片
                    originalUri = data.getData();
                    break;
                case TAKE_PICTURE: //从相机拍照获取
                    originalUri = mImageUri;
                    break;
            }


            LocalImageHelper.LocalFile localFile = new LocalImageHelper.LocalFile();

            localFile.setOriginalUri(originalUri.toString());
            localFile.setThumbnailUri(originalUri.toString());
            if(requestCode==TAKE_PICTURE){
                localFile.setOrientation(getBitmapDegree(camerPath));
            }
            addMyImageView(localFile);
            mLocalFiles.add(localFile);
        }
    }

    private void addMyImageView(final LocalImageHelper.LocalFile localFile){
        final MyImageUploadView imageUploadView = new MyImageUploadView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dipToPx(this, 110), dipToPx(this, 110));
        int mar = dipToPx(this, 5);
        layoutParams.setMargins(mar, mar, mar, mar);
        final int index = flowLayout.getChildCount()-1;
        imageUploadView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        ImageLoader.getInstance().displayImage(
                localFile.getThumbnailUri(),
                new ImageViewAware(imageUploadView.getImgView()),
                options,
                null,
                null
        );
        imageUploadView.setTag(localFile);
        imageUploadView.setCancelClickListener(new MyImageUploadView.OnCancelClickListener() {

            @Override
            public void cancelClick() {
                // TODO Auto-generated method stub
                flowLayout.removeView(imageUploadView);
                mLocalFiles.remove(imageUploadView.getTag());
            }
        });
        imageUploadView.setShowClickListner(new MyImageUploadView.OnShowClickListner() {

            @Override
            public void showClick() {
                // TODO Auto-generated method stub
                mShowOriginalPic.setVisibility(View.VISIBLE);
                mPhotoViewPager.setAdapter(new PhotoViewPager(MainActivity.this,mLocalFiles));
                mPhotoViewPager.setCurrentItem(mLocalFiles.indexOf(imageUploadView.getTag()));
                mPhotoViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        mShowIndex.setText((position+1)+"/"+mLocalFiles.size());
                    }

                    @Override
                    public void onPageSelected(int position) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        });
        flowLayout.addView(imageUploadView,index,layoutParams);
    }

    public int dipToPx(Context c, float dipValue) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public String getCachePath() {
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = getExternalCacheDir();
        else
            cacheDir = getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }


    /**
     * 读取图片的旋转的角度，还是三星的问题，需要根据图片的旋转角度正确显示
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                mShowOriginalPic.setVisibility(View.GONE);
                break;
            case R.id.submit:
                Log.d("SUBMIT", "onClick: "+mLocalFiles.size());
                for(LocalImageHelper.LocalFile lf:mLocalFiles){
                    Log.d("SUBMIT", lf.getOriginalUri().substring(7));
                }
                uploadPic();
                break;
        }
    }

    private void uploadPic() {
        String url = "http://10.168.44.31/php/test1/upload.php"; //换成自己的测试url地址
        Map<String, String> params = new HashMap<>();
        params.put("id", "19");
        params.put("type", "shop");

        List<File> f = new ArrayList<>();
        File file;
        if(mLocalFiles.size()==0){
            Toast.makeText(getApplicationContext(), "请选择图片上传", Toast.LENGTH_SHORT).show();
            return;
        }
        for(LocalImageHelper.LocalFile localFile:mLocalFiles){
            String s = localFile.getOriginalUri().substring(7);
            file = new File(s);
            if(!file.exists()){
                Toast.makeText(getApplicationContext(), "图片"+s+"不存在，测试无效", Toast.LENGTH_SHORT).show();
                return;
            }
            f.add(file);
        }
        MultipartRequest request = new MultipartRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("YanZi", "success,response = " + response);
                Toast.makeText(getApplicationContext(), "uploadSuccess,response = " + response, Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "uploadError,response = " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("YanZi", "error,response = " + error.getMessage());
            }
        }, "f_file[]", f, params); //注意这个key必须是f_file[],后面的[]不能少
        mSingleQueue.add(request);
    }

    private class PhotoViewPager extends PagerAdapter {

        private Context mContext;
        private ArrayList<LocalImageHelper.LocalFile> localFiles;

        public PhotoViewPager(Context context, ArrayList<LocalImageHelper.LocalFile> localFiles) {
            this.mContext = context;
            this.localFiles = localFiles;
        }

        @Override
        public int getCount() {
            return localFiles.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView pv = new PhotoView(mContext);
            pv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            pv.setBackgroundColor(Color.parseColor("#000000"));
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ImageLoader.getInstance().displayImage(
                    localFiles.get(position).getOriginalUri(),
                    new ImageViewAware(pv),
                    options,
                    null,
                    null);
            container.addView(pv,layoutParams);
            return pv;
        }
    }
}
