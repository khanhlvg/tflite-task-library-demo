package com.khanhlvg.taskapidemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter.ImageSegmenterOptions;
import org.tensorflow.lite.task.vision.segmenter.OutputType;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;

public class ImageSegmenterActivity extends AppCompatActivity {

  private TensorImage tensorImage;
  private ImageSegmenter imageSegmenter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_segmentor);

    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.girl);
    tensorImage = TensorImage.fromBitmap(image);
    ImageView originalImageView = findViewById(R.id.original_bitmap);
    originalImageView.setImageBitmap(image);

    // Initialization
    ImageSegmenterOptions options = ImageSegmenterOptions.builder().setOutputType(OutputType.CATEGORY_MASK).build();
    try {
      imageSegmenter = ImageSegmenter.createFromFileAndOptions(this, "deeplabv3.tflite", options);

      // Run inference
      List<Segmentation> results = imageSegmenter.segment(tensorImage);
      Segmentation result = results.get(0);
      List<ColoredLabel> coloredLabels = result.getColoredLabels();
      TensorImage tensorMask = result.getMasks().get(0);

      int[] rawMask = tensorMask.getTensorBuffer().getIntArray();
      int[] pixelData = new int[rawMask.length * 3];
      for (int i=0;i<rawMask.length; i++) {
        int color = coloredLabels.get(rawMask[i]).getColor().toArgb();
        pixelData[3 * i] = Color.red(color);
        pixelData[3 * i + 1] = Color.green(color);
        pixelData[3 * i + 2] = Color.blue(color);
      }
      int[] shape = {tensorMask.getWidth(), tensorMask.getHeight(), 3};
      TensorImage maskImage = new TensorImage();
      maskImage.load(pixelData, shape);
      ImageView imageView = findViewById(R.id.segmentation_bitmap);
      imageView.setImageBitmap(maskImage.getBitmap());
    } catch (IOException e) {
      Log.e("ImageSegmenter", "Error:", e);
    }
  }
}