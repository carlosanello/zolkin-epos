/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zolkin.epos.qrcodescanner.integration;

public final class IntentResult {

    private final String contents;
    private final String formatName;
    private final byte[] rawBytes;
    private final Integer orientation;
    private final String errorCorrectionLevel;
    private final String barcodeImagePath;

    IntentResult() {
        this(null, null, null, null, null, null);
    }

    IntentResult(String contents,
                 String formatName,
                 byte[] rawBytes,
                 Integer orientation,
                 String errorCorrectionLevel,
                 String barcodeImagePath) {
        this.contents = contents;
        this.formatName = formatName;
        this.rawBytes = rawBytes;
        this.orientation = orientation;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.barcodeImagePath = barcodeImagePath;
    }

    public String getContents() {
        return contents;
    }
    public String getFormatName() {
        return formatName;
    }
    public byte[] getRawBytes() {
        return rawBytes;
    }
    public Integer getOrientation() {
        return orientation;
    }
    public String getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }
    public String getBarcodeImagePath() {
        return barcodeImagePath;
    }

    @Override
    public String toString() {
        StringBuilder dialogText = new StringBuilder(120);
        dialogText.append("Formato: ").append(formatName).append('\n');
        dialogText.append("Conteudo: ").append(contents).append('\n');
        int rawBytesLength = rawBytes == null ? 0 : rawBytes.length;
        dialogText.append("Raw: (").append(rawBytesLength).append(" bytes)\n");
        dialogText.append("Orientação: ").append(orientation).append('\n');
        dialogText.append("EC: ").append(errorCorrectionLevel).append('\n');
        dialogText.append("Image: ").append(barcodeImagePath).append('\n');
        return dialogText.toString();
    }
}
