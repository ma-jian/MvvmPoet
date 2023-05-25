package com.mm.common.view.statusView;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mm.common.R;

/**
 * 多状态view
 * // 作用于 Activity 根布局 View
 * statusView = StatusView.init(Activity activity);
 * // 作用于 Activity 布局文件中指定的 View
 * statusView = StatusView.init(Activity activity, @IdRes int viewId);
 * // 作用于 Fragment 布局文件中指定的 View 在 onCreateView()之后使用
 * statusView = StatusView.init(Fragment fragment, @IdRes int viewId);
 * <p>
 * https://github.com/shehuan/StatusView
 */
public class StatusView extends FrameLayout {
    private final Context context;

    // 当前显示的 View
    private View currentView;
    // 原始内容 View
    private View contentView;

    // 状态布局文件 Id 声明
    private @LayoutRes
    int loadingLayoutId = R.layout.sv_loading_layout;
    private @LayoutRes
    int emptyLayoutId = R.layout.sv_empty_layout;
    private @LayoutRes
    int errorLayoutId = R.layout.sv_error_layout;
    private CharSequence text_error = "";
    private CharSequence text_empty = "";
    private CharSequence text_retry = "";
    private int error_drawable = 0;
    private int empty_drawable = 0;
    private String empty_lottie_fileName = "";
    private String error_lottie_fileName = "";
    private String loading_lottie_fileName = "";

    // 状态布局 View 缓存集合
    private final SparseArray<View> viewArray = new SparseArray<>();
    // 状态布局 View 显示时的回调接口集合
    private final SparseArray<StatusViewConvertListener> listenerArray = new SparseArray<>();
    // 索引对应的状态布局 Id 集合
    private final SparseIntArray layoutIdArray = new SparseIntArray();

    // 默认状态布局文件属性配置
    private StatusViewBuilder builder;

    public StatusView(@NonNull Context context) {
        this(context, null);
    }

