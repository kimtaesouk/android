package com.example.newproject.Main;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.newproject.R;
import com.example.newproject.fragment.ChatFragment;
import com.example.newproject.fragment.HomeFragment;
import com.example.newproject.fragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main_Activity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    Fragment fragment_home, fragment_chat, fragment_setting;
    String pid;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String CURRENT_FRAGMENT_TAG = "current_fragment";
    private static final String SHARED_PREFS_NAME = "fragmentPrefs";
    private static final String LAST_FRAGMENT_TAG = "lastFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        System.out.println(pid + "00000");

        fragment_home = new HomeFragment();
        fragment_chat = new ChatFragment();
        fragment_setting = new SettingFragment();

        Bundle bundle = new Bundle();
        bundle.putString("pid", pid);
        fragment_home.setArguments(bundle);
        fragment_chat.setArguments(bundle);
        fragment_setting.setArguments(bundle);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String fragmentTag = null;

                switch (item.getItemId()) {
                    case R.id.home:
                        Log.i(TAG, "home 들어옴");
                        selectedFragment = fragment_home;
                        fragmentTag = "HOME_FRAGMENT";
                        break;
                    case R.id.chat:
                        Log.i(TAG, "chat 들어옴");
                        selectedFragment = fragment_chat;
                        fragmentTag = "CHAT_FRAGMENT";
                        break;
                    case R.id.profile:
                        Log.i(TAG, "profile 들어옴");
                        selectedFragment = fragment_setting;
                        fragmentTag = "PROFILE_FRAGMENT";
                        break;
                }

                if (selectedFragment != null && fragmentTag != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment, fragmentTag).commitNowAllowingStateLoss();
                    saveLastFragmentTag(fragmentTag);  // 현재 프래그먼트 태그 저장
                }

                return true;
            }
        });

        // 상태 복원
        if (savedInstanceState != null) {
            int selectedItemId = savedInstanceState.getInt(SELECTED_ITEM_ID);
            bottomNavigationView.setSelectedItemId(selectedItemId);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentByTag(savedInstanceState.getString(CURRENT_FRAGMENT_TAG));
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commitNowAllowingStateLoss();
            }
        } else {
            // SharedPreferences에서 마지막 프래그먼트 태그 가져오기
            String lastFragmentTag = getLastFragmentTag();
            if (lastFragmentTag != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment lastFragment = fragmentManager.findFragmentByTag(lastFragmentTag);
                if (lastFragment == null) {
                    switch (lastFragmentTag) {
                        case "HOME_FRAGMENT":
                            lastFragment = fragment_home;
                            bottomNavigationView.setSelectedItemId(R.id.home);
                            break;
                        case "CHAT_FRAGMENT":
                            lastFragment = fragment_chat;
                            bottomNavigationView.setSelectedItemId(R.id.chat);
                            break;
                        case "PROFILE_FRAGMENT":
                            lastFragment = fragment_setting;
                            bottomNavigationView.setSelectedItemId(R.id.profile);
                            break;
                    }
                }
                if (lastFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, lastFragment, lastFragmentTag).commitNowAllowingStateLoss();
                }
            } else {
                // 초기 화면 설정
                bottomNavigationView.setSelectedItemId(R.id.home);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment_home, "HOME_FRAGMENT").commitNowAllowingStateLoss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SharedPreferences에서 프래그먼트 열림 여부 확인
        SharedPreferences prefs = getSharedPreferences("fragmentPrefs", MODE_PRIVATE);
        boolean shouldOpenChatFragment = prefs.getBoolean("shouldOpenChatFragment", false);

        if (shouldOpenChatFragment) {
            // 프래그먼트를 열어야 할 때만
            openChatFragment();

            // 다시 프래그먼트를 열지 않도록 플래그 초기화
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("shouldOpenChatFragment", false);
            editor.apply();
        }
    }


    private void openChatFragment() {
        // 기존의 ChatFragment 인스턴스를 가져옴
        FragmentManager fragmentManager = getSupportFragmentManager();
        ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentByTag("CHAT_FRAGMENT");

        if (chatFragment == null) {
            // 기존 인스턴스가 없으면 새로 생성하고, Bundle로 pid 전달
            chatFragment = new ChatFragment();
            Bundle bundle = new Bundle();
            bundle.putString("pid", pid); // 전달할 pid 값을 Bundle에 추가
            chatFragment.setArguments(bundle);
        }

        // ChatFragment를 열기 위한 메서드
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment, "CHAT_FRAGMENT") // fragment_container에 ChatFragment를 교체하여 표시
                .commit();
        bottomNavigationView.setSelectedItemId(R.id.chat); // R.id.chat는 BottomNavigationView의 chat 탭 ID
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 새로 전달된 Intent를 저장

        // Intent에서 프래그먼트를 열어야 하는지 확인
        if (intent != null && intent.hasExtra("openFragment")) {
            String openFragment = intent.getStringExtra("openFragment");
            if ("ChatFragment".equals(openFragment)) {
//                openChatFragment(); // ChatFragment를 표시하는 메서드 호출
            }
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, bottomNavigationView.getSelectedItemId());
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            outState.putString(CURRENT_FRAGMENT_TAG, currentFragment.getTag());
        }
    }

    private void saveLastFragmentTag(String fragmentTag) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_FRAGMENT_TAG, fragmentTag);
        editor.apply();
    }

    private String getLastFragmentTag() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(LAST_FRAGMENT_TAG, null);
    }

    public void changeFragment(Fragment fragment, Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // 백스택에 추가하여 백 버튼으로 돌아갈 수 있게 함
        fragmentTransaction.commit();
    }
}
