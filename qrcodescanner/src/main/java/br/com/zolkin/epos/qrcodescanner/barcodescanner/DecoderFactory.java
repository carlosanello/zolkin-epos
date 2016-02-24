package br.com.zolkin.epos.qrcodescanner.barcodescanner;

import com.google.zxing.DecodeHintType;

import java.util.Map;

public interface DecoderFactory {
    Decoder createDecoder(Map<DecodeHintType, ?> baseHints);
}
