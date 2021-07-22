package com.example.flowlayoutdemo;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowlayoutdemo.flowlayout.ZFlowLayout;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ZFlowLayout mZl_search_history;
    private LayoutInflater inflater;
    private EditText mEdit_search;
    private ImageView mIv_clear;
    private int isDelete = -1;
    private String hawkConfig = "DemoSearch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //应该放在APPLICATION中，demo就省了
        Hawk.init(this).build();

        initView();
    }

    private void initView() {
        mEdit_search = findViewById(R.id.edit_search);
        mIv_clear = findViewById(R.id.iv_clear);
        mZl_search_history = findViewById(R.id.zl_search_history);

        mIv_clear.setOnClickListener(this);
        mEdit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mIv_clear.setVisibility(TextUtils.isEmpty(charSequence) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEdit_search.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideSoftKeyboard();
                saveSearchHistory(textView);
                notifyData();
                return true;
            }
            return false;
        });

        notifyData();
    }

    private void saveSearchHistory(TextView textView) {
        List<String> searchHistory;
        if (Hawk.contains(hawkConfig)) {
            searchHistory = Hawk.get(hawkConfig);
        } else {
            searchHistory = new ArrayList();
        }

        if (textView != null) {
            searchHistory.add(0, textView.getText().toString());
            Hawk.put(hawkConfig, searchHistory);
        }
    }

    private void notifyData() {
        if (Hawk.contains(hawkConfig)) {
            List<String> searchHistory = Hawk.get(hawkConfig);
            if (searchHistory != null && searchHistory.size() >= 0) {
                initZFlowLayout(searchHistory);
            }
        }
        isDelete = -1;
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0); //强制隐藏键盘
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<View> mViewList = new ArrayList<>();

    private void initZFlowLayout(List<String> searchHistory) {
        inflater = LayoutInflater.from(this);
        mViewList.clear();
        for (int i = 0; i < searchHistory.size(); i++) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_search_history, mZl_search_history, false);
            textView.setText(searchHistory.get(i));
            mViewList.add(textView);
        }
        mZl_search_history.setChildren(mViewList);

        mZl_search_history.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mZl_search_history.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int lineCount = mZl_search_history.getLineCount();
                int twoLineViewCount = mZl_search_history.getTwoLineViewCount();
                if (lineCount > 2) {
                    initImageView2(searchHistory, twoLineViewCount);
                }
            }
        });

        mZl_search_history.setOnTagClickListener((view, position) -> {
            //点击了
            if (position == isDelete) {
                searchHistory.remove(position);
                Hawk.put(hawkConfig, searchHistory);
                notifyData();
            } else {
                Toast.makeText(this, "选择了" + ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
            }
        });

        mZl_search_history.setOnLongClickListener((view, position) -> {
            ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.mipmap.clip_search_delete_item_new), null);
            isDelete = position;
        });

    }

    private void initImageView(List<String> searchHistory, final int twoLineViewCount) {
        mViewList.clear();
        for (int i = 0; i < searchHistory.size(); i++) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_search_history, mZl_search_history, false);
            textView.setText(searchHistory.get(i));
            textView.setText(searchHistory.get(i));
            mViewList.add(textView);
        }
        ImageView imageView = (ImageView) inflater.inflate(R.layout.item_search_history_img, mZl_search_history, false);
        imageView.setImageResource(R.mipmap.search_open);
        imageView.setOnClickListener(v -> initImageView2(searchHistory, twoLineViewCount));
        mViewList.add(imageView);
        mZl_search_history.setChildren(mViewList);
    }

    private void initImageView2(List<String> searchHistory, final int twoLineViewCount) {
        mViewList.clear();
        for (int i = 0; i < twoLineViewCount; i++) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_search_history, mZl_search_history, false);
            textView.setText(searchHistory.get(i));
            textView.setText(searchHistory.get(i));
            mViewList.add(textView);
        }
        ImageView imageView = (ImageView) inflater.inflate(R.layout.item_search_history_img, mZl_search_history, false);
        imageView.setImageResource(R.mipmap.search_close);
        imageView.setOnClickListener(v -> initImageView(searchHistory, twoLineViewCount));
        mViewList.add(imageView);
        mZl_search_history.setChildren(mViewList);
        mZl_search_history.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mZl_search_history.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int lineCount = mZl_search_history.getLineCount();
                int twoLineViewCount = mZl_search_history.getTwoLineViewCount();
                if (lineCount > 2) {
                    initImageView2(searchHistory, twoLineViewCount - 1);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_clear) {
            mEdit_search.setText("");
        }
    }
}
