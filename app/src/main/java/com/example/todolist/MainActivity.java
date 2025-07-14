package com.example.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private FloatingActionButton FAB;
    private androidx.appcompat.widget.AppCompatButton appCompatButton;
    private GridView gridView;
    private LinearLayout linearLayout;
    private DBHelper dbh;
    private GridAdapter gridAdapter;
    private ArrayList<Row> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initlization();

        setListenerMethods();
    }

    public void initlization() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FAB = findViewById(R.id.floatingActionButton);
        appCompatButton = findViewById(R.id.appCompatButton);
        gridView = findViewById(R.id.gridView);
        linearLayout = findViewById(R.id.linearLayout);
        dbh = new DBHelper(MainActivity.this);
        data = dbh.getData();
        gridAdapter = new GridAdapter(this, data);
        gridView.setAdapter(gridAdapter);
        if (data.size() == 0) {
            gridView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
        }
    }

    public void setListenerMethods() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotesDialog();
            }
        };

        FAB.setOnClickListener(listener);
        appCompatButton.setOnClickListener(listener);
    }

    public void addNotesDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.show();

        Button addButton = dialog.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText1 = dialog.findViewById(R.id.editText1);
                EditText editText2 = dialog.findViewById(R.id.editText2);
                String str1 = editText1.getText().toString();
                String str2 = editText2.getText().toString();
                CheckBox checkBox = dialog.findViewById(R.id.checkBox);

                if (!(str1.equals("") || str2.equals(""))) {

                    SharedPreferences sp = getSharedPreferences("demo", MODE_PRIVATE);
                    int last = sp.getInt("last", 1);

                    Row r;
                    if (checkBox.isChecked()) {
                        r = new Row(last + 1, str1, str2, 1);
                    } else {
                        r = new Row(last + 1, str1, str2, 0);
                    }
                    dbh.insert(r);
                    linearLayout.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);

                    data = dbh.getData();
                    gridAdapter.updateData(data);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("last", last + 1);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Task added successfully!!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter full details!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void deleteCard(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbh.deleteRow(data.get(position).id);
                data = dbh.getData();
                gridAdapter.updateData(data);
                Toast.makeText(getApplicationContext(), "Task deleted successfully!!", Toast.LENGTH_SHORT).show();
                if (data.size() == 0) {
                    linearLayout.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.setTitle("Delete Task");
        alert.show();
    }

    public void updateNotesDialog(int position) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.show();

        Row row = data.get(position);
        EditText editText1 = dialog.findViewById(R.id.editText1);
        EditText editText2 = dialog.findViewById(R.id.editText2);
        editText1.setText(row._Title);
        editText2.setText(row._Discription);
        CheckBox checkBox = dialog.findViewById(R.id.checkBox);
        checkBox.setChecked(row.checked != 0);

        Button addButton = dialog.findViewById(R.id.addButton);
        addButton.setText("Update");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = editText1.getText().toString();
                String str2 = editText2.getText().toString();

                if (!(str1.equals("") || str2.equals(""))) {
                    Row r = new Row(row.id, str1, str2, checkBox.isChecked() ? 1 : 0);
                    dbh.updateRow(r);
                    data = dbh.getData();
                    gridAdapter.updateData(data);
                    Toast.makeText(MainActivity.this, "Task updated successfully!!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter full details!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void editCheckBox(int position) {
        Row row = data.get(position);
        Row r = new Row(row.id, row._Title, row._Discription, row.checked == 1 ? 0 : 1);
        dbh.updateRow(r);
        data = dbh.getData();
        gridAdapter.updateData(data);
    }

    class GridAdapter extends ArrayAdapter<Row> {

        Context context;
        LayoutInflater inflater;
        ArrayList<Row> data;
        int[] colors;

        public GridAdapter(Context context, ArrayList<Row> data) {
            super(context, R.layout.gridview_layout, data);
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.data = data;

            // Define colors from the resources
            colors = new int[]{
                    context.getResources().getColor(R.color.color1),
                    context.getResources().getColor(R.color.color2),
                    context.getResources().getColor(R.color.color3),
                    context.getResources().getColor(R.color.color4),
                    context.getResources().getColor(R.color.color5),
                    context.getResources().getColor(R.color.color6),
                    context.getResources().getColor(R.color.color7),
                    context.getResources().getColor(R.color.color8),
                    context.getResources().getColor(R.color.color9),
                    context.getResources().getColor(R.color.color10),
                    context.getResources().getColor(R.color.color11),
                    context.getResources().getColor(R.color.color12)
            };
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.gridview_layout, null);

            TextView textView1 = view.findViewById(R.id.textView1);
            TextView textView2 = view.findViewById(R.id.textView2);
            CheckBox checkBox = view.findViewById(R.id.checkBox2);
            ImageButton ib_delete = view.findViewById(R.id.imageButton_delete);
            ImageButton ib_edit = view.findViewById(R.id.imageButton_edit);
            CardView cardView = view.findViewById(R.id.cardView);

            textView1.setText(data.get(position)._Title);
            textView2.setText(data.get(position)._Discription);
            checkBox.setChecked(data.get(position).checked != 0);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editCheckBox(position);
                }
            });

            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCard(position);
                }
            });

            ib_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateNotesDialog(position);
                }
            });

            // Set the card background color
            int colorIndex = position % colors.length;
            cardView.setCardBackgroundColor(colors[colorIndex]);

            return view;
        }

        public void updateData(ArrayList<Row> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }
}
