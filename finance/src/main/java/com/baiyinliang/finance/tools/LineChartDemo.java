package com.baiyinliang.finance.tools;

import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Month;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class LineChartDemo extends ApplicationFrame {

    public LineChartDemo(String titile) {
        super(titile);
        CategoryDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        setContentPane(chartPanel);

    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(100, "精装", "20230301");
        dataset.addValue(101, "精装", "20230302");
        dataset.addValue(105, "精装", "20230303");
        dataset.addValue(103, "精装", "20230304");
        dataset.addValue(102, "精装", "20230305");
//        dataset.addValue("price", "Letter", "年月日");
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(
                "价格趋势图", // 标题
                "Category Axis", // 横坐标标签
                "Value Axis", // 纵坐标标签
                dataset,
                PlotOrientation.VERTICAL, // 垂直
                true, //显示说明
                true,// 显示工具提示
                false
        );
        return chart;
    }



    public static void setChineseTheme(JFreeChart chart) {
        //设置支持中文的字体
        Font FONT = new Font("宋体", Font.PLAIN, 12);

        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        chartTheme.setExtraLargeFont(FONT);
        // 设置图例的字体
        chartTheme.setRegularFont(FONT);
        // 设置轴向的字体
        chartTheme.setLargeFont(FONT);
        chartTheme.setSmallFont(FONT);
        ChartFactory.setChartTheme(chartTheme);
        // 使当前主题马上生效
//        ChartUtilities.applyCurrentTheme(chart);
    }




    public static void main(String args[])throws Exception{
        LineChartDemo demo=new LineChartDemo("Line CHart Demo0");
        demo.pack();
        UIUtils.centerFrameOnScreen(demo);
//        demo.setVisible(true);


        ccc();
    }

/*private static  void timeDraw(){
    // A网站的访问量统计
    @SuppressWarnings("deprecation")
    TimeSeries timeSeries1 = new TimeSeries("A", Month.class);
    // 添加数据
    timeSeries1.add(new Month(1, 2016), 154);
    timeSeries1.add(new Month(2, 2016), 256);
    timeSeries1.add(new Month(3, 2016), 312);
    timeSeries1.add(new Month(4, 2016), 489);
    timeSeries1.add(new Month(5, 2016), 563);
    timeSeries1.add(new Month(6, 2016), 555);
    timeSeries1.add(new Month(7, 2016), 359);
    timeSeries1.add(new Month(8, 2016), 291);
    timeSeries1.add(new Month(9, 2016), 123);
    timeSeries1.add(new Month(10, 2016), 438);
    timeSeries1.add(new Month(11, 2016), 286);

    // A网站的访问量统计
    TimeSeries timeSeries2 = new TimeSeries("A", Month.class);
//    TimeSeries timeSeries2 = new TimeSeries("A", Month.class);
    // 添加数据
    timeSeries2.add(new Month(1, 2016), 124);
    timeSeries2.add(new Month(2, 2016), 326);
    timeSeries2.add(new Month(3, 2016), 12);
    timeSeries2.add(new Month(4, 2016), 567);
    timeSeries2.add(new Month(5, 2016), 546);
    timeSeries2.add(new Month(6, 2016), 123);
    timeSeries2.add(new Month(7, 2016), 222);
    timeSeries2.add(new Month(8, 2016), 545);
    timeSeries2.add(new Month(9, 2016), 56);
    timeSeries2.add(new Month(10, 2016), 543);
    timeSeries2.add(new Month(11, 2016), 221);

    // 定义时间序列的集合
    TimeSeriesCollection lineDataset = new TimeSeriesCollection();
    lineDataset.addSeries(timeSeries1);
    lineDataset.addSeries(timeSeries2);


//         JFreeChart chart = ChartFactory.createXYStepChart("Time line graph", "M", "F", xySeriesCollection, PlotOrientation.HORIZONTAL, false, false, false);
    JFreeChart chart = ChartFactory.createTimeSeriesChart("Time line graph", "M", "F", lineDataset, false, false, false);
    //设置主标题
    chart.setTitle(new TextTitle("A,B网站访问量统计对比图"));
    //设置子标题
    TextTitle subtitle = new TextTitle("2016年度", new Font("宋体", Font.BOLD, 12));
    chart.addSubtitle(subtitle);

    chart.setAntiAlias(true);

    //设置时间轴的范围。
    XYPlot plot = (XYPlot) chart.getPlot();
    DateAxis dateaxis = (DateAxis) plot.getDomainAxis();
    dateaxis.setDateFormatOverride(new SimpleDateFormat("M"));
    dateaxis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, 2));

    //设置曲线是否显示数据点
    XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) plot.getRenderer();
    xylinerenderer.setDefaultShapesVisible(true);
//    xylinerenderer.setBaseShapesVisible(true);

    //设置曲线显示各数据点的值
    XYItemRenderer xyitem = plot.getRenderer();
    xyitem.setDefaultItemLabelsVisible(true);
    xyitem.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
    xyitem.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator());
    xyitem.setDefaultItemLabelFont(new Font("Dialog", Font.BOLD, 12));
//    xyitem.setBaseItemLabelsVisible(true);
//    xyitem.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
//    xyitem.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
//    xyitem.setBaseItemLabelFont(new Font("Dialog", Font.BOLD, 12));
    plot.setRenderer(xyitem);

    JPanel jPanel = new ChartPanel(chart);
    JFrame frame = new JFrame("JFreechart Test");
    frame.add(jPanel);
    frame.setBounds(0, 0, 800, 600);
    frame.setVisible(true);
}*/


    private static void ccc(){
        // 创建数据
        TimeSeries series = new TimeSeries("Random Data");
        Second current = new Second();
        double value = 100.0;
        for (int i = 0; i < 4000; i++) {
            try {
                value = value + Math.random() - 0.5;
                series.add(current, value);
                current = (Second) current.next();
            } catch (SeriesException e) {
                System.err.println("Error adding to series");
            }
        }
        XYDataset dataset = (XYDataset) new TimeSeriesCollection(series);

        // 创建JFreeChart对象
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Computing Test",
                "Seconds",
                "Value",
                dataset,
                true,true, false);

        // 利用awt进行显示
        ChartFrame chartFrame = new ChartFrame("Test", chart);
        chartFrame.pack();
        chartFrame.setVisible(true);

    }
}
