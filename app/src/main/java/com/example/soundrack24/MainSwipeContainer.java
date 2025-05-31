package com.example.soundrack24;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class MainSwipeContainer extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ViewPager2 viewPager;
    private Handler hideHandler = new Handler(Looper.getMainLooper());
    private Runnable fadeRunnable, hideRunnable;

    private String mParam1;
    private String mParam2;

    public MainSwipeContainer() {}

    public static MainSwipeContainer newInstance(String param1, String param2) {
        MainSwipeContainer fragment = new MainSwipeContainer();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_swipe_container, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        TextView statusText = view.findViewById(R.id.statusText);
        View statusBar = view.findViewById(R.id.bottomStatusBar);

        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new StyleLayout() : new MainLayout();
            }
        };

        viewPager.setAdapter(adapter);

        // Page swipe logic
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update text
                statusText.setText(position == 0 ? "Styles" : "Keyboard Expression Sets");

                // Cancel current animations
                statusBar.animate().cancel();

                // Reset alpha and visibility
                statusBar.setAlpha(1f);
                statusBar.setVisibility(View.VISIBLE);

                // Clear previous callbacks
                hideHandler.removeCallbacks(fadeRunnable);
                hideHandler.removeCallbacks(hideRunnable);

                // Define fresh runnables
                fadeRunnable = () -> statusBar.animate().alpha(0f).setDuration(4000).start();
                hideRunnable = () -> statusBar.setVisibility(View.GONE);

                // Start timers again
                hideHandler.postDelayed(fadeRunnable, 1000);
                hideHandler.postDelayed(hideRunnable, 5000);

            }
        });

        return view;
    }
}