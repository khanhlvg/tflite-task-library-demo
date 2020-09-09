package com.khanhlvg.taskapidemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.util.List;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;

public class ObjectDetectorActivity extends AppCompatActivity {

  private final static String TAG = "ObjectDetector";
  private final static String MODEL_NAME = "ssd_mobilenet_v1.tflite";
  // private final static String MODEL_NAME = "object_localizer.tflite";
  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_object_detector);

    imageView = findViewById(R.id.image_view);
    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

    try {
      runObjectDetector(image);
    } catch (IOException e) {
      Log.e(TAG, "Error initializing model", e);
    }
  }

  private void runObjectDetector(Bitmap image) throws IOException {
    TensorImage tensorImage = TensorImage.fromBitmap(image);
    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

    // Create canvas to draw detection result
    Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(image, 0,0,null);
    imageView.setImageBitmap(bitmap);

    // Initialization
    ObjectDetectorOptions options = ObjectDetectorOptions.builder().setMaxResults(3).build();
    ObjectDetector objectDetector = ObjectDetector
          .createFromFileAndOptions(this, MODEL_NAME, options);

    // Run inference
    List<Detection> results = objectDetector.detect(tensorImage);
    for (Detection detection : results) {
      Log.i(TAG, detection.getBoundingBox() + "," + detection.getCategories());
      drawRectangle(canvas, detection);
    }
  }

  private void drawRectangle(Canvas canvas, Detection detection) {
    RectF bBox = detection.getBoundingBox();

    Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(10);
    canvas.drawRect(bBox.left, bBox.top, bBox.right, bBox.bottom, paint);

    paint.setStrokeWidth(3);
    paint.setTextSize(60);

    StringBuilder title = new StringBuilder();
    for (Category category : detection.getCategories()) {
      title.append(category.getLabel());
      title.append("(").append(category.getScore()).append(")");
      title.append(";");
    }
    canvas.drawText(title.toString(), bBox.left, bBox.top - 10, paint);
  }
}