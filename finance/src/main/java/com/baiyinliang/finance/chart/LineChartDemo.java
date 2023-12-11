package com.baiyinliang.finance.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class LineChartDemo {
    public static JFreeChart createChart() {
        // 创建一个数据集
        XYSeriesCollection dataset = new XYSeriesCollection();

        // 创建一个数据系列
        XYSeries series = new XYSeries("折线图示例");

        // 添加数据
        series.add(1, 1);
        series.add(2, 3);
        series.add(3, 2);
        series.add(4, 5);
        series.add(5, 4);

        // 将数据系列添加到数据集中
        dataset.addSeries(series);

        // 创建折线图
        JFreeChart chart = ChartFactory.createXYLineChart(
                "折线图示例", // 图表标题
                "X", // X轴标签
                "Y", // Y轴标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // 图表方向
                true, // 是否显示图例
                true, // 是否生成工具提示
                false // 是否生成URL链接
        );

        return chart;
    }


    public static void main(String[] args) {
        // 创建一个折线图
        JFreeChart chart = createChart();

        // 创建一个图形窗口
        ChartFrame frame = new ChartFrame("折线图示例", chart);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


        // 保存折线图到文件
        /*try {
            ChartUtilities.saveChartAsPNG(new File("linechart.png"), chart, 500, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
