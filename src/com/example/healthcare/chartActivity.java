package com.example.healthcare;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.Menu;

public class chartActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);
		chartView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void chartView() {
		// 同样是需要数据dataset和视图渲染器renderer
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		// 设置图表的X轴的当前方向
		mRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setXTitle(getResources().getString(R.string.time));// 设置为X轴的标题
		// mRenderer.setYTitle("数值");// 设置y轴的标题
		mRenderer.setAxisTitleTextSize(40);// 设置轴标题文本大小
		mRenderer.setChartTitle(getResources().getString(R.string.chart));// 设置图表标题
		mRenderer.setChartTitleTextSize(60);// 设置图表标题文字的大小
		mRenderer.setLabelsTextSize(32);// 设置标签的文字大小
		mRenderer.setLegendHeight(100);
		mRenderer.setLegendTextSize(40);// 设置图例文本大小
		mRenderer.setPointSize(10f);// 设置点的大小
		mRenderer.setYAxisMin(0);// 设置y轴最小值是0
		mRenderer.setYAxisMax(180);
		mRenderer.setYLabels(9);// 设置Y轴刻度个数
		mRenderer.setXAxisMax(12);
		mRenderer.setShowGrid(true);// 显示网格
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setZoomEnabled(false, false);
		mRenderer.setXLabels(0);// 设置只显示如1月，2月等替换后的东西，不显示1,2,3等
		mRenderer.setMargins(new int[] { 100, 70, 60, 0 });// 设置视图位置
		mRenderer.setPanLimits(new double[] { -1, 31, 0, 100 });
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setAxesColor(Color.BLACK);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);

		XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
		int count = 0;
		for (int i = 0; i < 30; i++) {
			if (recordActivity.mDateTime[i] != null) {
				count++;
			}
		}

		XYSeries series = new XYSeries(getResources().getString(R.string.SBP));
		for (int i = 0; i < count; i++) {
			if (recordActivity.mSBP[count - i - 1] != 0) {
				series.add(i, recordActivity.mSBP[count - i - 1]);
			}
		}
		mDataset.addSeries(series);

		XYSeries seriesTwo = new XYSeries(getResources().getString(R.string.DBP));
		for (int i = 0; i < count; i++) {
			if (recordActivity.mDBP[count - i - 1] != 0) {
				seriesTwo.add(i, recordActivity.mDBP[count - i - 1]);
			}
		}
		mDataset.addSeries(seriesTwo);

		XYSeries seriesThree = new XYSeries(getResources().getString(R.string.HR));
		for (int i = 0; i < count; i++) {
			if (recordActivity.mHR[count - i - 1] != 0) {
				seriesThree.add(i, recordActivity.mHR[count - i - 1]);
			}
		}
		mDataset.addSeries(seriesThree);
		// 将x标签栏目显示如：1,2,3,4替换为显示1月，2月，3月，4月
		for (int i = 0; i < count; i++) {
			if (recordActivity.mDateTime[count - i - 1] != null) {
				mRenderer.addXTextLabel(i, recordActivity.mDateTime[count - i - 1].substring(5, 9));
			}
		}

		XYSeriesRenderer r = new XYSeriesRenderer();// (类似于一条线对象)
		r.setColor(Color.RED);// 设置颜色
		// r.setPointStyle(PointStyle.POINT);// 设置点的样式
		// r.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		r.setDisplayChartValues(true);// 将点的值显示出来
		r.setChartValuesSpacing(10);// 显示的点的值与图的距离
		r.setChartValuesTextSize(30);// 点的值的文字大小
		r.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(r);

		XYSeriesRenderer rTwo = new XYSeriesRenderer();// (类似于一条线对象)
		rTwo.setColor(Color.BLUE);// 设置颜色
		rTwo.setDisplayChartValues(true);// 将点的值显示出来
		rTwo.setChartValuesSpacing(10);// 显示的点的值与图的距离
		rTwo.setChartValuesTextSize(35);// 点的值的文字大小
		rTwo.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(rTwo);

		XYSeriesRenderer rThree = new XYSeriesRenderer();// (类似于一条线对象)
		rThree.setColor(Color.GREEN);// 设置颜色
		rThree.setDisplayChartValues(true);// 将点的值显示出来
		rThree.setChartValuesSpacing(10);// 显示的点的值与图的距离
		rThree.setChartValuesTextSize(35);// 点的值的文字大小
		rThree.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(rThree);

		GraphicalView view = ChartFactory.getLineChartView(this, mDataset, mRenderer);
		view.setBackgroundColor(Color.WHITE);
		setContentView(view);
	}

}
