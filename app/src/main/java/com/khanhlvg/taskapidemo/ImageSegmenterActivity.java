package com.khanhlvg.taskapidemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter.ImageSegmenterOptions;
import org.tensorflow.lite.task.vision.segmenter.OutputType;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;

public class ImageSegmenterActivity extends AppCompatActivity {

    private TensorImage tensorImage;
    private ImageSegmenter imageSegmenter;
    private ImageView originalImageView, outputImageView, overlayImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_segmentor);

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.girl);
        tensorImage = TensorImage.fromBitmap(image);
        originalImageView = findViewById(R.id.original_bitmap);
        overlayImageView = findViewById(R.id.segmentation_bitmap_overlay);
        outputImageView = findViewById(R.id.segmentation_bitmap);
        originalImageView.setImageBitmap(image);

        try {
            // Initialization
            ImageSegmenterOptions options = ImageSegmenterOptions.builder().setOutputType(OutputType.CATEGORY_MASK).build();
            imageSegmenter = ImageSegmenter.createFromFileAndOptions(this, "deeplabv3.tflite", options);

            // Run inference
            List<Segmentation> results = imageSegmenter.segment(tensorImage);
            Log.i("LIST", String.valueOf(results.get(0)));
            Segmentation result = results.get(0);
            TensorImage tensorMask = result.getMasks().get(0);
            Log.i("RESULT", String.valueOf(result.getColoredLabels()));
            int[] rawMask = tensorMask.getTensorBuffer().getIntArray();
            Log.i("NUMBER", String.valueOf(rawMask.length));
            Log.i("VALUES", Arrays.toString(rawMask));

            ///////////////////
            Bitmap output = Bitmap.createBitmap(tensorMask.getWidth(), tensorMask.getHeight(), Bitmap.Config.ARGB_8888);

            for (int y = 0; y < tensorMask.getHeight(); y++) {
                for (int x = 0; x < tensorMask.getWidth(); x++) {
                    output.setPixel(x, y, rawMask[y * tensorMask.getWidth() + x] == 0 ? Color.TRANSPARENT : Color.BLACK);
                }
            }
            //mask = BitmapUtils.scaleBitmap(mask, w, h);
            Bitmap scaledMaskBitmap = Bitmap.createScaledBitmap(output, image.getWidth(), image.getHeight(), true);
            //////////////////

            // Convert from raw mask data into a Bitmap instance
            /*int[] pixelData = new int[rawMask.length * 3];
            List<ColoredLabel> coloredLabels = result.getColoredLabels();
            Log.i("COLORED", String.valueOf(coloredLabels));
            for (int i = 0; i < rawMask.length; i++) {
                int color = coloredLabels.get(rawMask[i]).getColor().toArgb();
                Log.i("COLOR_VALUE", String.valueOf(rawMask[i]));
                Log.i("COLOR_RED", String.valueOf(Color.argb(0,255,255,255)));
                Log.i("COLOR_TRANSPARENT", String.valueOf(Color.red(Color.argb(0,255,255,255))));
                if (rawMask[i] == 0) {
                    pixelData[3 * i] = getResources().getColor(android.R.color.transparent);
                    pixelData[3 * i + 1] = getResources().getColor(android.R.color.transparent);
                    pixelData[3 * i + 2] = getResources().getColor(android.R.color.transparent);
                }
            }
            int[] shape = {tensorMask.getWidth(), tensorMask.getHeight(), 3};
            Log.i("SHAPE", String.valueOf(shape.length));
            Log.i("WIDTH", String.valueOf(tensorMask.getWidth()));
            Log.i("HEIGHT", String.valueOf(tensorMask.getHeight()));
            TensorImage maskImage = new TensorImage();
            maskImage.load(pixelData, shape);*/

            // Show the segmentation visualization on screen
            //Bitmap maskBitmap = Bitmap.createScaledBitmap(maskImage.getBitmap(), image.getWidth(), image.getHeight(), true);
            outputImageView.setImageBitmap(scaledMaskBitmap);
            overlayImageView.setImageBitmap(cropBitmapWithMask(image, scaledMaskBitmap));
        } catch (IOException e) {
            Log.e("ImageSegmenter", "Error: ", e);
        }
    }

    private Bitmap cropBitmapWithMask(Bitmap original, Bitmap mask) {
        if (original == null
                || mask == null) {
            return null;
        }

        final int w = original.getWidth();
        final int h = original.getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }

        Bitmap cropped = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(cropped);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(original, 0, 0, null);
        canvas.drawBitmap(mask, 0, 0, paint);

        paint.setXfermode(null);

        return cropped;
    }
}