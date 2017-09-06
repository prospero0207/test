package com.example.leolai.canvas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.UUID;
import java.util.jar.Manifest;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;


    public static Bitmap bm;

    private static final int SELECT_FILE = 101;


    public static String typedText;

    private DrawingView drawView;

    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, straightBtn,
                        iconButton, textBtn, callBtn, undoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);

        //paintLayout.setVisibility(View.GONE);

        currPaint = (ImageButton)paintLayout.getChildAt(0);

        currPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));


        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);


        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        drawView.setBrushSize(smallBrush);


        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);


        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);


        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        straightBtn = (ImageButton) findViewById(R.id.straight_btn);
        straightBtn.setOnClickListener(this);


        iconButton = (ImageButton)findViewById(R.id.icon_btn);
        iconButton.setOnClickListener(this);

        textBtn = (ImageButton)findViewById(R.id.edt_btn);
        textBtn.setOnClickListener(this);

        callBtn = (ImageButton)findViewById(R.id.call_btn);
        callBtn.setOnClickListener(this);

        undoBtn = (ImageButton)findViewById(R.id.undo_btn);
        undoBtn.setOnClickListener(this);



    }

    public void paintClicked(View view){
        //use chosen color
        if(view != currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();


            drawView.setColor(color);

            imgView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));
            currPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint));
            currPaint=(ImageButton)view;

            //When the user has been erasing and clicks a paint color button,
            // we will assume that they want to switch back to drawing
            drawView.setErase(false);
            drawView.setBrushSize(smallBrush);

            //set the brush size back to the last one used when drawing rather than erasing
            //drawView.setBrushSize(drawView.getLastBrushSize());
        }

    }


    @Override
    public void onClick(View v) {

        //drawView.saveCurrenScreen();

        if(v.getId()==R.id.draw_btn){
            //draw button clicked

            drawView.setBrushSize(smallBrush);
            drawView.setLastBrushSize(smallBrush);

            //When the user clicks the draw button and chooses a brush size,
            // we need to set back to drawing in case they have previously been erasing.
            drawView.setErase(false);

            drawView.setStraight(false);

            drawView.setBitmap(false);

            drawView.setText(false);

            drawView.setCalling(false);
            drawView.setUndo(false);


        }else if(v.getId() == R.id.erase_btn){


            drawView.setErase(true);
            drawView.setBrushSize(largeBrush);
            //drawView.setLastBrushSize(mediumBrush);

            drawView.setStraight(false);

            drawView.setBitmap(false);

            drawView.setText(false);
            drawView.setCalling(false);
            drawView.setUndo(false);



        }else if(v.getId() == R.id.new_btn){

            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("開始新圖檔");
            newDialog.setMessage("即將開啟新圖檔，尚未存檔的作圖將消失，確認開啟？");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });

            newDialog.show();



        }else if(v.getId() == R.id.save_btn){

            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("儲存為圖檔");
            saveDialog.setMessage("即將將圖檔儲存至圖庫，確認?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){


                    if (android.os.Build.VERSION.SDK_INT >= 23) {

                        // Check if we have read/write permission

                        int readPermission = ActivityCompat.checkSelfPermission(MainActivity.this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE);
                        int writePermission = ActivityCompat.checkSelfPermission(MainActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                                readPermission != PackageManager.PERMISSION_GRANTED ) {
                            // If don't have permission so prompt the user.
                            MainActivity.this.requestPermissions(
                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                            android.Manifest.permission.CAMERA},
                                    REQUEST_ID_READ_WRITE_PERMISSION
                            );
                            return;
                        }
                    }


                    drawView.setDrawingCacheEnabled(true);

                    //save drawing
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");

                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "已儲存至圖庫!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "儲存失敗", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }


                    //Destroy the drawing cache so that any future drawings saved won't use the existing cache
                    drawView.destroyDrawingCache();

                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();

        }else if (v.getId() == R.id.straight_btn) {

            drawView.setBrushSize(smallBrush);
            drawView.setLastBrushSize(smallBrush);

            drawView.setStraight(true);

            drawView.setErase(false);

            drawView.setBitmap(false);

            drawView.setCalling(false);
            drawView.setUndo(false);



        }else if(v.getId() == R.id.icon_btn){

            final Dialog iconDialog = new Dialog(this);
            iconDialog.setTitle("Genogram Symbols");
            iconDialog.setContentView(R.layout.brush_chooser);

            ImageButton squareBtn = (ImageButton)iconDialog.findViewById(R.id.square_btn);
            squareBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(0);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);

                    drawView.setText(false);

                    drawView.setCalling(false);
                    drawView.setUndo(false);


                    iconDialog.dismiss();
                }
            });

            ImageButton circledBtn = (ImageButton)iconDialog.findViewById(R.id.circled_btn);
            circledBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(1);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);

                    drawView.setText(false);

                    drawView.setCalling(false);
                    drawView.setUndo(false);


                    iconDialog.dismiss();
                }
            });

            ImageButton triangleBtn = (ImageButton)iconDialog.findViewById(R.id.triangle_btn);
            triangleBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(2);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);

                    drawView.setText(false);

                    drawView.setCalling(false);
                    drawView.setUndo(false);


                    iconDialog.dismiss();
                }
            });

            ImageButton uLineBtn = (ImageButton)iconDialog.findViewById(R.id.uline_btn);
            uLineBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(3);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);


                    iconDialog.dismiss();
                }
            });
            ImageButton aLineBtn = (ImageButton)iconDialog.findViewById(R.id.aline_btn);
            aLineBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(4);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });
            ImageButton divorceBtn = (ImageButton)iconDialog.findViewById(R.id.divorce_btn);
            divorceBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(5);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });
            ImageButton sepBtn = (ImageButton)iconDialog.findViewById(R.id.separated_btn);
            sepBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(6);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });
            ImageButton cohabitBtn = (ImageButton)iconDialog.findViewById(R.id.cohabit_btn);
            cohabitBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(7);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });
            ImageButton squareCaseBtn = (ImageButton)iconDialog.findViewById(R.id.square_c_btn);
            squareCaseBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(8);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });
            ImageButton cirCaseBtn = (ImageButton)iconDialog.findViewById(R.id.circled_c_btn);
            cirCaseBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setIconType(9);

                    drawView.setBitmap(true);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setText(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    iconDialog.dismiss();
                }
            });

            iconDialog.show();

        }else if(v.getId() == R.id.edt_btn){

            final Dialog textDialog = new Dialog(this);
            textDialog.setTitle("Input Text");
            textDialog.setContentView(R.layout.text_dialog);

            Button confirmBtn = (Button)textDialog.findViewById(R.id.confirm);
            confirmBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    EditText edt = (EditText)textDialog.findViewById(R.id.str);
                    typedText = edt.getText().toString();

                    drawView.setText(true);

                    drawView.setBitmap(false);

                    drawView.setStraight(false);

                    drawView.setErase(false);
                    drawView.setCalling(false);
                    drawView.setUndo(false);

                    textDialog.dismiss();
                }
            });

            textDialog.show();


        }else if(v.getId() == R.id.call_btn){

            drawView.setCalling(true);

            drawView.setBitmap(false);

            drawView.setStraight(false);

            drawView.setErase(false);

            drawView.setText(false);
            drawView.setUndo(false);

            Intent intent = new Intent( Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Select File"), SELECT_FILE);

        } else if (v.getId() == R.id.undo_btn) {

            drawView.setBrushSize(smallBrush);

            drawView.setUndo(true);

            drawView.setCalling(false);

            drawView.setBitmap(false);

            drawView.setStraight(false);

            drawView.setErase(false);

            drawView.setText(false);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode == SELECT_FILE){
            if(resultCode == RESULT_OK){

                drawView.setSelectPicFlag(true);

                setSelectFromGalleryResult(data);

            }else if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this, "Action canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };

        // managedQuery has been deprecated
        //Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);

        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null,null,null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        //Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;


        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 600;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;


        bm = BitmapFactory.decodeFile(selectedImagePath, options);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    drawView.setDrawingCacheEnabled(true);

                    //save drawing
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");

                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "已儲存至圖庫!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "儲存失敗", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }

                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


}
