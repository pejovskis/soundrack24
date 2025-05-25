package com.example.soundrack24;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Model.DatabaseHelper;
import Model.FavPerformance;
import Model.Ilocation;
import Model.Plocation;
import Model.Ulocation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainLayout#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainLayout extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LinearLayout mainLayout;

    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort midiInputPort;
    private List<FavPerformance> favPerformances;
    private TextView selectedFavButton;

    public MainLayout() {
        // Required empty public constructor
    }

    public static MainLayout newInstance(String param1, String param2) {
        MainLayout fragment = new MainLayout();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        initMidi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, container, false);

        // ScrollView scrollStyles = view.findViewById(R.id.scroll_styles);
        // ScrollView scrollPerformances = view.findViewById(R.id.scroll_performances);

        mainLayout = view.findViewById(R.id.mainLayout);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this.getContext());
        // styles = dbHelper.getAllStyles(this.getContext());
        favPerformances = dbHelper.getAllFavPerformances(this.getContext());

        populateMainLayout();
        // populateStylesContainer(scrollStyles, styles);
        // populatePerformancesContainer(scrollPerformances, performances);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initMidi() {
        midiManager = (MidiManager) requireContext().getSystemService(Context.MIDI_SERVICE);
        MidiDeviceInfo[] devices = midiManager.getDevices();
        if (devices.length == 0) {
            Log.e("MIDI", "No MIDI devices found!");
            return;
        }

        MidiDeviceInfo deviceInfo = devices[0];
        MidiDeviceInfo.PortInfo inputPort = Arrays.stream(deviceInfo.getPorts()).filter(port -> port.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT).findFirst().orElse(null);

        if (inputPort == null) {
            Log.e("MIDI", "No input port available!");
            return;
        }

        midiManager.openDevice(deviceInfo, device -> {
            if (device == null) {
                Log.e("MIDI", "Failed to open MIDI device!");
                return;
            }

            midiDevice = device;
            midiInputPort = midiDevice.openInputPort(inputPort.getPortNumber());
            if (midiInputPort == null) {
                Log.e("MIDI", "Failed to open MIDI input port!");
            } else {
                Log.d("MIDI", "MIDI input port opened successfully!");
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private void sendStyleChange(int msb, int lsb, int program) throws IOException {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x00, (byte) msb}, 0, 3);
            } catch (IOException e) {
                Log.e("MIDI", "MSB Send Failed: " + e.getMessage());
            }
        }, 150); // Send immediately

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x20, (byte) lsb}, 0, 3);
            } catch (IOException e) {
                Log.e("MIDI", "LSB Send Failed: " + e.getMessage());
            }
        }, 150); // Wait 167ms before sending LSB

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xC0, (byte) (program - 1)}, 0, 2);
            } catch (IOException e) {
                Log.e("MIDI", "Program Change Send Failed: " + e.getMessage());
            }
        }, 150); // Wait another 167ms before sending Program Change
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeMidi();
    }

    private void closeMidi() {
        if (midiInputPort != null) {
            try {
                midiInputPort.close();
                midiInputPort = null;
                Log.d("MIDI", "MIDI input port closed.");
            } catch (IOException e) {
                Log.e("MIDI", "Error closing MIDI input port: " + e.getMessage());
            }
        }

        if (midiDevice != null) {
            try {
                midiDevice.close();
                midiDevice = null;
                Log.d("MIDI", "MIDI device closed.");
            } catch (IOException e) {
                Log.e("MIDI", "Error closing MIDI device: " + e.getMessage());
            }
        }
    }

    private void setKeyboardPerformance(int msb, int lsb, int program) {
        if (midiInputPort == null) {
            Log.e("SetKeyboardPerformance", "MIDI input port not available!");
            return;
        }

        try {
            sendPerformanceChange(msb, lsb, program);
            Log.d("MIDI", "Performance Set: MSB=" + msb + ", LSB=" + lsb + ", Program=" + program);
        } catch (IOException e) {
            Log.e("SetKeyboardPerformance", "Error sending performance change: " + e.getMessage());
        }
    }

    private void sendPerformanceChange(int msb, int lsb, int program) throws IOException {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x00, (byte) msb}, 0, 3);
            } catch (IOException e) {
                Log.e("MIDI", "MSB Send Failed: " + e.getMessage());
            }
        }, 150); // No delay for first message

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x20, (byte) lsb}, 0, 3);
            } catch (IOException e) {
                Log.e("MIDI", "LSB Send Failed: " + e.getMessage());
            }
        }, 150); // ~167ms after first message

        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xC0, (byte) (program)}, 0, 2);
            } catch (IOException e) {
                Log.e("MIDI", "Program Change Send Failed: " + e.getMessage());
            }
        }, 150); // ~167ms after second message
    }

    private void populateMainLayout() {
        mainLayout.removeAllViews();
        mainLayout.setBackgroundColor(Color.BLACK);
        Context context = mainLayout.getContext();

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<FavPerformance> favs = db.getAllFavPerformances(getContext()); // from your new table
        Map<Integer, FavPerformance> indexed = new HashMap<>();
        for (FavPerformance f : favs) {
            indexed.put(f.getFavIndex(), f);
        }

        LinearLayout rowLayout = null;
        for (int i = 0; i < 24; i++) {
            if (i % 6 == 0) {
                rowLayout = new LinearLayout(context);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0, 1f
                ));
                mainLayout.addView(rowLayout);
            }

            TextView btn = new TextView(context);
            btn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            btn.setGravity(Gravity.CENTER);
            btn.setClickable(true);
            btn.setFocusable(true);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(28);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackground(getDefaultButtonDrawable());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            int margin = dpToPx(getContext(), 4); // 4dp margin
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);


            int slot = i + 1;
            FavPerformance partial = indexed.get(slot);

            FavPerformance fp = null;
            if (partial != null) {
                fp = new FavPerformance();
                fp.loadFavPerformance(partial.getId(), getContext()); // ← this must exist and load full row
            }


            if (fp != null && fp.getUlocation() != null && fp.getPlocation() != null && fp.getIlocation() != null) {
                final FavPerformance finalFp = fp;

                btn.setText(fp.getName() != null && !fp.getName().isEmpty() ? fp.getName() : "Fav " + slot);
                btn.setOnClickListener(v -> {
                    if (selectedFavButton != null) {
                        selectedFavButton.setBackground(getDefaultButtonDrawable());
                        selectedFavButton.setTextColor(Color.WHITE);
                    }

                    selectedFavButton = btn;
                    btn.setBackground(getSelectedButtonDrawable());
                    btn.setTextColor(Color.BLACK);

                    int uLocation = Integer.parseInt(finalFp.getUlocation().getName());
                    int pLocation = Integer.parseInt(finalFp.getPlocation().getName());
                    int iLocation = Integer.parseInt(finalFp.getIlocation().getName());

                    int lsb = uLocation - 1;
                    int program = 1 + (pLocation - 1) * 8 + (iLocation - 1);
                    setKeyboardPerformance(17, lsb, program - 1);
                });
            }

            final int favIndex;
            if (partial != null) {
                favIndex = partial.getFavIndex();
            } else {
                favIndex = slot;
            }

            btn.setOnLongClickListener(v -> {
                showMidiAssignDialog(btn, favIndex);
                return true;
            });

            rowLayout.addView(btn);
        }
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showMidiAssignDialog(TextView targetBtn, int favIndex) {
        Context ctx = requireContext();
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        android.widget.EditText name = new android.widget.EditText(ctx);
        name.setHint("Name");
        android.widget.EditText uInput = new android.widget.EditText(ctx);
        uInput.setHint("ULocation");
        android.widget.EditText pInput = new android.widget.EditText(ctx);
        pInput.setHint("PLocation");
        android.widget.EditText iInput = new android.widget.EditText(ctx);
        iInput.setHint("ILocation");

        // Try to load existing data
        FavPerformance existing = new FavPerformance();
        existing.loadFavPerformanceByFavIndex(favIndex, ctx);

        if (existing.getName() != null) name.setText(existing.getName());
        if (existing.getUlocation() != null) uInput.setText(existing.getUlocation().getName().replace("U", ""));
        if (existing.getPlocation() != null) pInput.setText(existing.getPlocation().getName().replace("P", ""));
        if (existing.getIlocation() != null) iInput.setText(existing.getIlocation().getName().replace("I", ""));

        layout.addView(name);
        layout.addView(uInput);
        layout.addView(pInput);
        layout.addView(iInput);

        new android.app.AlertDialog.Builder(ctx)
                .setTitle("Assign Slot " + favIndex)
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String sName = name.getText().toString().trim();
                    Ulocation ulocation = new Ulocation();
                    Plocation plocation = new Plocation();
                    Ilocation ilocation = new Ilocation();

                    String sULocation = "U" + uInput.getText().toString().trim();
                    String sPLocation = "P" + pInput.getText().toString().trim();
                    String sILocation = "I" + iInput.getText().toString().trim();

                    ulocation.loadUlocationByName(sULocation, getContext());
                    plocation.loadPlocationByName(sPLocation, getContext());
                    ilocation.loadIlocationByName(sILocation, getContext());

                    if (ulocation != null && plocation != null && ilocation != null) {
                        FavPerformance favPerformance = new FavPerformance(sName, ulocation, plocation, ilocation, favIndex);
                        favPerformance.saveOrUpdate(getContext());
                        targetBtn.setText(sName);
                        targetBtn.setTextColor(Color.WHITE);
                        targetBtn.setBackground(getDefaultButtonDrawable());
                        populateMainLayout(); // Refresh to sync visual state
                    } else {
                        targetBtn.setText("error");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private Drawable getDefaultButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#333333"));
        drawable.setCornerRadius(32); // rounded
        drawable.setStroke(2, Color.WHITE); // white border
        return drawable;
    }

    private Drawable getSelectedButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.YELLOW); // selected = yellow
        drawable.setCornerRadius(32);
        drawable.setStroke(2, Color.WHITE);
        return drawable;
    }

}