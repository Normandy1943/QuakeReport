/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    /** USGS 数据集中地震数据的 URL */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=6&limit=10";
    /** 地震列表的适配器 */
    private EarthquakeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // 创建伪地震列表
        /*ArrayList<Earthquake> earthquakes = new ArrayList<>();
        earthquakes.add(new Earthquake("7.2", "San Francisco", "Feb 2, 2016"));
        earthquakes.add(new Earthquake("6.1", "London", "July 20, 2015"));
        earthquakes.add(new Earthquake("3.9", "Tokyo", "Nov 10, 2014"));
        earthquakes.add(new Earthquake("5.4", "Mexico City", "May 3, 2014"));
        earthquakes.add(new Earthquake("2.8", "Moscow", "Jan 31, 2013"));
        earthquakes.add(new Earthquake("4.9", "Rio de Janeiro", "Aug 19, 2012"));
        earthquakes.add(new Earthquake("1.6", "Paris", "Oct 30, 2011"));*/

        // 在布局中查找 {@link ListView} 的引用
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // 创建新适配器，将空地震列表作为输入
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // 在 {@link ListView} 上设置适配器
        // 以便可以在用户界面中填充列表
        earthquakeListView.setAdapter(mAdapter);

        // 在 ListView 上设置项目单击监听器，该监听器会向 Web 浏览器发送 intent，
        // 打开包含有关所选地震详细信息的网站。
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // 查找单击的当前地震
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // 将字符串 URL 转换成 URI 对象（传递到 Intent 构造函数中）
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // 创建新 intent 以查看地震 URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // 发送 intent 以启动新活动
                startActivity(websiteIntent);
            }
        });

        // 启动 AsyncTask 以获取地震数据
        EarthquakeAsyncTask earthquakeAsyncTask = new EarthquakeAsyncTask();
        earthquakeAsyncTask.execute(USGS_REQUEST_URL);

    }



    /**
     * {@link AsyncTask} 用于在后台线程上执行网络请求，然后
     * 使用响应中的地震列表更新 UI。
     *
     * AsyncTask 有三个泛型参数：输入类型、用于进度更新的类型和
     * 输出类型。我们的任务将获取字符串 URL 并返回地震。我们不会执行
     * 进度更新，因此第二个泛型是无效的。
     *
     * 我们将仅覆盖 AsyncTask 的两个方法：doInBackground() 和 onPostExecute()。
     * doInBackground() 方法会在后台线程上运行，因此可以运行长时间运行的代码
     * （如网络活动），而不会干扰应用的响应性。
     * onPostExecute() 在 UI 线程上运行，系统会将 doInBackground() 方法的结果传递给它，
     * 因此该方法可使用生成的数据更新 UI。
     */
    private class EarthquakeAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {
        /**
         * 此方法在后台线程上运行并执行网络请求。
         * 我们不能够通过后台线程更新 UI，因此我们返回
         * {@link Earthquake} 的列表作为结果。
         */
        @Override
        protected List<Earthquake> doInBackground(String... strings) {

            // 如果不存在任何 URL 或第一个 URL 为空，切勿执行请求。
            if (strings.length < 1 || strings[0] == null) {
                return null;
            }
            return QueryUtils.fetchEarthquakeData(strings[0]);
        }

        /**
         * 后台工作完成后，此方法会在主 UI 线程上
         * 运行。此方法接收 doInBackground() 方法的返回值
         * 作为输入。首先，我们将清理适配器，除去先前 USGS 查询的地震
         * 数据。然后，我们使用新地震列表更新适配器，
         * 这将触发 ListView 重新填充其列表项。
         */
        @Override
        protected void onPostExecute(List<Earthquake> earthquakes) {
            // 如果不存在任何结果，则不执行任何操作。
            if (earthquakes == null) {
                return;
            }
            // 清除之前地震数据的适配器
            mAdapter.clear();

            // 如果存在 {@link Earthquake} 的有效列表，则将其添加到适配器的
            // 数据集。这将触发 ListView 执行更新。
            if (earthquakes != null && !earthquakes.isEmpty()) {
                mAdapter.addAll(earthquakes);
            }
        }
    }
}
