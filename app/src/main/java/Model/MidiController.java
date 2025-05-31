package Model;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

public class MidiController {

    private static MidiController instance;
    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort midiInputPort;
    private boolean initializing = false;

    public static MidiController getInstance() {
        if (instance == null) instance = new MidiController();
        return instance;
    }

    public void init(Context context) {
        if (midiDevice != null && midiInputPort != null) return;
        if (initializing) return;

        initializing = true;
        midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        MidiDeviceInfo[] infos = midiManager.getDevices();

        if (infos.length == 0) {
            Log.e("MidiController", "No MIDI devices available");
            initializing = false;
            return;
        }

        MidiDeviceInfo.PortInfo portInfo = null;
        for (MidiDeviceInfo.PortInfo p : infos[0].getPorts()) {
            if (p.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                portInfo = p;
                break;
            }
        }

        if (portInfo == null) {
            Log.e("MidiController", "No input port found");
            initializing = false;
            return;
        }

        final MidiDeviceInfo.PortInfo pi = portInfo;

        midiManager.openDevice(infos[0], device -> {
            if (device != null) {
                midiDevice = device;
                midiInputPort = device.openInputPort(pi.getPortNumber());
                Log.i("MidiController", "MIDI initialized OK");
            } else {
                Log.e("MidiController", "Failed to open MIDI device");
            }
            initializing = false;
        }, new Handler(Looper.getMainLooper()));
    }

    public MidiInputPort getPort() {
        return midiInputPort;
    }

    public void close() {
        try {
            if (midiInputPort != null) {
                midiInputPort.close();
                midiInputPort = null;
            }
            if (midiDevice != null) {
                midiDevice.close();
                midiDevice = null;
            }
        } catch (IOException e) {
            Log.e("MidiController", "Error closing MIDI", e);
        }
    }
}
