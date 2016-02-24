package br.com.zolkin.epos.qrcodescanner.barcodescanner.camera;

import br.com.zolkin.epos.qrcodescanner.barcodescanner.SourceData;

public interface PreviewCallback {
    void onPreview(SourceData sourceData);
}
