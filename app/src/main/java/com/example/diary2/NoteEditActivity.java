package com.example.diary2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.IOException;

public class NoteEditActivity extends Activity {
    public static final int TAKE_PHOTO=1;
    private ImageView picture;
    private Uri imageUri;

    private EditText titleEditText=null;
    private EditText contentEditText=null;
    private EditText authorEditText=null;
    private String noteId=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        Button takePhoto=(Button) findViewById(R.id.take_photo);
        picture=(ImageView) findViewById(R.id.picture);

        titleEditText=(EditText) NoteEditActivity.this.findViewById(R.id.title);
        contentEditText=(EditText) NoteEditActivity.this.findViewById(R.id.content);
        authorEditText=(EditText) NoteEditActivity.this.findViewById(R.id.author);
        initNoteEditValue();

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File对象，用于储存拍照后的图片
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri=Uri.fromFile(outputImage);
                //启动相机程序
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });


        //取消按钮监听
        this.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View org0) {
                NoteEditActivity.this.finish();
            }
        });
        this.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View org0) {
                final String title=titleEditText.getText().toString();
                final String content=contentEditText.getText().toString();
                String author=authorEditText.getText().toString();
                if("".equals(author)){
                    author="xxx";
                }
                //判断标题是否为空，不为空才可保存
                if("".equals(title)||"".equals(content)){
                    Toast.makeText(NoteEditActivity.this,"标题或者内容不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                //提示保存
                final String finalAuthor = author;
                new AlertDialog.Builder(NoteEditActivity.this).setTitle("提示框").setMessage("确认保存日记？").setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ContentValues values=new ContentValues();
                        values.put("title",title);
                        values.put("content",content);
                        values.put("author", finalAuthor);
                        //如果noteId不为空那么就是更新，为空就是添加
                        if (null == noteId || "".equals(noteId))
                            DBService.addNote(values);
                        else
                            DBService.updateNoteById(
                                    Integer.valueOf(noteId),
                                    values);
                        //结束当前activity
                        NoteEditActivity.this.finish();
                        Toast.makeText(NoteEditActivity.this, "保存成功！！",
                                Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("取消",null).show();
            }
        });
    }




    //初始化编辑页面的值（如果进入该页面时存在一个id）如标题、内容
    private void initNoteEditValue() {
        //从Intent中获取id的值
        long id=this.getIntent().getLongExtra("id",-1L);
        //如果有传入id那么id!=-1
        if(id!=-1L){
            //使用noteId保存id
            noteId=String.valueOf(id);
            //查询该id的笔记
            Cursor cursor=DBService.queryNoteById((int) id);
            if(cursor.moveToFirst()){
                //将内容提取出来
                titleEditText.setText(cursor.getString(1));
                contentEditText.setText(cursor.getString(2));
                authorEditText.setText(cursor.getString(3));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
}
