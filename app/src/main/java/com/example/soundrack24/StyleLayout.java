package com.example.soundrack24;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import com.google.android.material.button.MaterialButton;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import Model.ButtonFactory;
import Model.DatabaseHelper;
import Model.FavPerformance;
import Model.FavStyle;
import Model.Ilocation;
import Model.MidiController;
import Model.Plocation;
import Model.Ulocation;

public class StyleLayout extends Fragment {

    // Fields
    private LinearLayout mainLayout;
    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort midiInputPort;
    private TextView selectedFavButton;
    private FavStyle swapBuffer = null;
    private long swapId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, container, false);
        mainLayout = view.findViewById(R.id.mainLayout);
        populateMainLayout();
        return view;
    }

    private void populateMainLayout() {
        mainLayout.removeAllViews();
        mainLayout.setBackgroundColor(Color.BLACK);
        Context ctx = requireContext();
        List<FavStyle> favs = DatabaseHelper.getInstance(ctx).getAllFavStyles(ctx);

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
            FavStyle fs = favs.stream().filter(f -> f.getId() == id).findFirst().orElse(null);

            // Create Button & define shit
            TextView btn = ButtonFactory.createDefaultButton(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            int margin = ButtonFactory.dpToPx(ctx, 4);
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);

            // Add click-event listeners
            if (fs != null) {
                btn.setText(fs.getName() != null && !fs.getName().isEmpty() ? fs.getName() : "");
                btn.setOnClickListener(v -> {
                    // If swapping is active
                    if (swapBuffer != null && swapId != -1) {
                        swapButtons(id, fs, ctx);
                        return;
                    }
                    // Normal click event
                    if (!fs.isFsEmpty()) {
                        clickButton(btn, fs);
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

    private void clickButton(TextView btn, FavStyle finalFs) {
        // Reset previous selected button, if any
        if (selectedFavButton != null && selectedFavButton != btn) {
            selectedFavButton.setBackground(ButtonFactory.getDefaultButtonDrawable());
            selectedFavButton.setTextColor(Color.WHITE);
        }

        selectedFavButton = btn;
        btn.setBackground(ButtonFactory.getSelectedButtonDrawable());
        btn.setTextColor(Color.BLACK);

        int u = Integer.parseInt(finalFs.getUlocation().getName());
        int p = Integer.parseInt(finalFs.getPlocation().getName());
        int iLoc = Integer.parseInt(finalFs.getIlocation().getName());
        int lsb = u - 1;
        int program = 1 + (p - 1) * 8 + (iLoc - 1);
        setKeyboardStyle(2, lsb, program - 1);
    }

    private void swapButtons(long id, FavStyle finalFs, Context ctx) {
        FavStyle swapped;
        if (!swapBuffer.isFsEmpty()) {
            swapped = new FavStyle(swapBuffer.getName(), swapBuffer.getUlocation(), swapBuffer.getPlocation(), swapBuffer.getIlocation());
        } else {
            swapped = new FavStyle();
        }
        swapped.setId(id);

        FavStyle overwritten;
        if (!finalFs.isFsEmpty()) {
            overwritten = new FavStyle(finalFs.getName(), finalFs.getUlocation(), finalFs.getPlocation(), finalFs.getIlocation());
        } else {
            overwritten = new FavStyle();
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

        FavStyle existing = new FavStyle();
        existing.loadFavStyle(id, ctx);

        if (existing.getName() != null) name.setText(existing.getName());
        if (existing.getUlocation() != null)
            uInput.setText(existing.getUlocation().getName().replace("U", ""));
        if (existing.getPlocation() != null)
            pInput.setText(existing.getPlocation().getName().replace("P", ""));
        if (existing.getIlocation() != null)
            iInput.setText(existing.getIlocation().getName().replace("I", ""));

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
            FavStyle target = new FavStyle();
            target.loadFavStyle(id, ctx);

            if (swapBuffer == null) {
                Ulocation u = getU(ctx, uInput.getText().toString());
                Plocation p = getP(ctx, pInput.getText().toString());
                Ilocation i = getI(ctx, iInput.getText().toString());

                swapBuffer = new FavStyle(name.getText().toString(), u, p, i);
                swapId = id;
                targetBtn.setBackground(ButtonFactory.getSwapSelectedButtonDrawable());
                Toast.makeText(ctx, "Selected slot " + id + " for swap", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnHelp.setOnClickListener(v -> {
            dialog.dismiss(); // close current assign dialog
            showHelpDialog(ctx);
        });

        btnRemove.setOnClickListener(v -> {
            FavStyle fs = new FavStyle();
            fs.loadFavStyle(id, ctx);
            fs.remove(ctx);
            populateMainLayout();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            FavStyle f = new FavStyle(
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

    private void showHelpDialog(Context ctx) {
        View layout = LayoutInflater.from(ctx).inflate(R.layout.dialog_help, null);

        MaterialButton btnClose = layout.findViewById(R.id.btnCloseHelp);

        AlertDialog helpDialog = new AlertDialog.Builder(ctx)
                .setView(layout)
                .create();

        btnClose.setOnClickListener(v -> helpDialog.dismiss());

        helpDialog.show();

        TextView userNameText = layout.findViewById(R.id.userNameText);
        MaterialButton signOutBtn = layout.findViewById(R.id.btnSignOut);

        // Assuming FirebaseAuth is setup
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userNameText.setText("User: " + user.getDisplayName());
        } else {
            userNameText.setText("User: Guest");
        }

        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            helpDialog.dismiss();
        });

        Window window = helpDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (ctx.getResources().getDisplayMetrics().widthPixels * 0.7);
            window.setAttributes(layoutParams);
        }
    }

    private void setKeyboardStyle(int msb, int lsb, int program) {
        midiInputPort = MidiController.getInstance().getPort();
        if (midiInputPort == null) {
            return;
        }
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