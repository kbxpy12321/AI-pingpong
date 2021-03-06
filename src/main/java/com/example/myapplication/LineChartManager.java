package com.example.myapplication;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 叶明林 on 2017/8/7.
 */

public class LineChartManager {
    private LineChart lineChart;
    private List<String> dataName=new ArrayList<String>();
    private List<Integer> dataColor=new ArrayList<Integer>();
    private final int XRangeMaximum=100;
    private YAxis leftAxis;
    private YAxis rightAxis;
    private XAxis xAxis;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<ILineDataSet> lineDataSets = new ArrayList<ILineDataSet>();
    private boolean lineVisiable[]={true,true,true};

    //改变曲线颜色
    public void changeColor(int[] color)
    {
        if(color.length!=this.dataName.size())
            return ;
        this.dataColor.clear();
        for(int x:color)
            this.dataColor.add(x);
        initLineDataSet();
    }
    //改变曲线名称，但不改变曲线数量
    public void changeLineName(String names[])
    {
        if(dataColor.size()!=names.length)
            return ;
        this.dataName.clear();
        for(String x:names)
            this.dataName.add(x);
        initLineDataSet();
    }
    //非实时添加数据
    public void setLineData(List<Double> list)
    {
        lineData = new LineData(lineDataSets);
        lineChart.setData(this.lineData);
        for(int j=0;j<list.size();j++)
        {
            for (int i = 0; i < lineDataSets.size(); i++) {
                Entry entry = new Entry(lineDataSet.getEntryCount(),(float)((double)list.get(j*lineDataSets.size()+i)) );
                lineData.addEntry(entry, i);
            }
        }
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMaximum(XRangeMaximum);
        //lineChart.moveViewToX(lineData.getEntryCount() - 5);
    }
    //多条曲线
    public LineChartManager(LineChart mLineChart, String[] name, int[] color) {
        this.lineChart = mLineChart;
        for(int i=0;i<name.length;i++)
            this.dataName.add(name[i]);
        for(int i=0;i<color.length;i++)
            this.dataColor.add(color[i]);
        this.setDescription("");
        lineChart.getAxisLeft().setEnabled(false);
        xAxis = lineChart.getXAxis();
        xAxis.setLabelCount(50,true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        rightAxis=lineChart.getAxisRight();
        leftAxis=lineChart.getAxisLeft();
        rightAxis.setEnabled(false);
        //xAxis.setEnabled(false);
        initLineChart();
        initLineDataSet();
    }

    /**
     * 初始化LineChar
     */
    private void initLineChart() {

        lineChart.setDrawGridBackground(false);
        //显示边界
        lineChart.setDrawBorders(false);
        //折线图例 标签 设置
        lineChart.getLegend().setEnabled(false);        //隐藏注解
            /*Legend legend = lineChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(11f);
            //显示位置
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);*/

        //保证Y轴从0开始，不然会上移一点
        //leftAxis.setAxisMinimum(0f);
        //rightAxis.setAxisMinimum(0f);
    }

    private void initLineDataSet()
    {
        for (int i = 0; i < this.dataName.size(); i++) {
            lineDataSet = new LineDataSet(null, this.dataName.get(i));
            lineDataSet.setColor(this.dataColor.get(i));
            lineDataSet.setDrawCircles(false);
            lineDataSet.setColor(this.dataColor.get(i));
            lineDataSet.setDrawFilled(true);
            lineDataSet.setCircleColor(this.dataColor.get(i));
            lineDataSet.setHighLightColor(this.dataColor.get(i));
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setValueTextSize(10f);

            //TODO 设置线宽和填充色
            lineDataSet.setLineWidth(1f);
            lineDataSet.setFillColor(Color.parseColor("#EEC900"));
            lineDataSets.add(lineDataSet);
        }
        //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.setTouchEnabled(false);

        preAddData(250);
    }
    public void preAddData(int len)
    {
        if (lineDataSets.get(0).getEntryCount() == 0) {
            lineData = new LineData(lineDataSets);
        }
        lineChart.setData(this.lineData);
        for(int j=0;j<len;j++)
        {
            for (int i = 0; i < lineDataSets.size(); i++) {
                Entry entry = new Entry(lineDataSet.getEntryCount(),0 );
                lineData.addEntry(entry, i);
            }
        }
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMaximum(XRangeMaximum);
        lineChart.moveViewToX(lineData.getEntryCount() - 5);
    }
    /**
     * 动态添加数据（多条折线图）
     *
     * @param numbers
     */
    public void addEntry(List<Double> numbers) {
        if (lineDataSets.get(0).getEntryCount() == 0) {
            lineData = new LineData(lineDataSets);
        }
        lineChart.setData(this.lineData);
        for (int i = 0; i < numbers.size(); i++) {
            Entry entry = new Entry(lineDataSet.getEntryCount(), numbers.get(i).floatValue());
            lineData.addEntry(entry, i);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(XRangeMaximum);
            lineChart.moveViewToX(lineData.getEntryCount() - 5);
        }
    }
    public void chooseIndex(int index)
    {
        if(index>=0&&index<3)
        {
            this.lineVisiable[index]=!this.lineVisiable[index];
            lineDataSets.get(index).setVisible(this.lineVisiable[index]);
        }
    }
    /**
     * 设置Y轴值
     *
     * @param max
     * @param min
     * @param labelCount
     */
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        //leftAxis.setAxisMaximum(max);
        //leftAxis.setAxisMinimum(min);
        //leftAxis.setLabelCount(labelCount, false);

        rightAxis.setAxisMaximum(max);
        rightAxis.setAxisMinimum(min);
        rightAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }
    /**
     * 设置描述信息
     *
     * @param str
     */
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }
}
