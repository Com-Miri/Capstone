package com.example.finaltest1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by 태경원 on 2017-02-20.
 */

public class Transper extends AppCompatActivity {
    private String smartmirror_ip;
    private SharedPreferences prefs;
    static final int REQUEST_OPEN_IMAGE = 1;
    TextView messageText;
    Button uploadButton;
    Button runButton;
    Button closeButton;
    ImageView mImageView;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    /**********  File Path *************/
    //final String uploadFilePath = "storage/emulated/0/";//경로를 모르겠으면, 갤러리 어플리케이션 가서 메뉴->상세 정보
    //final String uploadFileName = "testimage.jpg"; //전송하고자하는 파일 이름
    String uploadFilePath;
    String uploadFileName;
    String mCurrentPhotoPath;
    Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transper);
        uploadButton = (Button)findViewById(R.id.uploadButton);
        runButton = (Button) findViewById(R.id.runButton);
        closeButton = (Button) findViewById(R.id.closeButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        mImageView = (ImageView) findViewById(R.id.trs_iv);
        uploadFileName = getIntent().getExtras().getString("name")+".png";
        uploadFilePath = getIntent().getExtras().getString("path");

        prefs = getSharedPreferences("login", 0);
        smartmirror_ip = prefs.getString("SMARTMIRROR_IP", "0.0.0.0");
        Log.d("smartmirror IP : ", smartmirror_ip);

        setPic();
        messageText.setText("Uploading file path :- "+uploadFilePath+""+uploadFileName);
        /************* Php script path ****************/
        upLoadServerUri = "http://"+smartmirror_ip+"/UploadToServer.php";//서버컴퓨터의 ip주소
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(Transper.this, "", "Uploading file...", true);
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });
                        uploadFile(uploadFilePath+""+uploadFileName);
                    }
                }).start();
            }
        });
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("run file started.....");
                            }
                        });
                        runFile();
                        //messageText.setText("fin");
                    }
                }).start();
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("close file .....");
                            }
                        });
                        closeFile();
                    }
                }).start();
            }
        });
    }
    public void setPic(){
        mBitmap = decodeFile(uploadFilePath+""+uploadFileName);
        mImageView.setImageBitmap(mBitmap);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_open_img:
                Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getPictureIntent.setType("image/*");
                Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
                        pickPictureIntent
                });
                startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imgUri = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
                            null, null, null);
                    cursor.moveToFirst();

                    int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mCurrentPhotoPath = cursor.getString(colIndex);
                    cursor.close();
                    uploadFileName = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/")+1);
                    uploadFilePath = mCurrentPhotoPath.substring(0, mCurrentPhotoPath.lastIndexOf("/")+1);
                    messageText.setText("Uploading file path :- "+uploadFilePath+""+uploadFileName);

                }
                break;
        }
    }
    public void closeFile() {
        try {
            String responseMsg = null;
            URL u = new URL("http://"+smartmirror_ip+":8080/close.html");
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
            while(true) {
                responseMsg = br.readLine();
                if(responseMsg == null) break;
                Log.d("[android command]", "Response : "+responseMsg);
            }
            huc.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("MalformedURLException Exception : check script url.");
                    Toast.makeText(Transper.this, "MalformedURLException",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Close file to server", "error: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Got Exception : see logcat ");
                    Toast.makeText(Transper.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Close file Exception", "Exception : " + e.getMessage(), e);
        } finally {
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("close success");
                }
            });
        }
    }
    public void runFile() {
        try {
            String responseMsg = null;
            URL u = new URL("http://"+smartmirror_ip+":8080/display.html?filename="+uploadFileName);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
            while(true) {
                responseMsg = br.readLine();
                if(responseMsg == null) break;
                Log.d("[android command]", "Response : "+responseMsg);
            }
            huc.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("MalformedURLException Exception : check script url.");
                    Toast.makeText(Transper.this, "MalformedURLException",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Run file to server", "error: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Got Exception : see logcat ");
                    Toast.makeText(Transper.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Run file Exception", "Exception : " + e.getMessage(), e);
        } finally {
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("display success");
                }
            });
        }
    }
    public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath+""+uploadFileName);
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :"
                            +uploadFilePath+""+uploadFileName);
                }
            });
            return 0;
        }
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    +uploadFileName;
                            messageText.setText(msg);
                            Toast.makeText(Transper.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                dialog.dismiss();
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(Transper.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(Transper.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file Exception", "Exception : "
                        + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;
        } // End else block
    }

}
