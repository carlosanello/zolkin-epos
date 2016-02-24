package br.com.zolkin.epos.qrcodescanner.barcodescanner.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.zolkin.epos.qrcodescanner.barcodescanner.Size;
import br.com.zolkin.epos.qrcodescanner.barcodescanner.SourceData;
import br.com.zolkin.epos.qrcodescanner.client.AmbientLightManager;
import br.com.zolkin.epos.qrcodescanner.client.camera.CameraConfigurationUtils;
import br.com.zolkin.epos.qrcodescanner.client.camera.open.OpenCameraInterface;

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private Camera camera;
    private Camera.CameraInfo cameraInfo;

    private AutoFocusManager autoFocusManager;
    private AmbientLightManager ambientLightManager;

    private boolean previewing;
    private String defaultParameters;

    private CameraSettings settings = new CameraSettings();

    private DisplayConfiguration displayConfiguration;

    private Size requestedPreviewSize;
    private Size previewSize;

    private int rotationDegrees = -1;

    private Context context;

    private final class CameraPreviewCallback implements Camera.PreviewCallback {
        private PreviewCallback callback;

        private Size resolution;

        public CameraPreviewCallback() {
        }

        public void setResolution(Size resolution) {
            this.resolution = resolution;
        }

        public void setCallback(PreviewCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Size cameraResolution = resolution;
            PreviewCallback callback = this.callback;
            if (cameraResolution != null && callback != null) {
                int format = camera.getParameters().getPreviewFormat();
                SourceData source = new SourceData(data, cameraResolution.width, cameraResolution.height, format, getCameraRotation());
                callback.onPreview(source);
            } else {
                Log.d(TAG, "Got preview callback, but no handler or resolution available");
            }
        }
    }

    private final CameraPreviewCallback cameraPreviewCallback;

    public CameraManager(Context context) {
        this.context = context;
        cameraPreviewCallback = new CameraPreviewCallback();
    }

    public void open() {
        camera = OpenCameraInterface.open(settings.getRequestedCameraId());
        if (camera == null) {
            throw new RuntimeException("Failed to open camera");
        }

        int cameraId = OpenCameraInterface.getCameraId(settings.getRequestedCameraId());
        cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
    }

    public void configure() {
        setParameters();
    }

    public void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        camera.setPreviewDisplay(holder);
    }

    public void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager = new AutoFocusManager(camera, settings);
            ambientLightManager = new AmbientLightManager(context, this, settings);
            ambientLightManager.start();
        }
    }

    public void stopPreview() {
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (ambientLightManager != null) {
            ambientLightManager.stop();
            ambientLightManager = null;
        }
        if (camera != null && previewing) {
            camera.stopPreview();
            cameraPreviewCallback.setCallback(null);
            previewing = false;
        }
    }


    public void close() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public boolean isCameraRotated() {
        if(rotationDegrees == -1) {
            throw new IllegalStateException("Rotation not calculated yet. Call configure() first.");
        }
        return rotationDegrees % 180 != 0;
    }

    public int getCameraRotation() {
        return rotationDegrees;
    }

    private Camera.Parameters getDefaultCameraParameters() {
        Camera.Parameters parameters = camera.getParameters();
        if (defaultParameters == null) {
            defaultParameters = parameters.flatten();
        } else {
            parameters.unflatten(defaultParameters);
        }
        return parameters;
    }

    private void setDesiredParameters(boolean safeMode) {
        Camera.Parameters parameters = getDefaultCameraParameters();

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }


        CameraConfigurationUtils.setFocus(parameters, settings.isAutoFocusEnabled(), !settings.isContinuousFocusEnabled(), safeMode);

        if (!safeMode) {
            CameraConfigurationUtils.setTorch(parameters, false);

            if (settings.isScanInverted()) {
                CameraConfigurationUtils.setInvertColor(parameters);
            }

            if (settings.isBarcodeSceneModeEnabled()) {
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }

            if (settings.isMeteringEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    CameraConfigurationUtils.setVideoStabilization(parameters);
                    CameraConfigurationUtils.setFocusArea(parameters);
                    CameraConfigurationUtils.setMetering(parameters);
                }
            }

        }

        List<Size> previewSizes = getPreviewSizes(parameters);
        if (previewSizes.size() == 0) {
            requestedPreviewSize = null;
        } else {
            requestedPreviewSize = displayConfiguration.getBestPreviewSize(previewSizes, isCameraRotated());

            parameters.setPreviewSize(requestedPreviewSize.width, requestedPreviewSize.height);
        }

        if (Build.DEVICE.equals("glass-1")) {
            CameraConfigurationUtils.setBestPreviewFPS(parameters);
        }

        Log.i(TAG, "Final camera parameters: " + parameters.flatten());

        camera.setParameters(parameters);
    }

    private static List<Size> getPreviewSizes(Camera.Parameters parameters) {
        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        List<Size> previewSizes = new ArrayList<>();
        if (rawSupportedSizes == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            if (defaultSize != null) {
                previewSizes.add(new Size(defaultSize.width, defaultSize.height));
            }
            return previewSizes;
        }
        for (Camera.Size size : rawSupportedSizes) {
            previewSizes.add(new Size(size.width, size.height));
        }
        return previewSizes;
    }

    private int calculateDisplayRotation() {
        int rotation = displayConfiguration.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        Log.i(TAG, "Camera Display Orientation: " + result);
        return result;
    }

    private void setCameraDisplayOrientation(int rotation) {
        camera.setDisplayOrientation(rotation);
    }


    private void setParameters() {
        try {
            this.rotationDegrees = calculateDisplayRotation();
            setCameraDisplayOrientation(rotationDegrees);
        } catch (Exception e) {
            Log.w(TAG, "Failed to set rotation.");
        }
        try {
            setDesiredParameters(false);
        } catch (Exception e) {
            // Failed, use safe mode
            try {
                setDesiredParameters(false);
            } catch (Exception e2) {
                // Well, darn. Give up
                Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
            }
        }

        Camera.Size realPreviewSize = camera.getParameters().getPreviewSize();
        if (realPreviewSize == null) {
            previewSize = requestedPreviewSize;
        } else {
            previewSize = new Size(realPreviewSize.width, realPreviewSize.height);
        }
        cameraPreviewCallback.setResolution(previewSize);
    }


    public boolean isOpen() {
        return camera != null;
    }

    public Size getNaturalPreviewSize() {
        return previewSize;
    }

    public Size getPreviewSize() {
        if (previewSize == null) {
            return null;
        } else if (this.isCameraRotated()) {
            return previewSize.rotate();
        } else {
            return previewSize;
        }
    }

    public void requestPreviewFrame(PreviewCallback callback) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            cameraPreviewCallback.setCallback(callback);
            theCamera.setOneShotPreviewCallback(cameraPreviewCallback);
        }
    }

    public CameraSettings getCameraSettings() {
        return settings;
    }

    public void setCameraSettings(CameraSettings settings) {
        this.settings = settings;
    }

    public DisplayConfiguration getDisplayConfiguration() {
        return displayConfiguration;
    }

    public void setDisplayConfiguration(DisplayConfiguration displayConfiguration) {
        this.displayConfiguration = displayConfiguration;
    }

    public void setTorch(boolean on) {
        if (camera != null) {
            boolean isOn = isTorchOn();
            if (on != isOn) {
                if (autoFocusManager != null) {
                    autoFocusManager.stop();
                }

                Camera.Parameters parameters = camera.getParameters();
                CameraConfigurationUtils.setTorch(parameters, on);
                if (settings.isExposureEnabled()) {
                    CameraConfigurationUtils.setBestExposure(parameters, on);
                }
                camera.setParameters(parameters);

                if (autoFocusManager != null) {
                    autoFocusManager.start();
                }
            }
        }
    }

    public boolean isTorchOn() {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters != null) {
            String flashMode = parameters.getFlashMode();
            return flashMode != null &&
                    (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
                            Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
        } else {
            return false;
        }
    }
}
