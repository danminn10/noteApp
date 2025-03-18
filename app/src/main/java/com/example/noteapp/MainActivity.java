package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.Adapters.NotesListAdapter;
import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Models.Category;
import com.example.noteapp.Models.Notes;
import com.example.noteapp.Models.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    EditText editTextSearch;
    ImageView icon_search;
    Spinner spinner_filter_category;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_button);
        editTextSearch = findViewById(R.id.editText_search);
        icon_search = findViewById(R.id.icon_search);
        spinner_filter_category = findViewById(R.id.spinner_filter_category);

        // Initialize database
        database = RoomDB.getInstance(this);
        // Load notes
        loadNotes();
        // Load categories
        loadCategories();
        // Tạo NotificationChannel nếu ứng dụng chạy trên Android 8.0 trở lên
        ReminderReceiver.createNotificationChannel(this);


        new Thread(() -> {
            // Tải danh mục từ cơ sở dữ liệu
            categories = database.categoryDAO().getAllCategories();

            // Cập nhật Spinner trên giao diện chính
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories.stream().map(Category::getName).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_filter_category.setAdapter(adapter);
            });
        }).start();

        spinner_filter_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!categories.isEmpty()) {
                    int categoryId = categories.get(position).getId();
                    if (categoryId == -1) { // Nếu "All Notes" được chọn
                        loadNotes(); // Hiển thị tất cả ghi chú
                    } else {
                        filterNotesByCategory(categoryId); // Lọc ghi chú theo danh mục
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadNotes(); // Hiển thị tất cả ghi chú nếu không có danh mục nào được chọn
            }
        });

        // Add new note
        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            startActivityForResult(intent, 101);
        });

        // Search notes
        icon_search.setOnClickListener(v -> {
            String query = editTextSearch.getText().toString().trim();
            if (query.isEmpty()) {
                // Reload all notes if search bar is empty
                loadNotes();
            } else {
                filterNotes(query);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Notes newNote = (Notes) data.getSerializableExtra("note");
            if (newNote != null) {
                new Thread(() -> {
                    if (requestCode == 101) { // Add note
                        database.mainDAO().insert(newNote);
                    } else if (requestCode == 102) { // Edit note
                        database.mainDAO().update(newNote.getID(), newNote.getTitle(), newNote.getNotes());
                    }
                    runOnUiThread(() -> {
                        loadNotes(); // Cập nhật ghi chú
                        loadCategories(); // Cập nhật danh mục
                    });
                }).start();
            }
        }
        updateRecycler(notes);
    }
    private void filterNotesByCategory(int categoryId) {
        new Thread(() -> {
            List<Notes> filteredNotes;
            if (categoryId == -1) { // Nếu "All Notes" được chọn
                filteredNotes = database.mainDAO().getAll(); // Lấy tất cả ghi chú
            } else {
                filteredNotes = database.mainDAO().getNotesByCategory(categoryId); // Lọc theo danh mục
            }
            runOnUiThread(() -> updateRecycler(filteredNotes));
        }).start();
    }


    private void loadNotes() {
        new Thread(() -> {
            notes = database.mainDAO().getAll();
            runOnUiThread(() -> updateRecycler(notes));
        }).start();
    }

    private void updateRecycler(List<Notes> notes) {
        if (notesListAdapter == null) {
            notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(notesListAdapter);
        } else {
            notesListAdapter.updateList(notes);
            notesListAdapter.notifyDataSetChanged();
        }
    }

    private void filterNotes(String query) {
        new Thread(() -> {
            List<Notes> filteredNotes = database.mainDAO().searchNotes("%" + query + "%");
            runOnUiThread(() -> {
                if (filteredNotes.isEmpty()) {
                    Toast.makeText(this, "No matching notes found!", Toast.LENGTH_SHORT).show();
                }
                updateRecycler(filteredNotes);
            });
        }).start();
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes note) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", note);
            startActivityForResult(intent, 102);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onLongClick(Notes note, CardView cardView) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, cardView);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_pin_unpin) {
                    togglePin(note);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    deleteNoteWithUndo(note);
                    return true;
                } else if (item.getItemId() == R.id.menu_add_widget) {
                    addWidget(note.getID());
                    return true;
                } else if (item.getItemId() == R.id.menu_set_reminder) {
                    setReminderForNote(note);  // Gọi phương thức đặt nhắc nhở
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }
    };

    private void setReminderForNote(Notes note) {
        Calendar calendar = Calendar.getInstance();

        // Hiển thị DatePickerDialog để chọn ngày
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Sau khi chọn ngày, hiển thị TimePickerDialog để chọn giờ và phút
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            MainActivity.this,
                            (timeView, hourOfDay, minute) -> {
                                // Lấy thời gian chọn và cập nhật vào calendar
                                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                                long reminderTime = calendar.getTimeInMillis();

                                // Lưu nhắc nhở vào cơ sở dữ liệu
                                saveReminderToDatabase(note, reminderTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveReminderToDatabase(Notes note, long reminderTime) {
        // Tạo đối tượng Reminder với thông tin từ note và thời gian nhắc nhở
        Reminder reminder = new Reminder();
        reminder.setNoteId(note.getID());  // Lưu ID của ghi chú
        reminder.setReminderTime(reminderTime);  // Lưu thời gian nhắc nhở (timestamp)

        // Lưu nhắc nhở vào cơ sở dữ liệu trong một luồng riêng để tránh Block UI Thread
        new Thread(() -> {
            // Thêm nhắc nhở vào cơ sở dữ liệu
            database.reminderDAO().insert(reminder);  // Lưu vào bảng 'reminders'

            // Sau khi lưu nhắc nhở thành công, bạn có thể thông báo cho người dùng
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
            });

            // Có thể thêm logic khác ở đây như tạo nhắc nhở bằng AlarmManager
            scheduleReminder(reminder);  // Lên lịch nhắc nhở
        }).start();
    }

    private void scheduleReminder(Reminder reminder) {
        // Tạo Intent để nhận thông báo khi nhắc nhở
        Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
        intent.putExtra("noteId", reminder.getNoteId());

        // Sử dụng PendingIntent để thực thi Intent khi đến thời gian
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, reminder.getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cấu hình AlarmManager để nhắc nhở
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            // Đặt nhắc nhở vào thời điểm đã chọn (reminderTime)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getReminderTime(), pendingIntent);
        }

    }

    private void togglePin(Notes note) {
        boolean isPinned = note.isPinned();
        new Thread(() -> {
            database.mainDAO().pin(note.getID(), !isPinned);
            runOnUiThread(() -> {
                Toast.makeText(this, isPinned ? "Unpinned" : "Pinned", Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        }).start();
    }

    private void deleteNoteWithUndo(Notes note) {
        // Remove note immediately from database
        new Thread(() -> {
            database.mainDAO().delete(note);

            // Update UI
            runOnUiThread(() -> {
                notes.remove(note);
                notesListAdapter.notifyDataSetChanged();
                // Show Undo option
                Snackbar.make(recyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            // Restore the note if Undo is clicked
                            new Thread(() -> {
                                database.mainDAO().insert(note);
                                runOnUiThread(this::loadNotes);
                            }).start();
                        }).show();
            });
        }).start();
    }

    private void addWidget(int selectedNoteId) {
        new Thread(() -> {
            try {
                // Save selected note's ID to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("NoteWidgetPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("note_id", selectedNoteId);
                editor.apply();

                // Proceed to add or update widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
                ComponentName componentName = new ComponentName(MainActivity.this, NoteWidgetProvider.class);

                // Get the list of widget IDs already added
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

                if (appWidgetIds.length == 0) {
                    // If no widget is added, request to add it programmatically
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appWidgetManager.requestPinAppWidget(componentName, null, null);
                        // Notify the user
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Widget added to home screen!", Toast.LENGTH_SHORT).show());
                    } else {
                        // Notify if running on a version below Android 8.0
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Widget can only be added on Android 8.0 or higher.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // If widget is already present, update it with the selected note ID
                    Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                    updateIntent.putExtra("note_id", selectedNoteId); // Pass the note ID to the widget
                    sendBroadcast(updateIntent); // Send the update broadcast
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Widget updated!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error occurred while adding widget", Toast.LENGTH_SHORT).show());
            }
        }).start();  // Run in background thread
    }

    private void loadCategories() {
        new Thread(() -> {
            categories = database.categoryDAO().getAllCategories(); // Lấy tất cả danh mục từ cơ sở dữ liệu
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories.stream().map(Category::getName).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_filter_category.setAdapter(adapter);

                // Thêm danh mục "All Notes" để hiển thị tất cả ghi chú
                if (!categories.isEmpty()) {
                    Category allNotesCategory = new Category();
                    allNotesCategory.setId(-1); // Sử dụng ID đặc biệt để biểu thị "All Notes"
                    allNotesCategory.setName("All Notes");
                    categories.add(0, allNotesCategory);

                    adapter.insert("All Notes", 0); // Hiển thị "All Notes" ở đầu Spinner
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

}
