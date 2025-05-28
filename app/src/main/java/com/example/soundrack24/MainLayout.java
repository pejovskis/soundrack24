// Full MainLayout.java file with complete swap, save, delete, cancel logic
package com.example.soundrack24;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.DatabaseHelper;
import Model.FavPerformance;
import Model.Ilocation;
import Model.Plocation;
import Model.Ulocation;

public class MainLayout extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LinearLayout mainLayout;

    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort midiInputPort;
    private TextView selectedFavButton;
    private FavPerformance swapBuffer = null;
    private int swapIndex = -1;

    public MainLayout() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, container, false);
        mainLayout = view.findViewById(R.id.mainLayout);
        populateMainLayout();
        return view;
    }

    private void initMidi() {
        midiManager = (MidiManager) requireContext().getSystemService(Context.MIDI_SERVICE);
        MidiDeviceInfo[] devices = midiManager.getDevices();
        if (devices.length == 0) return;
        MidiDeviceInfo deviceInfo = devices[0];
        MidiDeviceInfo.PortInfo inputPort = Arrays.stream(deviceInfo.getPorts()).filter(p -> p.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT).findFirst().orElse(null);
        if (inputPort == null) return;
        midiManager.openDevice(deviceInfo, device -> {
            if (device == null) return;
            midiDevice = device;
            midiInputPort = device.openInputPort(inputPort.getPortNumber());
        }, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (midiInputPort != null) midiInputPort.close();
            if (midiDevice != null) midiDevice.close();
        } catch (IOException ignored) {
        }
    }

    private void sendPerformanceChange(int msb, int lsb, int program) throws IOException {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x00, (byte) msb}, 0, 3);
            } catch (IOException ignored) {
            }
        }, 150);
        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xB0, 0x20, (byte) lsb}, 0, 3);
            } catch (IOException ignored) {
            }
        }, 150);
        handler.postDelayed(() -> {
            try {
                midiInputPort.send(new byte[]{(byte) 0xC0, (byte) program}, 0, 2);
            } catch (IOException ignored) {
            }
        }, 150);
    }

    private void setKeyboardPerformance(int msb, int lsb, int program) {
        if (midiInputPort == null) return;
        try {
            sendPerformanceChange(msb, lsb, program);
        } catch (IOException ignored) {
        }
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private Drawable getDefaultButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#333333"));
        drawable.setCornerRadius(32);
        drawable.setStroke(2, Color.WHITE);
        return drawable;
    }

    private Drawable getSelectedButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.YELLOW);
        drawable.setCornerRadius(32);
        drawable.setStroke(2, Color.WHITE);
        return drawable;
    }

    private Drawable getSwapSelectedButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#444444"));
        drawable.setCornerRadius(32);
        drawable.setStroke(4, Color.YELLOW);
        return drawable;
    }

    private void showMidiAssignDialog(TextView targetBtn, int favIndex) {
        Context ctx = requireContext();
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        EditText name = new EditText(ctx);
        name.setHint("Name");
        EditText uInput = new EditText(ctx);
        uInput.setHint("ULocation");
        EditText pInput = new EditText(ctx);
        pInput.setHint("PLocation");
        EditText iInput = new EditText(ctx);
        iInput.setHint("ILocation");

        FavPerformance existing = new FavPerformance();
        existing.loadFavPerformanceByFavIndex(favIndex, ctx);

        if (existing.getName() != null) name.setText(existing.getName());
        if (existing.getUlocation() != null)
            uInput.setText(existing.getUlocation().getName().replace("U", ""));
        if (existing.getPlocation() != null)
            pInput.setText(existing.getPlocation().getName().replace("P", ""));
        if (existing.getIlocation() != null)
            iInput.setText(existing.getIlocation().getName().replace("I", ""));

        layout.addView(name);
        layout.addView(uInput);
        layout.addView(pInput);
        layout.addView(iInput);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Assign Slot " + favIndex)
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setNeutralButton("Swap", null)
                .create();

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete", (d, w) -> {
            FavPerformance.deleteByFavIndex(ctx, favIndex);
            populateMainLayout();
        });

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            String nameText = name.getText().toString().trim();
            String uRaw = uInput.getText().toString().trim();
            String pRaw = pInput.getText().toString().trim();
            String iRaw = iInput.getText().toString().trim();

            Ulocation u = null;
            Plocation p = null;
            Ilocation i = null;

            // Try from input fields
            if (!uRaw.isEmpty()) u = getU(ctx, uRaw);
            if (!pRaw.isEmpty()) p = getP(ctx, pRaw);
            if (!iRaw.isEmpty()) i = getI(ctx, iRaw);

            // If still empty → fallback to existing DB slot
            if (u == null || p == null || i == null) {
                FavPerformance fallback = new FavPerformance();
                fallback.loadFavPerformanceByFavIndex(favIndex, ctx);
                if (u == null) u = fallback.getUlocation();
                if (p == null) p = fallback.getPlocation();
                if (i == null) i = fallback.getIlocation();
                if (nameText.isEmpty()) nameText = fallback.getName();
            }

            if (u == null || p == null || i == null) {
                Toast.makeText(ctx, "Missing location info", Toast.LENGTH_SHORT).show();
                return;
            }

            swapBuffer = new FavPerformance(nameText, u, p, i, favIndex);
            swapIndex = favIndex;
            targetBtn.setBackground(getSwapSelectedButtonDrawable());
            Toast.makeText(ctx, "Selected slot " + favIndex + " for swap", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            FavPerformance f = new FavPerformance(
                    name.getText().toString(),
                    getU(ctx, uInput.getText().toString()),
                    getP(ctx, pInput.getText().toString()),
                    getI(ctx, iInput.getText().toString()),
                    favIndex
            );
            f.saveOrUpdate(ctx);
            populateMainLayout();
            dialog.dismiss();
        });
    }

    private Ulocation getU(Context ctx, String raw) {
        Ulocation u = new Ulocation();
        u.loadUlocationByName("U" + raw, ctx);
        return u;
    }

    private Plocation getP(Context ctx, String raw) {
        Plocation p = new Plocation();
        p.loadPlocationByName("P" + raw, ctx);
        return p;
    }

    private Ilocation getI(Context ctx, String raw) {
        Ilocation i = new Ilocation();
        i.loadIlocationByName("I" + raw, ctx);
        return i;
    }

    private void populateMainLayout() {
        mainLayout.removeAllViews();
        mainLayout.setBackgroundColor(Color.BLACK);
        Context context = mainLayout.getContext();
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<FavPerformance> favs = db.getAllFavPerformances(getContext());
        Map<Integer, FavPerformance> indexed = new HashMap<>();
        for (FavPerformance f : favs) indexed.put(f.getFavIndex(), f);

        LinearLayout rowLayout = null;
        for (int i = 0; i < 24; i++) {
            if (i % 6 == 0) {
                rowLayout = new LinearLayout(context);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
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
            int margin = dpToPx(getContext(), 4);
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);

            int slot = i + 1;
            FavPerformance partial = indexed.get(slot);
            FavPerformance fp = null;
            if (partial != null) {
                fp = new FavPerformance();
                fp.loadFavPerformance(partial.getId(), getContext());
            }

            final int favIndex = slot;
            FavPerformance currentFp = fp;

            if (currentFp != null && currentFp.getUlocation() != null && currentFp.getPlocation() != null && currentFp.getIlocation() != null) {
                // good
                btn.setText(currentFp.getName() != null && !currentFp.getName().isEmpty() ? currentFp.getName() : "" + slot);
            } else {
                btn.setText("");
            }

            btn.setOnClickListener(v -> {
                if (swapBuffer != null && swapIndex != -1) {
                    // Save swap buffer to the new selected slot
                    FavPerformance swapCopy = new FavPerformance(
                            swapBuffer.getName(),
                            swapBuffer.getUlocation(),
                            swapBuffer.getPlocation(),
                            swapBuffer.getIlocation(),
                            favIndex // target slot
                    );
                    swapCopy.saveOrUpdate(getContext());

                    // Delete the original (from where swapBuffer came)
                    FavPerformance.deleteByFavIndex(getContext(), swapIndex);

                    swapBuffer = null;
                    swapIndex = -1;
                    selectedFavButton = null;
                    populateMainLayout();
                    return;
                }

                if (currentFp != null) {
                    if (selectedFavButton != null) {
                        selectedFavButton.setBackground(getDefaultButtonDrawable());
                        selectedFavButton.setTextColor(Color.WHITE);
                    }
                    selectedFavButton = btn;
                    btn.setBackground(getSelectedButtonDrawable());
                    btn.setTextColor(Color.BLACK);
                    int u = Integer.parseInt(currentFp.getUlocation().getName());
                    int p = Integer.parseInt(currentFp.getPlocation().getName());
                    int iLoc = Integer.parseInt(currentFp.getIlocation().getName());
                    int lsb = u - 1;
                    int program = 1 + (p - 1) * 8 + (iLoc - 1);
                    setKeyboardPerformance(17, lsb, program - 1);
                }
            });

            btn.setOnLongClickListener(v -> {
                showMidiAssignDialog(btn, favIndex);
                return true;
            });

            rowLayout.addView(btn);
        }
    }
}
