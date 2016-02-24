package br.com.zolkin.epos.qrcodescanner.barcodescanner;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import br.com.zolkin.epos.qrcodescanner.barcodescanner.camera.CameraInstance;
import br.com.zolkin.epos.qrcodescanner.barcodescanner.camera.PreviewCallback;
import br.com.zolkin.qrcodescanner.R;

import java.util.List;

public class DecoderThread {
    private static final String TAG = DecoderThread.class.getSimpleName();

    private CameraInstance cameraInstance;
    private HandlerThread thread;
    private Handler handler;
    private Decoder decoder;
    private Handler resultHandler;
    private Rect cropRect;
    private boolean running = false;
    private final Object LOCK = new Object();

    private final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == R.id.zolkin_decode) {
                decode((SourceData) message.obj);
            }
            return true;
        }
    };

    public DecoderThread(CameraInstance cameraInstance, Decoder decoder, Handler resultHandler) {
        Util.validateMainThread();

        this.cameraInstance = cameraInstance;
        this.decoder = decoder;
        this.resultHandler = resultHandler;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public Rect getCropRect() {
        return cropRect;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }

    public void start() {
        Util.validateMainThread();

        thread = new HandlerThread(TAG);
        thread.start();
        handler = new Handler(thread.getLooper(), callback);
        running = true;
        requestNextPreview();
    }


    public void stop() {
        Util.validateMainThread();

        synchronized (LOCK) {
            running = false;
            handler.removeCallbacksAndMessages(null);
            thread.quit();
        }
    }


    private final PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreview(SourceData sourceData) {
            synchronized (LOCK) {
                if (running) {
                    handler.obtainMessage(R.id.zolkin_decode, sourceData).sendToTarget();
                }
            }
        }
    };

    private void requestNextPreview() {
        if (cameraInstance.isOpen()) {
            cameraInstance.requestPreview(previewCallback);
        }
    }

    protected LuminanceSource createSource(SourceData sourceData) {
        if (this.cropRect == null) {
            return null;
        } else {
            return sourceData.createSource();
        }
    }

    private void decode(SourceData sourceData) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        sourceData.setCropRect(cropRect);
        LuminanceSource source = createSource(sourceData);

        if(source != null) {
            rawResult = decoder.decode(source);
        }

        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
            if (resultHandler != null) {
                BarcodeResult barcodeResult = new BarcodeResult(rawResult, sourceData);
                Message message = Message.obtain(resultHandler, R.id.zolkin_decode_succeeded, barcodeResult);
                Bundle bundle = new Bundle();
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (resultHandler != null) {
                Message message = Message.obtain(resultHandler, R.id.zolkin_decode_failed);
                message.sendToTarget();
            }
        }
        if (resultHandler != null) {
            List<ResultPoint> resultPoints = decoder.getPossibleResultPoints();
            Message message = Message.obtain(resultHandler, R.id.zolkin_possible_result_points, resultPoints);
            message.sendToTarget();
        }
        requestNextPreview();
    }
}
