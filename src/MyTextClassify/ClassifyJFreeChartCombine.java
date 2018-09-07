package MyTextClassify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Vector;

import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartUtilities;  
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;  
import org.jfree.chart.plot.CategoryPlot;  
import org.jfree.chart.plot.PlotOrientation;  
import org.jfree.chart.renderer.category.LineAndShapeRenderer;  
import org.jfree.data.category.CategoryDataset;  
import org.jfree.data.general.DatasetUtilities;  
  
//JFreeChart Line Chart（折线图）     
public class ClassifyJFreeChartCombine {  
    
    public static void main(String[] args) {  
        // 步骤1：创建CategoryDataset对象（准备数据）  
        CategoryDataset dataset = createDataset();  
        // 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置  
        JFreeChart freeChart = createChart(dataset);  
        // 步骤3：将JFreeChart对象输出到文件，Servlet输出流等  
        saveAsFile(freeChart, "D:\\directory\\combine.jpg",2000,1600);  
    }  
  
    // 保存为文件  
    public static void saveAsFile(JFreeChart chart, String outputPath,  
            int w, int h) {  
        FileOutputStream out = null;  
        try {  
            File outFile = new File(outputPath);  
            if (!outFile.getParentFile().exists()) {  
                outFile.getParentFile().mkdirs();  
            }  
            out = new FileOutputStream(outputPath);  
            // 保存为PNG  
            // ChartUtilities.writeChartAsPNG(out, chart, 600, 400);  
            // 保存为JPEG  
            ChartUtilities.writeChartAsJPEG(out, chart,w,h);  
            out.flush();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (out != null) {  
                try {  
                    out.close();  
                } catch (IOException e) {  
                    // do nothing  
                }  
            }  
        }  
    }  
  
    // 根据CategoryDataset创建JFreeChart对象  
    public static JFreeChart createChart(CategoryDataset categoryDataset) {  
        // 创建JFreeChart对象：ChartFactory.createLineChart  
        JFreeChart jfreechart = ChartFactory.createLineChart("F1 Averaging for each dimensionality reduction", // 标题  
        		"Dimension in Reduction",  // categoryAxisLabel （category轴，横轴，X轴标签）  
               "F1", // valueAxisLabel（value轴，纵轴，Y轴的标签）  
                categoryDataset, // dataset  
                PlotOrientation.VERTICAL, true, // legend  
                false, // tooltips  
                false); // URLs  
        // 使用CategoryPlot设置各种参数。以下设置可以省略。  
        CategoryPlot plot = (CategoryPlot)jfreechart.getPlot();  
        // 背景色 透明度  
        plot.setBackgroundAlpha(0.5f);  
        // 前景色 透明度  
        plot.setForegroundAlpha(0.5f);  
        // 其他设置 参考 CategoryPlot类  
        ValueAxis va=plot.getRangeAxis();
        va.setLowerBound(79);
        va.setUpperBound(97);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();  
        renderer.setBaseShapesVisible(true); // series 点（即数据点）可见  
        renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见  
        renderer.setUseSeriesOffset(true); // 设置偏移量  
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());  
        renderer.setBaseItemLabelsVisible(true);  
        return jfreechart;  
    }  
 
    public static CategoryDataset createDataset() {  
    	String s;
    	String subs;
    	String[] rowKeys = {"Macro_F1","Micro_F1"};  
    	String[] rowKeysAll= {"Macro_MI","Micro_MI","Macro_CHI","Micro_CHI","Macro_IG","Micro_IG"};
        Vector <String> v = new Vector();   //横坐标集合
       Vector <Double> dataMI1=new Vector();  //宏F1值集合
       Vector <Double> dataMI2=new Vector();  //微F1值集合
       Vector <Double> dataCHI1=new Vector();  //宏F1值集合
       Vector <Double> dataCHI2=new Vector();  //微F1值集合
       Vector <Double> dataIG1=new Vector();  //宏F1值集合
       Vector <Double> dataIG2=new Vector();  //微F1值集合
        int n;
    	try {
			FileInputStream fi=new FileInputStream("D:\\directory\\MI1.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fi));
			while((s=br.readLine())!=null) {
				subs=s.substring(0, 5);
				v.addElement(subs);
				n=s.indexOf(rowKeys[0])+rowKeys[0].length()+1;
				subs=s.substring(n, n+7);
				dataMI1.addElement(Double.parseDouble(subs));
				n=s.indexOf(rowKeys[1])+rowKeys[1].length()+1;
				subs=s.substring(n, n+7);
				dataMI2.addElement(Double.parseDouble(subs));
			}
			fi.close();
			fi=new FileInputStream("D:\\directory\\CHI3.txt");
		    br=new BufferedReader(new InputStreamReader(fi));
			while((s=br.readLine())!=null) {
				n=s.indexOf(rowKeys[0])+rowKeys[0].length()+1;
				subs=s.substring(n, n+7);
				dataCHI1.addElement(Double.parseDouble(subs));
				n=s.indexOf(rowKeys[1])+rowKeys[1].length()+1;
				subs=s.substring(n, n+7);
				dataCHI2.addElement(Double.parseDouble(subs));
			}
			fi.close();
			fi=new FileInputStream("D:\\directory\\IG3.txt");
		    br=new BufferedReader(new InputStreamReader(fi));
			while((s=br.readLine())!=null) {
				n=s.indexOf(rowKeys[0])+rowKeys[0].length()+1;
				subs=s.substring(n, n+7);
				dataIG1.addElement(Double.parseDouble(subs));
				n=s.indexOf(rowKeys[1])+rowKeys[1].length()+1;
				subs=s.substring(n, n+7);
				dataIG2.addElement(Double.parseDouble(subs));
			}
			fi.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String[] colKeys=new String[v.size()];  //横坐标数组
    	double[][] data=new double[6][dataMI1.size()];   //F1值集合
    	for(int i = 0;i < v.size();i++){ 
    		System.out.println(v.get(i)); 
    		colKeys[i]=v.get(i);
    	} 
    	for(int i = 0;i < dataMI1.size();i++){ 
    		System.out.println(dataMI1.get(i)); 
    		data[0][i]=dataMI1.get(i)*100;
    	} 
    	for(int i = 0;i < dataMI2.size();i++){ 
    		System.out.println(dataMI2.get(i)); 
    		data[1][i]=dataMI2.get(i)*100;
    	} 
    	for(int i = 0;i < dataCHI1.size();i++){ 
    		System.out.println(dataCHI1.get(i)); 
    		data[2][i]=dataCHI1.get(i)*100;
    	}
    	for(int i = 0;i < dataCHI2.size();i++){ 
    		System.out.println(dataCHI2.get(i)); 
    		data[3][i]=dataCHI2.get(i)*100;
    	} 
    	for(int i = 0;i < dataIG1.size();i++){ 
    		System.out.println(dataIG1.get(i)); 
    		data[4][i]=dataIG1.get(i)*100;
    	} 
    	for(int i = 0;i < dataIG2.size();i++){ 
    		System.out.println(dataIG2.get(i)); 
    		data[5][i]=dataIG2.get(i)*100;
    	} 
      
         
  
       /* String[] rowKeys = {"Macro_F1"};  
        String[] colKeys = {"1000","2000","3000","4000","5000","6000","7000","8000","9000","10000"};  
        double[][] data = {{92.76,94.85,93.37,93.76,93.94,93.43,95.09,94.95,94.80,95.11},};  */
        return DatasetUtilities.createCategoryDataset(rowKeysAll, colKeys, data);  
    }  
}  
