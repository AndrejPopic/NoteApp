package com.example.NoteApp.activates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.NoteApp.R;
import com.example.NoteApp.database.NotesDatabase;
import com.example.NoteApp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Activity2 extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private ImageView imageNote;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;


    private String selectedNoteColor;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener((v) -> onBackPressed());

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);

        textDateTime.setText(
                new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
                        .format(new Date())
        );

        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener((v) -> saveNote());

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imageRemoveWebURL).setOnClickListener(v -> {
            textWebURL.setText(null);
            layoutWebURL.setVisibility(View.GONE);
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(v -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });
        if (getIntent().getBooleanExtra("isFromQuickActions", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                } else if (type.equals("URL")) {
                    textWebURL.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNotetitle());
        textDateTime.setText(alreadyAvailableNote.getDatetitle());

        if (alreadyAvailableNote.getImagepath() != null && !alreadyAvailableNote.getImagepath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagepath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagepath();
        }
        if (alreadyAvailableNote.getWeb_link() != null && !alreadyAvailableNote.getWeb_link().trim().isEmpty()) {
            textWebURL.setText(alreadyAvailableNote.getWeb_link());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title cant be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note cant be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNotetitle(inputNoteText.getText().toString());
        note.setDatetitle(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagepath(selectedImagePath);

        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWeb_link(textWebURL.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

            }
        }
        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener((v) -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.ImageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.ImageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.ImageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.ImageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.ImageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener((v) -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(android.R.color.transparent); //.setImageResource(0);   .setBackground(null);  .setImageResource(android.R.color.transparent);
            imageColor3.setImageResource(android.R.color.transparent);
            imageColor4.setImageResource(android.R.color.transparent);
            imageColor5.setImageResource(android.R.color.transparent);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener((v) -> {
            selectedNoteColor = "#E6BF00";
            imageColor1.setImageResource(android.R.color.transparent);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(android.R.color.transparent);
            imageColor4.setImageResource(android.R.color.transparent);
            imageColor5.setImageResource(android.R.color.transparent);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener((v) -> {
            selectedNoteColor = "#8a0303";
            imageColor1.setImageResource(android.R.color.transparent);
            imageColor2.setImageResource(android.R.color.transparent);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(android.R.color.transparent);
            imageColor5.setImageResource(android.R.color.transparent);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener((v) -> {
            selectedNoteColor = "#006994";
            imageColor1.setImageResource(android.R.color.transparent);
            imageColor2.setImageResource(android.R.color.transparent);
            imageColor3.setImageResource(android.R.color.transparent);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(android.R.color.transparent);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener((v) -> {
            selectedNoteColor = "#567d46";
            imageColor1.setImageResource(android.R.color.transparent);
            imageColor2.setImageResource(android.R.color.transparent);
            imageColor3.setImageResource(android.R.color.transparent);
            imageColor4.setImageResource(android.R.color.transparent);
            imageColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
                case "#E6BF00":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#8a0303":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#006994":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#567d46":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        Activity2.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }

    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity2.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNoteTask().execute();

                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        }

        dialogDeleteNote.show();
    }


    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));

    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);

                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity2.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(Activity2.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    Toast.makeText(Activity2.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    textWebURL.setText(inputURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogAddURL.dismiss());
        }
        dialogAddURL.show();
    }
}

