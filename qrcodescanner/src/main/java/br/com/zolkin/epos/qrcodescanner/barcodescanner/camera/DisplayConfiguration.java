package br.com.zolkin.epos.qrcodescanner.barcodescanner.camera;

import android.graphics.Rect;
import android.util.Log;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.zolkin.epos.qrcodescanner.barcodescanner.Size;


public class DisplayConfiguration {
    private static final String TAG = DisplayConfiguration.class.getSimpleName();

    private Size viewfinderSize;
    private int rotation;
    private boolean center = false;

    public DisplayConfiguration(int rotation) {
        this.rotation = rotation;
    }

    public DisplayConfiguration(int rotation, Size viewfinderSize) {
        this.rotation = rotation;
        this.viewfinderSize = viewfinderSize;
    }

    public int getRotation() {
        return rotation;
    }

    public Size getViewfinderSize() {
        return viewfinderSize;
    }

    public Size getDesiredPreviewSize(boolean rotate) {
        if (viewfinderSize == null) {
            return null;
        } else if (rotate) {
            return viewfinderSize.rotate();
        } else {
            return viewfinderSize;
        }
    }

    public Size getBestPreviewSize(List<Size> sizes, boolean isRotated) {
        final Size desired = getDesiredPreviewSize(isRotated);

        if (desired == null) {
            return sizes.get(0);
        }

        Collections.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(Size a, Size b) {
                Size ascaled = scale(a, desired);
                int aScale = ascaled.width - a.width;
                Size bscaled = scale(b, desired);
                int bScale = bscaled.width - b.width;

                if (aScale == 0 && bScale == 0) {
                    return a.compareTo(b);
                } else if (aScale == 0) {
                    return -1;
                } else if (bScale == 0) {
                    return 1;
                } else if (aScale < 0 && bScale < 0) {
                    return a.compareTo(b);
                } else if (aScale > 0 && bScale > 0) {
                    return -a.compareTo(b);
                } else if (aScale < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        Log.i(TAG, "Viewfinder size: " + desired);
        Log.i(TAG, "Preview in order of preference: " + sizes);

        return sizes.get(0);
    }

    public static Size scale(Size from, Size to) {
        Size current = from;

        if (!to.fitsIn(current)) {
            while (true) {
                Size scaled150 = current.scale(3, 2);
                Size scaled200 = current.scale(2, 1);
                if (to.fitsIn(scaled150)) {
                    return scaled150;
                } else if (to.fitsIn(scaled200)) {
                    return scaled200;
                } else {
                    current = scaled200;
                }
            }
        } else {
            while (true) {
                Size scaled66 = current.scale(2, 3);
                Size scaled50 = current.scale(1, 2);

                if (!to.fitsIn(scaled50)) {
                    if (to.fitsIn(scaled66)) {
                        return scaled66;
                    } else {
                        return current;
                    }
                } else {
                    current = scaled50;
                }
            }
        }
    }

    public Rect scalePreview(Size previewSize) {
        Size scaledPreview = scale(previewSize, viewfinderSize);
        Log.i(TAG, "Preview: " + previewSize + "; Scaled: " + scaledPreview + "; Want: " + viewfinderSize);

        int dx = (scaledPreview.width - viewfinderSize.width) / 2;
        int dy = (scaledPreview.height - viewfinderSize.height) / 2;

        return new Rect(-dx, -dy, scaledPreview.width - dx, scaledPreview.height - dy);
    }
}
