// FULL MAINLAYOUT FILE REWRITTEN WITHOUT FAV_INDEX
package com.example.soundrack24;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import com.google.android.material.button.MaterialButton;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import Model.ButtonFactory;
import Model.DatabaseHelper;
import Model.FavPerformance;
import Model.Ilocation;
import Model.Plocation;
import Model.Ulocation;

public class MainLayout extends Fragment {

    // Fields
    private LinearLayout mainLayout;
    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort midiInputPort;
    private TextView selectedFavButton;
    private FavPerformance swapBuffer = null;
    private long swapId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, container, false);
        mainLayout = view.findViewById(R.id.mainLayout);
        initMidi();
        populateMainLayout();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (midiInputPort != null) midiInputPort.close();
            if (midiDevice != null) midiDevice.close();
        } catch (IOException ignored) {}
    }

    private void initMidi() {
        midiManager = (MidiManager) requireContext().getSystemService(Context.MIDI_SERVICE);
        MidiDeviceInfo[] devices = midiManager.getDevices();
        if (devices.length == 0) return;
        MidiDeviceInfo deviceInfo = devices[0];
        MidiDeviceInfo.PortInfo inputPort = Arrays.stream(deviceInfo.getPorts())
                .filter(p -> p.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT)
                .findFirst().orElse(null);
        if (inputPort == null) return;
        midiManager.openDevice(deviceInfo, device -> {
            if (device == null) return;
            midiDevice = device;
            midiInputPort = device.openInputPort(inputPort.getPortNumber());
        }, new Handler(Looper.getMainLooper()));
    }

    private void populateMainLayout() {
        mainLayout.removeAllViews();
        mainLayout.setBackgroundColor(Color.BLACK);
        Context ctx = requireContext();
        List<FavPerformance> favs = DatabaseHelper.getInstance(ctx).getAllFavPerformances(ctx);

        // Populating rows
        LinearLayout rowLayout = null;
        for (int i = 0; i < 24; i++) {
            if (i % 6 == 0) {
                rowLayout = new LinearLayout(ctx);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
                mainLayout.addView(rowLayout);
            }

            long id = i + 1;
            FavPerformance fp = favs.stream().filter(f -> f.getId() == id).findFirst().orElse(null);

            // Create Button & define shit
            TextView btn = ButtonFactory.createDefaultButton(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            int margin = ButtonFactory.dpToPx(ctx, 4);
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);

            // Add click-event listeners
            if (fp != null) {
                btn.setText(fp.getName() != null && !fp.getName().isEmpty() ? fp.getName() : "");
                btn.setOnClickListener(v -> {
                    // If swapping is active
                    if (swapBuffer != null && swapId != -1) {
                        swapButtons(id, fp, ctx);
                        return;
                    }
                    // Normal click event
                    if (!fp.isFpEmpty()) {
                        clickButton(btn, fp);
                    }
                });
            }

            // Add hold-event listener
            final long assignedId = id;
            btn.setOnLongClickListener(v -> {
                showMidiAssignDialog(btn, assignedId);
                return true;
            });

            rowLayout.addView(btn);
        }
    }

    private void clickButton(TextView btn, FavPerformance finalFp) {
        // Reset previous selected button, if any
        if (selectedFavButton != null && selectedFavButton != btn) {
            selectedFavButton.setBackground(ButtonFactory.getDefaultButtonDrawable());
            selectedFavButton.setTextColor(Color.WHITE);
        }

        selectedFavButton = btn;
        btn.setBackground(ButtonFactory.getSelectedButtonDrawable());
        btn.setTextColor(Color.BLACK);

        int u = Integer.parseInt(finalFp.getUlocation().getName());
        int p = Integer.parseInt(finalFp.getPlocation().getName());
        int iLoc = Integer.parseInt(finalFp.getIlocation().getName());
        int lsb = u - 1;
        int program = 1 + (p - 1) * 8 + (iLoc - 1);
        setKeyboardPerformance(17, lsb, program - 1);
    }

    private void swapButtons(long id, FavPerformance finalFp, Context ctx) {
        FavPerformance swapped;
        if (!swapBuffer.isFpEmpty()) {
            swapped = new FavPerformance(swapBuffer.getName(), swapBuffer.getUlocation(), swapBuffer.getPlocation(), swapBuffer.getIlocation());
        } else {
            swapped = new FavPerformance();
        }
        swapped.setId(id);

        FavPerformance overwritten;
        if (!finalFp.isFpEmpty()) {
            overwritten = new FavPerformance(finalFp.getName(), finalFp.getUlocation(), finalFp.getPlocation(), finalFp.getIlocation());
        } else {
            overwritten = new FavPerformance();
        }
        overwritten.setId(swapId);

        swapped.update(ctx);
        overwritten.update(ctx);

        swapBuffer = null;
        swapId = -1;
        selectedFavButton = null;
        populateMainLayout();
    }

    private void showMidiAssignDialog(TextView targetBtn, long id) {
        Context ctx = requireContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View layout = inflater.inflate(R.layout.dialog_assign_slot, null);

        EditText name = layout.findViewById(R.id.editName);
        EditText uInput = layout.findViewById(R.id.editU);
        EditText pInput = layout.findViewById(R.id.editP);
        EditText iInput = layout.findViewById(R.id.editI);

        MaterialButton btnSwap = layout.findViewById(R.id.btnSwap);
        MaterialButton btnHelp = layout.findViewById(R.id.btnHelp);
        MaterialButton btnRemove = layout.findViewById(R.id.btnRemove);
        MaterialButton btnCancel = layout.findViewById(R.id.btnCancel);
        MaterialButton btnSave = layout.findViewById(R.id.btnSave);

        FavPerformance existing = new FavPerformance();
        existing.loadFavPerformance(id, ctx);

        if (existing.getName() != null) name.setText(existing.getName());
        if (existing.getUlocation() != null) uInput.setText(existing.getUlocation().getName().replace("U", ""));
        if (existing.getPlocation() != null) pInput.setText(existing.getPlocation().getName().replace("P", ""));
        if (existing.getIlocation() != null) iInput.setText(existing.getIlocation().getName().replace("I", ""));

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(layout)
                .create();

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (ctx.getResources().getDisplayMetrics().widthPixels * 0.6);
            window.setAttributes(layoutParams);
        }

        btnSwap.setOnClickListener(v -> {
            FavPerformance target = new FavPerformance();
            target.loadFavPerformance(id, ctx);

            if (swapBuffer == null) {
                Ulocation u = getU(ctx, uInput.getText().toString());
                Plocation p = getP(ctx, pInput.getText().toString());
                Ilocation i = getI(ctx, iInput.getText().toString());

                swapBuffer = new FavPerformance(name.getText().toString(), u, p, i);
                swapId = id;
                targetBtn.setBackground(ButtonFactory.getSwapSelectedButtonDrawable());
                Toast.makeText(ctx, "Selected slot " + id + " for swap", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(ctx, "Enter valid U-P-I codes. U1-30, P1-20, I1-20", Toast.LENGTH_LONG).show();
        });

        btnRemove.setOnClickListener(v -> {
            FavPerformance fp = new FavPerformance();
            fp.loadFavPerformance(id, ctx);
            fp.remove(ctx, (int) id);
            populateMainLayout();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            FavPerformance f = new FavPerformance(
                    name.getText().toString(),
                    getU(ctx, uInput.getText().toString()),
                    getP(ctx, pInput.getText().toString()),
                    getI(ctx, iInput.getText().toString()));
            f.setId(id);
            f.update(ctx);
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

    private void setKeyboardPerformance(int msb, int lsb, int program) {
        if (midiInputPort == null) return;
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                try {
                    midiInputPort.send(new byte[]{(byte) 0xB0, 0x00, (byte) msb}, 0, 3);
                } catch (IOException ignored) {}
            }, 150);
            handler.postDelayed(() -> {
                try {
                    midiInputPort.send(new byte[]{(byte) 0xB0, 0x20, (byte) lsb}, 0, 3);
                } catch (IOException ignored) {}
            }, 150);
            handler.postDelayed(() -> {
                try {
                    midiInputPort.send(new byte[]{(byte) 0xC0, (byte) program}, 0, 2);
                } catch (IOException ignored) {}
            }, 150);
        } catch (Exception ignored) {}
    }

}
