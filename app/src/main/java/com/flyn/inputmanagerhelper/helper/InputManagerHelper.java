package com.flyn.inputmanagerhelper.helper;


import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flyn.inputmanagerhelper.R;
import com.flyn.inputmanagerhelper.view.KeyboardListenLayout;

public class InputManagerHelper {


    private Activity activity;
    private int lastKeyBoardHeight;

    private InputManagerHelper(Activity activity) {
        this.activity = activity;
    }

    public static InputManagerHelper attachToActivity(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return new InputManagerHelper(activity);
    }

    public void bindCustomLayout(final KeyboardListenLayout keyboardListenLayout, final View lastVisibleView) {
        keyboardListenLayout.setOnSizeChangedListener(new KeyboardListenLayout.onSizeChangedListener() {
            @Override
            public void onChanged(final boolean showKeyboard, final int h, final int oldh) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (showKeyboard) {
                            //oldh代表输入法未弹出前最外层布局高度，h代表当前最外层布局高度，oldh-h可以计算出布局大小改变后输入法的高度
                            //oldh-输入法高度即为键盘最顶端处在布局中的位置，其实直接用h计算就可以，代码这么写便于理解
                            int keyboardTop = oldh - (oldh - h);
                            int[] location = new int[2];
                            lastVisibleView.getLocationOnScreen(location);
                            //登录按钮顶部在屏幕中的位置+登录按钮的高度=登录按钮底部在屏幕中的位置
                            int lastVisibleViewBottom = location[1] + lastVisibleView.getHeight();
                            //登录按钮底部在布局中的位置-输入法顶部的位置=需要将布局弹起多少高度
                            int reSizeLayoutHeight = lastVisibleViewBottom - keyboardTop;
                            //因为keyboardListenLayout的高度不包括外层的statusbar的高度和actionbar的高度
                            //所以需要减去status bar的高度
                            reSizeLayoutHeight -= getStatusBarHeight();
                            //如果界面里有actionbar并且处于显示状态则需要少减去actionbar的高度
                            if (null != (((AppCompatActivity) activity).getSupportActionBar()) && (((AppCompatActivity) activity).getSupportActionBar()).isShowing()) {
                                reSizeLayoutHeight -= getActionBarHeight();
                            }
                            //设置登录按钮与输入法之间8dp间距
                            reSizeLayoutHeight += getPxFromDp(8);
                            keyboardListenLayout.setPadding(0, -reSizeLayoutHeight, 0, 0);
                        } else {
                            //还原布局
                            keyboardListenLayout.setPadding(0, 0, 0, 0);
                        }
                    }
                }, 10);
            }
        });
    }

    public void bindLayout(final ViewGroup viewGroup, final View lastVisibleView) {
        viewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adjustLayout(viewGroup, lastVisibleView);
                    }
                }, 10);
            }
        });
    }

    private void adjustLayout(ViewGroup viewGroup, View lastVisibleView) {
        //获得屏幕高度
        int screenHeight = viewGroup.getRootView().getHeight();
        //r.bottom - r.top计算出输入法弹起后viewGroup的高度，屏幕高度-viewGroup高度即为键盘高度
        Rect r = new Rect();
        viewGroup.getWindowVisibleDisplayFrame(r);
        int keyboardHeight = screenHeight - (r.bottom - r.top);
        //当设置layout_keyboard设置完padding以后会重绘布局再次执行onGlobalLayout()
        //所以判断如果键盘高度未改变就不执行下去
        if (keyboardHeight == this.lastKeyBoardHeight) {
            return;
        }
        this.lastKeyBoardHeight = keyboardHeight;
        if (keyboardHeight < 300) {
            //键盘关闭后恢复布局
            viewGroup.setPadding(0, 0, 0, 0);
        } else {
            //计算出键盘最顶端在布局中的位置
            int keyboardTop = screenHeight - keyboardHeight;
            int[] location = new int[2];
            lastVisibleView.getLocationOnScreen(location);
            //获取登录按钮底部在屏幕中的位置
            int lastVisibleViewBottom = location[1] + lastVisibleView.getHeight();
            //登录按钮底部在布局中的位置-输入法顶部的位置=需要将布局弹起多少高度
            int reSizeLayoutHeight = lastVisibleViewBottom - keyboardTop;
            //需要多弹起一个StatusBar的高度
            reSizeLayoutHeight -= getStatusBarHeight();
            //设置登录按钮与输入法之间存有间距
            reSizeLayoutHeight += getPxFromDp(8);
            viewGroup.setPadding(0, -reSizeLayoutHeight, 0, 0);
        }
    }

    public void bindScrollView(final ScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scroll(scrollView);
                    }
                }, 10);
            }
        });
    }

    private void scroll(final ScrollView scrollView) {
        //获得屏幕高度
        int screenHeight = scrollView.getRootView().getHeight();
        //r.bottom - r.top计算出输入法弹起后viewGroup的高度，屏幕高度-viewGroup高度即为键盘高度
        Rect r = new Rect();
        scrollView.getWindowVisibleDisplayFrame(r);
        int keyboardHeight = screenHeight - (r.bottom - r.top);
        //当设置layout_keyboard设置完padding以后会重绘布局再次执行onGlobalLayout()
        //所以判断如果键盘高度未改变就不执行下去
        if (keyboardHeight == this.lastKeyBoardHeight) {
            return;
        }
        this.lastKeyBoardHeight = keyboardHeight;
        View view = activity.getWindow().getCurrentFocus();
        if (keyboardHeight > 300 && null != view) {
            if (view instanceof TextView) {
                //计算出键盘最顶端在布局中的位置
                int keyboardTop = screenHeight - keyboardHeight;
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                //获取登录按钮底部在屏幕中的位置
                int viewBottom = location[1] + view.getHeight();
                //比较输入框与键盘的位置关系，如果输入框在键盘之上的位置就不做处理
                if (viewBottom <= keyboardTop)
                    return;
                //需要滚动的距离即为文字底部到输入框底部的距离
                int height = (view.getHeight() - (int) ((TextView) view).getTextSize()) / 2;
                scrollView.smoothScrollBy(0, height);
            }
        }
    }

    private int getPxFromDp(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = activity.getResources().getDimensionPixelOffset(resId);
        }
        return result;
    }

    private int getActionBarHeight() {
        if (null != (((AppCompatActivity) activity).getSupportActionBar())) {
            //如果界面里有actionbar则需要多向上弹起一个actionbar的高度
            TypedValue typedValue = new TypedValue();
            if (activity.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
                return TypedValue.complexToDimensionPixelSize(typedValue.data, activity.getResources().getDisplayMetrics());
            }
        }
        return 0;
    }

    private boolean isTranslucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flag = activity.getWindow().getAttributes().flags;
            return (flag & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        return false;
    }
}
