package com.sun.hotfixdemo;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sun on 2019/04/01.
 */
public class MainActivity extends AppCompatActivity {
    private DownloadManager downloadManager;
    private long mTaskId;
    @BindView(R.id.tv_download)
    TextView mTextView;
    @BindView(R.id.tv_input)
    EditText mInputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv_push)
    public void push() {
        try {
            new HotFixUtil().startFix();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void test() {
        throw new RuntimeException();
//        Toast.makeText(this, "bug修复成功", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.tv_click_me)
    public void clickMe() {
        test();
    }

    @OnClick(R.id.tv_download)
    public void download() {
        if (TextUtils.isEmpty(mInputUrl.getText().toString().trim())) {
            Toast.makeText(this, "请输入补丁文件的下载路径", Toast.LENGTH_LONG).show();
        } else {
            downloadFixBugFile(mInputUrl.getText().toString().trim(), "patch.dex");
        }
    }

    private void downloadFixBugFile(String fileUrl, String fileName) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

//        //在通知栏中显示，默认就是显示的
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        request.setVisibleInDownloadsUi(true);

        //sdcard的目录下的download文件夹，必须设置
//        "/storage/emulated/0/Android/data/com.sun.hotfixdemo/files/patch"
        request.setDestinationInExternalPublicDir("/storage/emulated/0/Android/data/com.sun.hotfixdemo/files/patch", fileName);
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

        //将下载请求加入下载队列
        downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);
        //注册广播接收者，监听下载状态
        this.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };


    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    mTextView.setText("下载暂停");
                    break;
                case DownloadManager.STATUS_PENDING:
                    mTextView.setText("下载延迟");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    mTextView.setText("正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    mTextView.setText("下载完成");
                    //下载完成，进行bug修复
                    try {
                        new HotFixUtil().startFix();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    mTextView.setText("下载失败");
                    break;
            }
        }
    }
}
