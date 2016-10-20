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
		// ͬ������Ҫ����dataset����ͼ��Ⱦ��renderer
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		// ����ͼ���X��ĵ�ǰ����
		mRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setXTitle(getResources().getString(R.string.time));// ����ΪX��ı���
		// mRenderer.setYTitle("��ֵ");// ����y��ı���
		mRenderer.setAxisTitleTextSize(40);// ����������ı���С
		mRenderer.setChartTitle(getResources().getString(R.string.chart));// ����ͼ�����
		mRenderer.setChartTitleTextSize(60);// ����ͼ��������ֵĴ�С
		mRenderer.setLabelsTextSize(32);// ���ñ�ǩ�����ִ�С
		mRenderer.setLegendHeight(100);
		mRenderer.setLegendTextSize(40);// ����ͼ���ı���С
		mRenderer.setPointSize(10f);// ���õ�Ĵ�С
		mRenderer.setYAxisMin(0);// ����y����Сֵ��0
		mRenderer.setYAxisMax(180);
		mRenderer.setYLabels(9);// ����Y��̶ȸ���
		mRenderer.setXAxisMax(12);
		mRenderer.setShowGrid(true);// ��ʾ����
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setZoomEnabled(false, false);
		mRenderer.setXLabels(0);// ����ֻ��ʾ��1�£�2�µ��滻��Ķ���������ʾ1,2,3��
		mRenderer.setMargins(new int[] { 100, 70, 60, 0 });// ������ͼλ��
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
		// ��x��ǩ��Ŀ��ʾ�磺1,2,3,4�滻Ϊ��ʾ1�£�2�£�3�£�4��
		for (int i = 0; i < count; i++) {
			if (recordActivity.mDateTime[count - i - 1] != null) {
				mRenderer.addXTextLabel(i, recordActivity.mDateTime[count - i - 1].substring(5, 9));
			}
		}

		XYSeriesRenderer r = new XYSeriesRenderer();// (������һ���߶���)
		r.setColor(Color.RED);// ������ɫ
		// r.setPointStyle(PointStyle.POINT);// ���õ����ʽ
		// r.setFillPoints(true);// ���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�
		r.setDisplayChartValues(true);// �����ֵ��ʾ����
		r.setChartValuesSpacing(10);// ��ʾ�ĵ��ֵ��ͼ�ľ���
		r.setChartValuesTextSize(30);// ���ֵ�����ִ�С
		r.setLineWidth(3);// �����߿�
		mRenderer.addSeriesRenderer(r);

		XYSeriesRenderer rTwo = new XYSeriesRenderer();// (������һ���߶���)
		rTwo.setColor(Color.BLUE);// ������ɫ
		rTwo.setDisplayChartValues(true);// �����ֵ��ʾ����
		rTwo.setChartValuesSpacing(10);// ��ʾ�ĵ��ֵ��ͼ�ľ���
		rTwo.setChartValuesTextSize(35);// ���ֵ�����ִ�С
		rTwo.setLineWidth(3);// �����߿�
		mRenderer.addSeriesRenderer(rTwo);

		XYSeriesRenderer rThree = new XYSeriesRenderer();// (������һ���߶���)
		rThree.setColor(Color.GREEN);// ������ɫ
		rThree.setDisplayChartValues(true);// �����ֵ��ʾ����
		rThree.setChartValuesSpacing(10);// ��ʾ�ĵ��ֵ��ͼ�ľ���
		rThree.setChartValuesTextSize(35);// ���ֵ�����ִ�С
		rThree.setLineWidth(3);// �����߿�
		mRenderer.addSeriesRenderer(rThree);

		GraphicalView view = ChartFactory.getLineChartView(this, mDataset, mRenderer);
		view.setBackgroundColor(Color.WHITE);
		setContentView(view);
	}

}
