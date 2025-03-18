package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Models.Category;
import com.example.noteapp.Models.Notes;
import com.example.noteapp.Models.Reminder;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class NotesTakerActivity extends AppCompatActivity {

    EditText editText_title, editText_notes;
    ImageView imageView_save, imageView_export;
    Notes notes;
    boolean isOldNotes = false;
    RoomDB database;
    Spinner spinnerCategory;
    List<Category> categories = new ArrayList<>();
    int selectedCategoryId = -1;
    ImageView imageViewAddCategory;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        spinnerCategory = findViewById(R.id.spinner_category);
        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);
        imageView_save = findViewById(R.id.imageView_save);
        imageView_export = findViewById(R.id.imageView_export);
        imageViewAddCategory = findViewById(R.id.imageView_add_category);


        database = RoomDB.getInstance(this);

        loadCategories();

        // Nút thêm danh mục
        imageViewAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        // Xử lý khi chọn danh mục
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories.stream().map(Category::getName).collect(Collectors.toList())
        );
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnTouchListener((v, event) -> {
            v.setOnLongClickListener(v1 -> {
                // Lấy vị trí được chọn
                int position = spinnerCategory.getSelectedItemPosition();
                if (position >= 0 && position < categories.size()) {
                    showCategoryOptionsPopup(v, categories.get(position));
                }
                return true;
            });
            return false;
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Khi click, chọn danh mục để sử dụng
                selectedCategoryId = categories.get(position).getId();

                // Hiển thị thông báo khi người dùng chọn (tùy chọn)
                Toast.makeText(NotesTakerActivity.this, "Selected Category: " + categories.get(position).getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không chọn danh mục
                selectedCategoryId = -1;
            }
        });


        // Get the note if it is being edited
        notes = (Notes) getIntent().getSerializableExtra("old_note");
        if (notes != null) {
            isOldNotes = true;
            editText_title.setText(notes.getTitle());
            editText_notes.setText(notes.getNotes());
        } else {
            notes = new Notes(); // New note
        }

        imageView_save.setOnClickListener(v -> saveNote());

        imageView_export.setOnClickListener(v -> {
            try {
                exportNoteToWord(notes);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NotesTakerActivity.this, "Error exporting note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showCategoryOptionsPopup(View view, Category category) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_category_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_update_category) {
                showUpdateCategoryDialog(category);
                return true;
            } else if (item.getItemId() == R.id.menu_delete_category) {
                showDeleteCategoryDialog(category);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }


    private void saveNote() {
        String title = editText_title.getText().toString().trim();
        String content = editText_notes.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Note content cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a");
        Date date = new Date();

        notes.setTitle(title);
        notes.setNotes(content);
        notes.setDate(formatter.format(date));
        notes.setCategoryId(selectedCategoryId); // Lưu ID danh mục được chọn

        new Thread(() -> {
            // If old note, update it
            if (!isOldNotes) {
                database.mainDAO().update(notes.getID(), notes.getTitle(), notes.getNotes());
            } else { // If new note, insert it
                database.mainDAO().insert(notes);
            }

            // Return the result to MainActivity
            Intent intent = new Intent();
            intent.putExtra("note", notes);
            setResult(Activity.RESULT_OK, intent);

            runOnUiThread(() -> {
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void exportNoteToWord(Notes notes) throws IOException {
        if (notes.getTitle() == null || notes.getTitle().isEmpty()) {
            Toast.makeText(this, "Please provide a title for the note before exporting!", Toast.LENGTH_SHORT).show();
            return;
        }

        XWPFDocument document = new XWPFDocument();

        XWPFParagraph titleParagraph = document.createParagraph();
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(notes.getTitle());
        titleRun.setBold(true);
        titleRun.setFontSize(20);

        XWPFParagraph contentParagraph = document.createParagraph();
        XWPFRun contentRun = contentParagraph.createRun();
        contentRun.setText(notes.getNotes());
        contentRun.setFontSize(16);

        File file = new File(getExternalFilesDir(null), notes.getTitle() + ".docx");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            document.write(outputStream);
        }
        document.close();

        Toast.makeText(this, "File saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    private void loadCategories() {
        new Thread(() -> {
            categories = database.categoryDAO().getAllCategories();
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories.stream().map(Category::getName).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            });
        }).start();
    }
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);

        EditText editTextCategoryName = view.findViewById(R.id.editText_category_name);
        Button buttonAddCategory = view.findViewById(R.id.button_add_category);

        AlertDialog dialog = builder.create();

        buttonAddCategory.setOnClickListener(v -> {
            String categoryName = editTextCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                Category category = new Category();
                category.setName(categoryName);

                new Thread(() -> {
                    database.categoryDAO().insert(category);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Category added successfully.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCategories(); // Cập nhật Spinner
                    });
                }).start();
            } else {
                Toast.makeText(this, "Category name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showUpdateCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);

        EditText editTextCategoryName = view.findViewById(R.id.editText_category_name);
        Button buttonAddCategory = view.findViewById(R.id.button_add_category);
        buttonAddCategory.setText("Update");
        editTextCategoryName.setText(category.getName());

        AlertDialog dialog = builder.create();

        buttonAddCategory.setOnClickListener(v -> {
            String updatedName = editTextCategoryName.getText().toString().trim();
            if (!updatedName.isEmpty()) {
                category.setName(updatedName);

                new Thread(() -> {
                    database.categoryDAO().update(category);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCategories(); // Reload danh mục
                    });
                }).start();
            } else {
                Toast.makeText(this, "Category name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showDeleteCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Category");
        builder.setMessage("Are you sure you want to delete this category?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteCategory(category);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCategory(Category category) {
        new Thread(() -> {
            database.categoryDAO().delete(category);

            // Cập nhật các ghi chú liên quan
            database.mainDAO().updateNotesCategoryToNull(category.getId());

            runOnUiThread(() -> {
                Toast.makeText(this, "Category deleted successfully.", Toast.LENGTH_SHORT).show();
                loadCategories();
            });
        }).start();
    }

}
