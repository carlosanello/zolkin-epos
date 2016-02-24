package br.com.zolkin.epos.qrcodescanner.barcodescanner.camera;

import android.os.Handler;
import android.os.HandlerThread;

class CameraThread {
    private static final String TAG = CameraThread.class.getSimpleName();

    private static CameraThread instance;

    public static CameraThread getInstance() {
        if (instance == null) {
            instance = new CameraThread();
        }
        return instance;
    }


    private Handler handler;
    private HandlerThread thread;

    private int openCount = 0;

    private final Object LOCK = new Object();


    private CameraThread() {
    }

    protected void enqueue(Runnable runnable) {
        synchronized (LOCK) {
            if (this.handler == null) {
                if (openCount <= 0) {
                    throw new IllegalStateException("CameraThread is not open");
                }
                this.thread = new HandlerThread("CameraThread");
                this.thread.start();
                this.handler = new Handler(thread.getLooper());
            }
            this.handler.post(runnable);
        }
    }

    private void quit() {
        synchronized (LOCK) {
            this.thread.quit();
            this.thread = null;
            this.handler = null;
        }
    }

    protected void decrementInstances() {
        synchronized (LOCK) {
            openCount -= 1;
            if (openCount == 0) {
                quit();
            }
        }
    }

    protected void incrementAndEnqueue(Runnable runner) {
        synchronized (LOCK) {
            openCount += 1;
            enqueue(runner);
        }
    }
}
