package br.com.zolkin.epos.qrcodescanner.barcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import br.com.zolkin.epos.qrcodescanner.barcodescanner.camera.CameraInstance;
import br.com.zolkin.epos.qrcodescanner.barcodescanner.camera.CameraSettings;
import br.com.zolkin.epos.qrcodescanner.barcodescanner.camera.DisplayConfiguration;
import br.com.zolkin.qrcodescanner.R;

import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends ViewGroup {
    public interface StateListener {
        void previewSized();
        void previewStarted();
        void previewStopped();
        void cameraError(Exception error);
    }

    private static final String TAG = CameraPreview.class.getSimpleName();
    private CameraInstance cameraInstance;
    private WindowManager windowManager;
    private Handler stateHandler;
    private SurfaceView surfaceView;
    private boolean previewActive = false;
    private RotationListener rotationListener;
    private List<StateListener> stateListeners = new ArrayList<>();
    private DisplayConfiguration displayConfiguration;
    private CameraSettings cameraSettings = new CameraSettings();
    private Size containerSize;
    private Size previewSize;
    private Rect surfaceRect;
    private Size currentSurfaceSize;
    private Rect framingRect = null;
    private Rect previewFramingRect = null;
    private Size framingRectSize = null;

    private double marginFraction = 0.1d;

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            currentSurfaceSize = null;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder == null) {
                Log.e(TAG, "*** WARNING *** surfaceChanged() gave us a null surface!");
                return;
            }
            currentSurfaceSize = new Size(width, height);
            startPreviewIfReady();
        }
    };

    private final Handler.Callback stateCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == R.id.zolkin_prewiew_size_ready) {
                previewSized((Size) message.obj);
                return true;
            } else if (message.what == R.id.zolkin_camera_error) {
                Exception error = (Exception) message.obj;

                if (isActive()) {
                    // This check prevents multiple errors from begin passed through.
                    pause();
                    fireState.cameraError(error);
                }
            }
            return false;
        }
    };

    private RotationCallback rotationCallback = new RotationCallback() {
        @Override
        public void onRotationChanged(int rotation) {
            // Make sure this is run on the main thread.
            stateHandler.post(new Runnable() {
                @Override
                public void run() {
                    rotationChanged();
                }
            });
        }
    };

    public CameraPreview(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }


    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (getBackground() == null) {
            // Default to SurfaceView colour, so that there are less changes.
            setBackgroundColor(Color.BLACK);
        }

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.zolkin_camera_preview);
        int framingRectWidth = (int) attributes.getDimension(R.styleable.zolkin_camera_preview_zolkin_framing_rect_width, -1);
        int framingRectHeight = (int) attributes.getDimension(R.styleable.zolkin_camera_preview_zolkin_framing_rect_height, -1);
        attributes.recycle();

        if (framingRectWidth > 0 && framingRectHeight > 0) {
            this.framingRectSize = new Size(framingRectWidth, framingRectHeight);
        }

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        stateHandler = new Handler(stateCallback);

        setupSurfaceView();

        rotationListener = new RotationListener();
    }

    private void rotationChanged() {
        pause();
        resume();
    }

    private void setupSurfaceView() {
        surfaceView = new SurfaceView(getContext());
        if (Build.VERSION.SDK_INT < 11) {
            surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        surfaceView.getHolder().addCallback(surfaceCallback);
        addView(surfaceView);
    }

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    private final StateListener fireState = new StateListener() {
        @Override
        public void previewSized() {
            for (StateListener listener : stateListeners) {
                listener.previewSized();
            }
        }

        @Override
        public void previewStarted() {
            for (StateListener listener : stateListeners) {
                listener.previewStarted();
            }

        }

        @Override
        public void previewStopped() {
            for (StateListener listener : stateListeners) {
                listener.previewStopped();
            }
        }

        @Override
        public void cameraError(Exception error) {
            for (StateListener listener : stateListeners) {
                listener.cameraError(error);
            }
        }
    };

    private void calculateFrames() {
        if (containerSize == null || previewSize == null || displayConfiguration == null) {
            previewFramingRect = null;
            framingRect = null;
            surfaceRect = null;
            throw new IllegalStateException("containerSize or previewSize is not set yet");
        }

        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        int width = containerSize.width;
        int height = containerSize.height;

        surfaceRect = displayConfiguration.scalePreview(previewSize);

        Rect container = new Rect(0, 0, width, height);
        framingRect = calculateFramingRect(container, surfaceRect);
        Rect frameInPreview = new Rect(framingRect);
        frameInPreview.offset(-surfaceRect.left, -surfaceRect.top);

        previewFramingRect = new Rect(frameInPreview.left * previewWidth / surfaceRect.width(),
                frameInPreview.top * previewHeight / surfaceRect.height(),
                frameInPreview.right * previewWidth / surfaceRect.width(),
                frameInPreview.bottom * previewHeight / surfaceRect.height());

        if (previewFramingRect.width() <= 0 || previewFramingRect.height() <= 0) {
            previewFramingRect = null;
            framingRect = null;
            Log.w(TAG, "Preview frame is too small");
        } else {
            fireState.previewSized();
        }
    }

    public void setTorch(boolean on) {
        if (cameraInstance != null) {
            cameraInstance.setTorch(on);
        }
    }

    private void containerSized(Size containerSize) {
        this.containerSize = containerSize;
        if (cameraInstance != null) {
            if (cameraInstance.getDisplayConfiguration() == null) {
                displayConfiguration = new DisplayConfiguration(getDisplayRotation(), containerSize);
                cameraInstance.setDisplayConfiguration(displayConfiguration);
                cameraInstance.configureCamera();
            }
        }
    }

    private void previewSized(Size size) {
        this.previewSize = size;
        if (containerSize != null) {
            calculateFrames();
            requestLayout();
            startPreviewIfReady();
        }
    }

    private void startPreviewIfReady() {
        if (currentSurfaceSize != null && previewSize != null && surfaceRect != null) {
            if (currentSurfaceSize.equals(new Size(surfaceRect.width(), surfaceRect.height()))) {
                startCameraPreview(surfaceView.getHolder());
            } else {
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        containerSized(new Size(r - l, b - t));

        if (surfaceRect == null) {
            surfaceView.layout(0, 0, getWidth(), getHeight());
        } else {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
        }
    }

    public Rect getFramingRect() {
        return framingRect;
    }

    public Rect getPreviewFramingRect() {
        return previewFramingRect;
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(CameraSettings cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public void resume() {
        Util.validateMainThread();
        Log.d(TAG, "resume()");

        initCamera();

        if (currentSurfaceSize != null) {
            startPreviewIfReady();
        } else {
            surfaceView.getHolder().addCallback(surfaceCallback);
        }

        requestLayout();
        rotationListener.listen(getContext(), rotationCallback);
    }


    public void pause() {
        Util.validateMainThread();
        Log.d(TAG, "pause()");

        if (cameraInstance != null) {
            cameraInstance.close();
            cameraInstance = null;
            previewActive = false;
        }
        if (currentSurfaceSize == null) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(surfaceCallback);
        }

        this.containerSize = null;
        this.previewSize = null;
        this.previewFramingRect = null;
        rotationListener.stop();

        fireState.previewStopped();
    }

    public Size getFramingRectSize() {
        return framingRectSize;
    }

    public void setFramingRectSize(Size framingRectSize) {
        this.framingRectSize = framingRectSize;
    }

    public double getMarginFraction() {
        return marginFraction;
    }

    public void setMarginFraction(double marginFraction) {
        if(marginFraction >= 0.5d) {
            throw new IllegalArgumentException("The margin fraction must be less than 0.5");
        }
        this.marginFraction = marginFraction;
    }

    protected boolean isActive() {
        return cameraInstance != null;
    }

    private int getDisplayRotation() {
        return windowManager.getDefaultDisplay().getRotation();
    }

    private void initCamera() {
        if (cameraInstance != null) {
            Log.w(TAG, "initCamera called twice");
            return;
        }

        cameraInstance = new CameraInstance(getContext());
        cameraInstance.setCameraSettings(cameraSettings);

        cameraInstance.setReadyHandler(stateHandler);
        cameraInstance.open();
    }


    private void startCameraPreview(SurfaceHolder holder) {
        if (!previewActive) {
            Log.i(TAG, "Starting preview");
            cameraInstance.setSurfaceHolder(holder);
            cameraInstance.startPreview();
            previewActive = true;

            previewStarted();
            fireState.previewStarted();
        }
    }

    protected void previewStarted() {

    }

    public CameraInstance getCameraInstance() {
        return cameraInstance;
    }

    public boolean isPreviewActive() {
        return previewActive;
    }

    protected Rect calculateFramingRect(Rect container, Rect surface) {
        Rect intersection = new Rect(container);
        boolean intersects = intersection.intersect(surface);

        if(framingRectSize != null) {
            int horizontalMargin = Math.max(0, (intersection.width() - framingRectSize.width) / 2);
            int verticalMargin = Math.max(0, (intersection.height() - framingRectSize.height) / 2);
            intersection.inset(horizontalMargin, verticalMargin);
            return intersection;
        }
        int margin = (int)Math.min(intersection.width() * marginFraction, intersection.height() * marginFraction);
        intersection.inset(margin, margin);
        if (intersection.height() > intersection.width()) {
            intersection.inset(0, (intersection.height() - intersection.width()) / 2);
        }
        return intersection;
    }
}