    public StatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StatusView, 0, 0);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.StatusView_sv_loading_view) {
                loadingLayoutId = ta.getResourceId(attr, loadingLayoutId);
            } else if (attr == R.styleable.StatusView_sv_empty_view) {
                emptyLayoutId = ta.getResourceId(attr, emptyLayoutId);
            } else if (attr == R.styleable.StatusView_sv_error_view) {
                errorLayoutId = ta.getResourceId(attr, errorLayoutId);
            } else if (attr == R.styleable.StatusView_sv_empty_text) {
                text_empty = ta.getText(attr);
            } else if (attr == R.styleable.StatusView_sv_error_text) {
                text_error = ta.getText(attr);
            } else if (attr == R.styleable.StatusView_sv_retry_text) {
                text_retry = ta.getText(attr);
            } else if (attr == R.styleable.StatusView_sv_empty_drawable) {
                empty_drawable = ta.getResourceId(attr, empty_drawable);
            } else if (attr == R.styleable.StatusView_sv_error_drawable) {
                error_drawable = ta.getResourceId(attr, error_drawable);
            } else if (attr == R.styleable.StatusView_sv_empty_lottie) {
                empty_lottie_fileName = ta.getString(R.styleable.StatusView_sv_empty_lottie);
            } else if (attr == R.styleable.StatusView_sv_error_lottie) {
                error_lottie_fileName = ta.getString(R.styleable.StatusView_sv_error_lottie);
            } else if (attr == R.styleable.StatusView_sv_loading_lottie) {
                loading_lottie_fileName = ta.getString(R.styleable.StatusView_sv_loading_lottie);
            }
        }
        ta.recycle();
    }

    /**
     * 在 XML 中使用时，布局文件加载完后获得 StatusView 对应的子 ContentView
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() == 1) {
            View view = getChildAt(0);
            setContentView(view);
        }
    }

    /**
     * 在 Activity 中的初始化方法，默认页面的根布局使用多状态布局
     *
     * @param activity
     * @return
     */
    public static StatusView init(Activity activity) {
        View contentView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        return init(contentView);
    }

    /**
     * 在 Activity 中的初始化方法
     *
     * @param activity
     * @param viewId   使用多状态布局的 ViewId
     * @return
     */
    public static StatusView init(Activity activity, @IdRes int viewId) {
        View rootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        View contentView = rootView.findViewById(viewId);
        return init(contentView);
    }

    /**
     * 在Fragment中的初始化方法
     *
     * @param fragment
     * @param viewId   使用多状态布局的 ViewId
     * @return
     */
    public static StatusView init(Fragment fragment, @IdRes int viewId) {
        View rootView = fragment.getView();
        View contentView = null;
        if (rootView != null) {
            contentView = rootView.findViewById(viewId);
        }
        return init(contentView);
    }

    /**
     * 用 StatusView 替换要使用多状态布局的 View
     */
    private static StatusView init(View contentView) {
        if (contentView == null) {
            throw new RuntimeException("ContentView can not be null!");
        }
        ViewGroup parent = (ViewGroup) contentView.getParent();
        if (parent == null) {
            throw new RuntimeException("ContentView must have a parent view!");
        }
        ViewGroup.LayoutParams lp = contentView.getLayoutParams();
        int index = parent.indexOfChild(contentView);
        parent.removeView(contentView);
        StatusView statusView = new StatusView(contentView.getContext());
        statusView.addView(contentView);
        statusView.setContentView(contentView);
        parent.addView(statusView, index, lp);
        return statusView;
    }

    private void setContentView(View contentView) {
        this.contentView = currentView = contentView;
    }

    /**
     * 设置自定义 Loading 布局文件
     */
    public void setLoadingView(@LayoutRes int loadingLayoutRes) {
        this.loadingLayoutId = loadingLayoutRes;
    }

    /**
     * 设置自定义 Empty 布局文件
     */
    public void setEmptyView(@LayoutRes int emptyLayoutRes) {
        this.emptyLayoutId = emptyLayoutRes;
    }

    /**
     * 设置自定义 Error 布局文件
     */
    public void setErrorView(@LayoutRes int errorLayoutRes) {
        this.errorLayoutId = errorLayoutRes;
    }

    /**
     * 显示 原始内容 布局
     */
    public void showContentView() {
        switchStatusView(contentView);
    }

    /**
     * 显示 Loading 布局
     */
    public void showLoadingView() {
        switchStatusView(loadingLayoutId);
    }

    /**
     * 显示 Empty 布局
     */
    public void showEmptyView() {
        switchStatusView(emptyLayoutId);
    }

    /**
     * 显示 Error 布局
     */
    public void showErrorView() {
        switchStatusView(errorLayoutId);
    }

    /**
     * 设置 Loading 布局首次显示时的回调，可在回调中更新布局、绑定事件等
     */
    public void setOnLoadingViewConvertListener(StatusViewConvertListener listener) {
        listenerArray.put(loadingLayoutId, listener);
    }

    /**
     * 设置 Empty 布局首次显示时的回调，可在回调中更新布局、绑定事件等
     */
    public void setOnEmptyViewConvertListener(StatusViewConvertListener listener) {
        listenerArray.put(emptyLayoutId, listener);
    }

    /**
     * 设置 Error 布局首次显示时的回调，可在回调中更新布局、绑定事件等
     */
    public void setOnErrorViewConvertListener(StatusViewConvertListener listener) {
        listenerArray.put(errorLayoutId, listener);
    }

    /**
     * 设置索引对应的状态布局
     *
     * @param index    布局索引
     * @param layoutId 布局 Id
     */
    public void setStatusView(int index, @LayoutRes int layoutId) {
        layoutIdArray.put(index, layoutId);
    }

    /**
     * 显示指定索引对应的状态布局
     *
     * @param index 布局索引
     */
    public void showStatusView(int index) {
        switchStatusView(layoutIdArray.get(index));
    }

    /**
     * 为指定索引对应的状态布局设置初次显示的监听事件，用来进行状态布局的相关初始化
     *
     * @param index    布局索引
     * @param listener
     */
    public void setOnStatusViewConvertListener(int index, StatusViewConvertListener listener) {
        listenerArray.put(layoutIdArray.get(index), listener);
    }


    public void setOnEmptyRetryClickListener(OnClickListener emptyRetryClickListener) {
        if (builder == null) {
            builder = new StatusViewBuilder.Builder(getContext()).defaultBuild();
        }
        builder.setEmptyRetryClickListener(emptyRetryClickListener);
    }

    public void setOnErrorRetryClickListener(OnClickListener errorRetryClickListener) {
        if (builder == null) {
            builder = new StatusViewBuilder.Builder(getContext()).defaultBuild();
        }
        builder.setErrorRetryClickListener(errorRetryClickListener);
    }

    /**
     * 设置默认状态布局相关控件属性
     *
     * @param builder
     */
    public void config(StatusViewBuilder builder) {
        this.builder = builder;
    }

    private void configStatusView(@LayoutRes int layoutId, View statusView) {
        StatusViewHolder viewHolder;
        StatusViewConvertListener listener = listenerArray.get(layoutId);

        viewHolder = StatusViewHolder.create(statusView);
        updateStatusView(layoutId, viewHolder);

        // 设置状态布局首次显示的监听接口
        if (listener != null) {
            listener.onConvert(viewHolder);
        }
    }

    private void switchStatusView(View statusView) {
        if (statusView == currentView) {
            return;
        }
        removeView(currentView);
        currentView = statusView;
        addView(currentView);
    }

    private void switchStatusView(@LayoutRes int layoutId) {
        View statusView = generateStatusView(layoutId);
        switchStatusView(statusView);
    }

    /**
     * 根据布局文件 Id 得到对应的 View，并设置控件属性、绑定接口
     */
    private View generateStatusView(@LayoutRes int layoutId) {
        View statusView = viewArray.get(layoutId);
        if (statusView == null) {
            statusView = inflate(layoutId);
            viewArray.put(layoutId, statusView);
            configStatusView(layoutId, statusView);
        }
        return statusView;
    }

    /**
     * 配置状态布局相关控件
     *
     * @param layoutId
     * @param viewHolder
     */
    private void updateStatusView(@LayoutRes int layoutId, StatusViewHolder viewHolder) {
        if (builder == null) {
            builder = new StatusViewBuilder.Builder(getContext()).defaultBuild();
        }

        if (layoutId == R.layout.sv_loading_layout) {
            setTip(R.id.sv_loading_tip, builder.getLoadingTip(), viewHolder);
            setTipColor(R.id.sv_loading_tip, viewHolder);
            setTipSize(R.id.sv_loading_tip, viewHolder);
            setIcon(R.id.sv_load_icon, 0, loading_lottie_fileName, viewHolder);
        } else if (layoutId == R.layout.sv_empty_layout) {
            setTip(R.id.sv_empty_tip, !TextUtils.isEmpty(text_empty) ? text_empty : builder.getEmptyTip(), viewHolder);
            setTipColor(R.id.sv_empty_tip, viewHolder);
            setTipSize(R.id.sv_empty_tip, viewHolder);
            setIcon(R.id.sv_empty_icon, empty_drawable > 0 ? empty_drawable : builder.getEmptyIcon(), empty_lottie_fileName, viewHolder);

            setRetry(R.id.sv_empty_retry, builder.isShowEmptyRetry(), !TextUtils.isEmpty(text_retry) ? text_retry : builder.getEmptyRetryText(),
                    builder.getEmptyRetryClickListener(), viewHolder);

        } else if (layoutId == R.layout.sv_error_layout) {
            setTip(R.id.sv_error_tip, !TextUtils.isEmpty(text_error) ? text_error : builder.getErrorTip(), viewHolder);
            setTipColor(R.id.sv_error_tip, viewHolder);
            setTipSize(R.id.sv_error_tip, viewHolder);
            setIcon(R.id.sv_error_icon, error_drawable > 0 ? error_drawable : builder.getErrorIcon(), error_lottie_fileName, viewHolder);

            setRetry(R.id.sv_error_retry, builder.isShowErrorRetry(), !TextUtils.isEmpty(text_retry) ? text_retry : builder.getErrorRetryText(),
                    builder.getErrorRetryClickListener(), viewHolder);
        }
    }

    private void setTip(int viewId, CharSequence tip, StatusViewHolder viewHolder) {
        if (!TextUtils.isEmpty(tip))
            viewHolder.setText(viewId, tip);
    }

    private void setTipColor(int viewId, StatusViewHolder viewHolder) {
        if (builder.getTipColor() > 0)
            viewHolder.setTextColor(viewId, getResources().getColor(builder.getTipColor()));
    }

    private void setTipSize(int viewId, StatusViewHolder viewHolder) {
        if (builder.getTipSize() > 0)
            viewHolder.setTextSize(viewId, builder.getTipSize());
    }

    private void setIcon(int viewId, int iconId, String lottie, StatusViewHolder viewHolder) {
        if (!TextUtils.isEmpty(lottie)) {
            viewHolder.setImageLottie(viewId, lottie);
        } else if (iconId > 0) {
            viewHolder.setImageResource(viewId, iconId);
        }
    }

    private void setRetry(int viewId, boolean isShowRetry, CharSequence retryText,
                          OnClickListener listener, StatusViewHolder viewHolder) {
        if (isShowRetry) {
            viewHolder.getView(viewId).setVisibility(VISIBLE);
            if (!TextUtils.isEmpty(retryText))
                viewHolder.setText(viewId, retryText);

            if (listener != null) {
                viewHolder.setOnClickListener(viewId, listener);
            } else {
                viewHolder.getView(viewId).setVisibility(GONE);
            }

            if (builder.getRetryDrawable() > 0)
                viewHolder.setBackgroundDrawable(viewId, ContextCompat.getDrawable(getContext(), builder.getRetryDrawable()));

            if (builder.getRetryColor() > 0)
                viewHolder.setTextColor(viewId, builder.getRetryColor());

            if (builder.getRetrySize() > 0)
                viewHolder.setTextSize(viewId, builder.getRetrySize());
        } else {
            viewHolder.getView(viewId).setVisibility(GONE);
        }
    }

    private View inflate(int layoutId) {
        return LayoutInflater.from(context).inflate(layoutId, null);
    }
}