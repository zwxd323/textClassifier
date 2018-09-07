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
  
//JFreeChart Line Chart������ͼ��     
public class ClassifyJFreeChartSVM {  
    
    public static void main(String[] args) {  
        // ����1������CategoryDataset����׼�����ݣ�  
        CategoryDataset dataset = createDataset();  
        // ����2������Dataset ����JFreeChart�����Լ�����Ӧ������  
        JFreeChart freeChart = createChart(dataset);  
        // ����3����JFreeChart����������ļ���Servlet�������  
        saveAsFile(freeChart, "D:\\directory\\SVM.jpg",1500,1100);  
    }  
  
    // ����Ϊ�ļ�  
    public static void saveAsFile(JFreeChart chart, String outputPath,  
            int w, int h) {  
        FileOutputStream out = null;  
        try {  
            File outFile = new File(outputPath);  
            if (!outFile.getParentFile().exists()) {  
                outFile.getParentFile().mkdirs();  
            }  
            out = new FileOutputStream(outputPath);  
            // ����ΪPNG  
            // ChartUtilities.writeChartAsPNG(out, chart, 600, 400);  
            // ����ΪJPEG  
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
  
    // ����CategoryDataset����JFreeChart����  
    public static JFreeChart createChart(CategoryDataset categoryDataset) {  
        // ����JFreeChart����ChartFactory.createLineChart  
        JFreeChart jfreechart = ChartFactory.createLineChart("Precision for each dimensionality reduction", // ����  
        		"Dimension in Reduction",  // categoryAxisLabel ��category�ᣬ���ᣬX���ǩ��  
               "Precision", // valueAxisLabel��value�ᣬ���ᣬY��ı�ǩ��  
                categoryDataset, // dataset  
                PlotOrientation.VERTICAL, true, // legend  
                false, // tooltips  
                false); // URLs  
        // ʹ��CategoryPlot���ø��ֲ������������ÿ���ʡ�ԡ�  
        CategoryPlot plot = (CategoryPlot)jfreechart.getPlot();  
        // ����ɫ ͸����  
        plot.setBackgroundAlpha(0.5f);  
        // ǰ��ɫ ͸����  
        plot.setForegroundAlpha(0.5f);  
        // �������� �ο� CategoryPlot��  
        ValueAxis va=plot.getRangeAxis();
        va.setLowerBound(20);
        va.setUpperBound(90);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();  
        renderer.setBaseShapesVisible(true); // series �㣨�����ݵ㣩�ɼ�  
        renderer.setBaseLinesVisible(true); // series �㣨�����ݵ㣩�������߿ɼ�  
        renderer.setUseSeriesOffset(true); // ����ƫ����  
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());  
        renderer.setBaseItemLabelsVisible(true);  
        return jfreechart;  
    }  
 
    public static CategoryDataset createDataset() {  
    	String s;
    	String subs;
    	String[] rowKeys = {"Precision"};  
        Vector <String> v = new Vector();  
       Vector <Double> data1=new Vector();
       Vector <Double> data2=new Vector();
        int n;
    	try {
			FileInputStream fi=new FileInputStream("D:\\directory\\showSVM.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fi));
			while((s=br.readLine())!=null) {
				n=s.indexOf("0.");
				subs=s.substring(0, n-1);
				v.addElement(subs);
				subs=s.substring(n, n+7);
				data1.addElement(Double.parseDouble(subs));
				n=s.indexOf(rowKeys[1])+rowKeys[1].length()+1;
				subs=s.substring(n, n+7);
				data2.addElement(Double.parseDouble(subs));
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String[] colKeys=new String[v.size()];
    	double[][] data=new double[2][data1.size()]; 
    	for(int i = 0;i < v.size();i++){ 
    		System.out.println(v.get(i)); 
    		colKeys[i]=v.get(i);
    		} 
    	for(int i = 0;i < data1.size();i++){ 
    		System.out.println(data1.get(i)); 
    		data[0][i]=data1.get(i)*100;
    		} 
    	for(int i = 0;i < data2.size();i++){ 
    		System.out.println(data2.get(i)); 
    		data[1][i]=data2.get(i)*100;
    		} 
      
         
  
       /* String[] rowKeys = {"Macro_F1"};  
        String[] colKeys = {"1000","2000","3000","4000","5000","6000","7000","8000","9000","10000"};  
        double[][] data = {{92.76,94.85,93.37,93.76,93.94,93.43,95.09,94.95,94.80,95.11},};  */
        return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);  
    }  
}  
