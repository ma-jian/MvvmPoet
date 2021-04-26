package com.mm.mvvmpoet;

import android.app.Dialog;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.mm.lib_http.RetrofitGlobal;
import com.mm.lib_util.DialogQueue;
import com.mm.lib_util.FitDisplayMetrics;
import com.mm.lib_util.etoast.ToastCompat;
import com.mm.lib_util.etoast.ToastGlobal;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by : majian
 * Date : 4/25/21
 * Describe :
 */

public class JActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        EditText editName = findViewById(R.id.editName);
        TextView textView = findViewById(R.id.textView);
        findViewById(R.id.retrofit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<Object> call = RetrofitGlobal.Companion.create(DemoService.class).getUser(editName.getText().toString());
                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        textView.setText(response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        textView.setText(Log.getStackTraceString(t));
                    }
                });
            }
        });

        findViewById(R.id.toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastGlobal.Companion.show("我是java -> Toast");
                ToastGlobal.Companion.showByQueue("我是java -> QueueToast");
                ToastGlobal.Companion.showByQueue("我是java -> QueueToast = ");
                ToastGlobal.Companion.showByQueue("我是java -> QueueToast -> 自定义属性", new Function1<ToastCompat.Builder, Unit>() {
                    @Override
                    public Unit invoke(ToastCompat.Builder builder) {
                        builder.duration(1000);
                        builder.setGravity(Gravity.BOTTOM);
                        return null;
                    }
                });
            }
        });

        findViewById(R.id.dialogQueue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });
    }

    private void createDialog() {
        ContextWrapper context = FitDisplayMetrics.Companion.restDisplayMetrics(this, 0);
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        float density = context.getResources().getDisplayMetrics().density;
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        String disply = "density:" + density + "\ndensityDpi:" + densityDpi + "\nscaledDensity" + scaledDensity;
        Dialog dialog = new AlertDialog.Builder(context).setTitle("我是dialog1").setMessage(disply).create();
        DialogQueue.Companion.addDialog(dialog, 2, new Function1<DialogQueue.Node, Unit>() {
            @Override
            public Unit invoke(DialogQueue.Node node) {
                node.setDelay(1000);
                return null;
            }
        });

        Dialog dialog2 = new AlertDialog.Builder(context).setTitle("我是dialog2").setMessage(disply).create();
        new DialogQueue.Builder().addDialog(dialog2, 2).build();

        DialogFragment dialog4 = new DialogFragment(R.layout.custom_toast_view_success);

        DialogQueue.Companion.addDialog(dialog4, 1, new Function1<DialogQueue.Node, Unit>() {
            @Override
            public Unit invoke(DialogQueue.Node node) {
                node.setDelay(500);
                return null;
            }
        });

        DialogFragment dialog5 = new DialogFragment(R.layout.custom_toast_view_success);
        new DialogQueue.Builder().addDialog(dialog5, 2).delay(2000).build();
    }
}
