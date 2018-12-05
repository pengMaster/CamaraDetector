package king.bird.camaradetector.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import king.bird.camaradetector.MyApplication;
import king.bird.camaradetector.R;
import king.bird.camaradetector.base.BaseAppAdapter;
import king.bird.camaradetector.base.BaseHolderL;

/**
 * <pre>
 *     author : Wp
 *     e-mail : 18141924293@163.com
 *     time   : 2018/12/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecordAdapter extends BaseAppAdapter {

    public RecordAdapter(AbsListView listView, List datas) {
        super(listView, datas);
    }

    @Override
    protected BaseHolderL getHolder() {
        return new Holder();
    }

    class Holder extends BaseHolderL {

        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_desc)
        TextView tvDesc;
        @BindView(R.id.tv_count)
        TextView tv_count;
        @BindView(R.id.rl_layout)
        RelativeLayout rl_layout;

        @Override
        protected View initView() {
            return LayoutInflater.from(MyApplication.Companion.getContext()).inflate(R.layout.item_record, null);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void refreshView() {
            int data = (int)getData();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format=new SimpleDateFormat("MM-dd HH:mm:ss");
            tvTime.setText(format.format(new Date()));
            tv_count.setText("第 "+data+" 次");
        }
    }
}
