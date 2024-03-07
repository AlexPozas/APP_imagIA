package com.example.imageai.ui.home;

import static com.example.imageai.LoginActivity.nom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.camera.core.CameraX;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.imageai.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;

public class HomeFragment extends Fragment {
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Intervalo de tiempo en milisegundos para considerar un doble clic
    private long lastClickTime = 0; // Variable para almacenar el tiempo del último clic
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static ImageCapture imageCapture;
    private String url = "https://ams27.ieti.site/data";
    TextToSpeech t1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button takePhotoButton = view.findViewById(R.id.takePhoto);
        previewView = view.findViewById(R.id.previewView);
        if (cameraProviderFuture==null){
            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        }

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
        takePhotoButton.setAlpha(0f);


        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Se ha detectado un doble clic, realizar la acción correspondiente
                    if (imageCapture != null) {
                        File photoFile = createPhotoFile();
                        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()),
                                new ImageCapture.OnImageSavedCallback() {
                                    @Override
                                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                rotateImage90Degrees(photoFile);
                                                compressImage(photoFile.getAbsolutePath());
                                                String base64image=imageToBase64(photoFile.getAbsolutePath());

                                                String json = "{\"data\": {\"type\": \"image\", \"prompt\": \"Que hay en imagen?\", \"image\": [\"" + base64image + "\"], \"user\": \""+nom.getText()+"\"}}";
                                                System.out.println(json);
                                                sendHttpPostRequest(url, json);
                                               // Toast.makeText(requireContext(), "Foto guardada en " + photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                                Toast.makeText(requireContext(), "Foto enviado", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        exception.printStackTrace();
                                    }
                                });
                    }

                    // Restablecer el tiempo del último clic
                    lastClickTime = 0;
                } else {
                    // No se ha detectado un doble clic, actualizar el tiempo del último clic
                    lastClickTime = clickTime;
                }
            }
        });


        t1 = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = t1.setLanguage(new Locale("es", "ES"));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Handle error: language not supported
                        Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error: Text-to-Speech initialization failed
                    Toast.makeText(requireContext(), "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        // Configurar la vista previa
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Configurar la captura de imágenes
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        // Configurar el selector de cámara
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Asignar la vista previa y la captura de imágenes al ciclo de vida del fragmento
        try {
            cameraProvider.unbindAll(); // Desvincular todos los casos de uso antes de volver a vincular
            cameraProvider.bindToLifecycle((LifecycleOwner) requireActivity(), cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void rotateImage90Degrees(File imageFile) {
        try {
            // Decodificar el archivo de imagen en un objeto Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            // Crear una matriz de transformación para rotar la imagen 90 grados
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            // Aplicar la transformación a la imagen original
            Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                    originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            // Guardar la imagen rotada en el mismo archivo
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createPhotoFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String fileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = requireContext().getExternalFilesDir(null);
        return new File(storageDir, fileName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Liberar recursos de la cámara
        releaseCamera();
    }

    private void releaseCamera() {
        if (imageCapture != null) {
            imageCapture = null;
        }
        // Liberar otros recursos de la cámara si es necesario
    }

    public static void compressImage(String originalImagePath) {
        final long TARGET_SIZE_KB = 50; // Tamaño objetivo en kilobytes
        final int MAX_QUALITY = 100; // Calidad de compresión máxima

        try {
            File originalFile = new File(originalImagePath);
            Bitmap originalBitmap = BitmapFactory.decodeFile(originalImagePath);

            // Redimensionar la imagen
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float aspectRatio = (float) width / height;

            int targetWidth = 800; // Especifica el ancho deseado de la imagen
            int targetHeight = Math.round(targetWidth / aspectRatio);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int compressionQuality = MAX_QUALITY;
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream);

            // Reduce la calidad de compresión hasta que el tamaño del archivo sea menor o igual al tamaño objetivo
            while ((outputStream.toByteArray().length / 1024) > TARGET_SIZE_KB && compressionQuality > 0) {
                outputStream.reset(); // Limpiar el outputStream
                compressionQuality -= 5; // Reducir la calidad de compresión en un 5%
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream);
            }

            // Escribe la imagen comprimida en un archivo
            FileOutputStream fos = new FileOutputStream(originalFile);
            fos.write(outputStream.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String imageToBase64(String imagePath) {
        String base64Image = "";
        File file = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Lee la imagen como un arreglo de bytes
            byte[] imageData = new byte[(int) file.length()];
            imageInFile.read(imageData);
            // Codifica los bytes en formato Base64
            base64Image = Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64Image;
    }
    private void sendHttpPostRequest(final String urlStr, final String postData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Crea una conexión HTTP
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Escribe los datos en el cuerpo de la solicitud
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();

                    // Obtiene la respuesta
                    int responseCode = connection.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Procesa la respuesta si es necesario
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        String result = sb.toString();

                        // Muestra el resultado en un Toast
                        final String Msg = result;
                        System.out.println("Correcto");
                        System.out.println(Msg);
                        t1.speak(Msg, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        System.out.println("Mal");
                    }

                    // Cierra la conexión
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
