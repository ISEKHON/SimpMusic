package com.myjavaapp.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivityNew extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton searchFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        searchFab = findViewById(R.id.searchFab);

        // Setup ViewPager with adapter
        FeedPagerAdapter adapter = new FeedPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("🏠 Home");
                    break;
                case 1:
                    tab.setText("🔥 Top Tracks");
                    break;
                case 2:
                    tab.setText("🆕 New Singles");
                    break;
            }
        }).attach();

        // Setup FAB
        searchFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private static class FeedPagerAdapter extends FragmentStateAdapter {

        public FeedPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    // Use section-based layout for home feed
                    return FeedFragmentSections.newInstance();
                case 1:
                    return FeedFragment.newInstance(FeedFragment.FeedType.TOP_TRACKS);
                case 2:
                    return FeedFragment.newInstance(FeedFragment.FeedType.NEW_SINGLES);
                default:
                    return FeedFragmentSections.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

