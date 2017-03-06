package com.arialyy.simple.upload;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.frame.util.FileUtil;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityUploadMeanBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.lang.ref.WeakReference;

/**
 * Created by Aria.Lao on 2017/2/9.
 */
public class UploadActivity extends BaseActivity<ActivityUploadMeanBinding> {
  @Bind(R.id.pb) HorizontalProgressBarWithNumber mPb;
  private static final int START = 0;
  private static final int STOP = 1;
  private static final int CANCEL = 2;
  private static final int RUNNING = 3;
  private static final int COMPLETE = 4;

  private static final String FILE_PATH = "/sdcard/Download/test.zip";

  private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      UploadTask task = (UploadTask) msg.obj;
      switch (msg.what) {
        case START:
          getBinding().setFileSize(FileUtil.formatFileSize(task.getFileSize()));
          break;
        case STOP:
          mPb.setProgress(0);
          break;
        case CANCEL:
          mPb.setProgress(0);
          break;
        case RUNNING:
          int p = (int) (task.getCurrentProgress() * 100 / task.getFileSize());
          mPb.setProgress(p);
          break;
        case COMPLETE:
          T.showShort(UploadActivity.this, "上传完成");
          mPb.setProgress(100);
          break;
      }
    }
  };

  @Override protected int setLayoutId() {
    return R.layout.activity_upload_mean;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
  }

  @OnClick(R.id.upload) void upload() {
    Aria.upload(this)
        .load(FILE_PATH)
        .setUploadUrl("http://172.18.104.50:8080/upload/sign_file")
        .setAttachment("file")
        .start();
  }

  @OnClick(R.id.stop) void stop() {
    Aria.upload(this).load(FILE_PATH).stop();
  }

  @OnClick(R.id.remove) void remove() {
    Aria.upload(this).load(FILE_PATH).cancel();
  }

  @Override protected void onResume() {
    super.onResume();
    Aria.upload(this).addSchedulerListener(new UploadListener(mHandler));
  }

  static class UploadListener extends Aria.UploadSchedulerListener {
    WeakReference<Handler> handler;

    UploadListener(Handler handler) {
      this.handler = new WeakReference<>(handler);
    }

    @Override public void onTaskStart(UploadTask task) {
      super.onTaskStart(task);
      handler.get().obtainMessage(START, task).sendToTarget();
    }

    @Override public void onTaskStop(UploadTask task) {
      super.onTaskStop(task);
      handler.get().obtainMessage(STOP, task).sendToTarget();
    }

    @Override public void onTaskCancel(UploadTask task) {
      super.onTaskCancel(task);
      handler.get().obtainMessage(CANCEL, task).sendToTarget();
    }

    @Override public void onTaskRunning(UploadTask task) {
      super.onTaskRunning(task);
      handler.get().obtainMessage(RUNNING, task).sendToTarget();
    }

    @Override public void onTaskComplete(UploadTask task) {
      super.onTaskComplete(task);
      handler.get().obtainMessage(COMPLETE, task).sendToTarget();
    }
  }
}
