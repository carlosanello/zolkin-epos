/*
 * Copyright (C) 2008 ZXing authors
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

package br.com.zolkin.epos.qrcodescanner.client;

public final class Intents {
    private Intents() {
    }

    public static final class Scan {
        public static final String ACTION = "br.com.zolkin.qrcodescanner.client.SCAN";
        public static final String MODE = "SCAN_MODE";
        public static final String PRODUCT_MODE = "PRODUCT_MODE";
        public static final String ONE_D_MODE = "ONE_D_MODE";
        public static final String QR_CODE_MODE = "QR_CODE_MODE";
        public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";
        public static final String AZTEC_MODE = "AZTEC_MODE";
        public static final String PDF417_MODE = "PDF417_MODE";
        public static final String FORMATS = "SCAN_FORMATS";
        public static final String CAMERA_ID = "SCAN_CAMERA_ID";
        public static final String CHARACTER_SET = "CHARACTER_SET";
        public static final String BEEP_ENABLED = "BEEP_ENABLED";
        public static final String BARCODE_IMAGE_ENABLED = "BARCODE_IMAGE_ENABLED";
        public static final String ORIENTATION_LOCKED = "SCAN_ORIENTATION_LOCKED";
        public static final String PROMPT_MESSAGE = "PROMPT_MESSAGE";
        public static final String RESULT = "SCAN_RESULT";
        public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";
        public static final String RESULT_UPC_EAN_EXTENSION = "SCAN_RESULT_UPC_EAN_EXTENSION";
        public static final String RESULT_BYTES = "SCAN_RESULT_BYTES";
        public static final String RESULT_ORIENTATION = "SCAN_RESULT_ORIENTATION";
        public static final String RESULT_ERROR_CORRECTION_LEVEL = "SCAN_RESULT_ERROR_CORRECTION_LEVEL";
        public static final String RESULT_BYTE_SEGMENTS_PREFIX = "SCAN_RESULT_BYTE_SEGMENTS_";
        public static final String RESULT_BARCODE_IMAGE_PATH = "SCAN_RESULT_IMAGE_PATH";

        private Scan() {
        }
    }
}
