package MyTextClassify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import jeasy.analysis.MMAnalyzer;

public class VSMSVMN2tfdfClassification {
	class WordItem{	
		private int[] cf;//cf储存在每一个类中出现该单词的文档数
		private boolean dfgt2;
		private double CHI;//X2统计的权值
		private int sumCf;
		public void computeCHI() {
			double A,B,C,D;
			CHI=0;
			for(int i=0;i<cf.length;i++) {
				A=cf[i];
				B=sumCf-A;
				C=documentCnt[i]-A;
				D=N-A-B-C;
				CHI+=(((A+C)/N)*(A*D-C*B)*(A*D-C*B)*N)/((A+C)*(B+D)*(A+B)*(C+D));
				
				//System.out.println(String.valueOf(A)+' '+String.valueOf(B)+' '+String.valueOf(C)+' '+String.valueOf(D)+' '+String.format("%.5f", t));								
			}
		}
		public int getSumCf() {
			return sumCf;
		}
		public void setSumCf(int i) {
			sumCf=i;
		}
		public double getCHI() {
			return CHI;
		}
		public void initCf() {
			for(int i=0;i<cf.length;i++) {
				cf[i]=0;
			}
		}
		public WordItem() {
			cf=new int[CLASSCOUNT];
			initCf();
			dfgt2=false;			
		}
		public WordItem(boolean b) {
			cf=new int[CLASSCOUNT];
			initCf();
			dfgt2=b;
		}
		public boolean getDfgt2() {
			return dfgt2;
		}
		public void addCf(int i) {
			this.cf[i]++;
		}
		public int getCf(int i) {
			return cf[i];
		}
	}
	public static F1[] f1_result=new F1[ConstantInterface.ATTRCOUNT.length];
	public static final int CLASSCOUNT=7;
	private int[] documentCnt=new int[CLASSCOUNT];
	private int N;
	private Map<String,WordItem> allWords;
	private static FileOutputStream fos;
	private PrintStream ps;
	String testPath;
	String trainPath;
	private char []a=new char[20000];
	private String[] labelsName=null;
	private CHIWordItem[] allCHI;
	private Vector<documentItem> trainDocuments;//存放所有训练文档向量
	public static void show_f1_result() {
		for(int i=0;i<f1_result.length;i++) {
			System.out.println(ConstantInterface.ATTRCOUNT[i]+"     "+f1_result[i].macro_avr+"  "+f1_result[i].micro_avr);
		}
	}
	public void setN(int i) {
		N=i;
	}
	public int getN() {
		return N;
	}
	public int findMax(double[] values){
		double max=values[0];
		int mark=0;
		for(int i=0;i<values.length;i++){
			if(values[i]>max){
				max=values[i];
				mark=i;
			}
		}
		return mark;
	}
	public double computeNorm(double[] d) {
		double norm=0;
		for(int i=0;i<d.length;i++) {
			norm+=d[i]*d[i];
		}
		norm=Math.pow(norm, 0.5);
		return norm;		
	}
	public double computeSim(double[] d1,double[] d2) {
		double n1,n2,sim=0;
		n1=computeNorm(d1);
		n2=computeNorm(d2);
		for(int i=0;i<d1.length;i++) {
			sim+=d1[i]*d2[i];
		}
		sim=sim/(n1*n2);
		return sim;				
	}
	public String[] sort(String[] pData, int left, int right){
		String middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].compareTo(middle)<0) && (i<right))
				i++;
			while((pData[j].compareTo(middle)>0) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sort(pData,left,j); //递归调用
		if(right>i)
			sort(pData,i,right); //递归调用
		return pData;
	}
	public CHIWordItem[] sort(CHIWordItem[] pData, int left, int right){
		CHIWordItem middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].CHI>middle.CHI) && (i<right))
				i++;
			while((pData[j].CHI<middle.CHI) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sort(pData,left,j); //递归调用
		if(right>i)
			sort(pData,i,right); //递归调用
		return pData;
	}
	public similarityItem[] sort(similarityItem[] pData, int left, int right){
		similarityItem middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].sim>middle.sim) && (i<right))
				i++;
			while((pData[j].sim<middle.sim) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sort(pData,left,j); //递归调用
		if(right>i)
			sort(pData,i,right); //递归调用
		return pData;
	}
	public similarityItem[] sortByCl(similarityItem[] pData, int left, int right){
		similarityItem strTemp,middle;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].cl.compareTo(middle.cl)<0) && (i<right))
				i++;
			while((pData[j].cl.compareTo(middle.cl)>0) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sortByCl(pData,left,j); //递归调用
		if(right>i)
			sortByCl(pData,i,right); //递归调用
		return pData;
	}
	Vector<String> read2Bigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1]));			
				//vtemp.add(String.valueOf(a[i]));
		}	
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2trigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-2;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
						vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2]));			
				//vtemp.add(String.valueOf(a[i]));
		}	
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2quadrigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-3;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
						if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
							vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2])+String.valueOf(a[i+3]));			
				//vtemp.add(String.valueOf(a[i]));
		}	
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2Unigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
						
				vtemp.add(String.valueOf(a[i]));
		}	
		return vtemp;
	}
	Vector<String> read2gram12(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]")) {						
				vtemp.add(String.valueOf(a[i]));
				if (String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[i+1]));
					vtemp.add(String.valueOf(a[i+1])+String.valueOf(a[i]));
				}
			}	
		}	
		if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) 
			vtemp.add(String.valueOf(a[a.length-1]));
		return vtemp;
	}
	Vector<String> read2gram1234(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-3;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[i]));
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1]));
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]")) {
						vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2]));
						if(String.valueOf(a[i+3]).matches("[\u4e00-\u9fa5]"))
							vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2])+String.valueOf(a[i+3]));
					}
				}
			}
		}
		if (String.valueOf(a[a.length-3]).matches("[\u4e00-\u9fa5]")) {//处理最后三个字
			vtemp.add(String.valueOf(a[a.length-3]));
			if (String.valueOf(a[a.length-2]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2]));
				vtemp.add(String.valueOf(a[a.length-3])+String.valueOf(a[a.length-2]));
				if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-3])+String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				}
			}else if(String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")){
				vtemp.add(String.valueOf(a[a.length-1]));
			}
		}else {
			if (String.valueOf(a[a.length-2]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2]));
				if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				}
			}else if(String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")){
				vtemp.add(String.valueOf(a[a.length-1]));
			}			
		}
		//System.out.println(vtemp.size());
		return vtemp;
	}
	public void setTrainPath(String folderPat){
		trainPath=folderPat;
	}
	public void setTestPath(String testPat){
		testPath=testPat;
	}
	 static Vector<String> readFile(String fileName) throws IOException, FileNotFoundException{
		 //返回一篇文章中分词后的字符串列表
		File f=new File(fileName);
		InputStreamReader isr=new InputStreamReader(new FileInputStream(f),"GBK");
		char[] cbuf=new char[(int) f.length()];
		isr.read(cbuf);
		Analyzer analyzer=new MMAnalyzer();//采用正向最大匹配的中文分词算法，相当于分词粒度等于1
		TokenStream tokens=analyzer.tokenStream("Contents", new StringReader(new String(cbuf)));
		Token token=null;
		Vector<String> v=new Vector<String>();
		while((token=tokens.next(new Token()))!=null){
			v.add(token.term());
		}
		return v;
	}
	public Vector<documentItem> train(int attrcount) throws IOException {
		trainDocuments=new Vector<documentItem>();
		allWords=new HashMap<String,WordItem>();
		ps=new PrintStream(fos);
		File folder=new File(trainPath);
		labelsName=folder.list();//存放所有类别的String[]
		System.out.println("Collecting attributes...");
		for(int i=0;i<labelsName.length;i++){//第一遍遍历把每个类中tf，df大于2的2gram放入allWords
			Map<String,WordItem> m=new HashMap<String, WordItem>();//存放该类下的2gram出现次数
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[0]);
			String[] firstWords=new String[v.size()];
			String[] otherWords;
			for(int j=0;j<v.size();j++)
				firstWords[j]=v.elementAt(j);
			sort(firstWords,0,v.size()-1);
			String previous=firstWords[0];
			double count=1;
			for(int j=1;j<firstWords.length;j++){
				if(firstWords[j].equals(previous))
					count++;
				else{
					if(count>=2)
						m.put(previous, new WordItem());//频度大于等于2
					//将上一个单词和出现次数放入Map中
					previous=firstWords[j];
					count=1;
				}
			}
			for(int j=1;j<files.length;j++){
				try {
					v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					otherWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						otherWords[k]=v.elementAt(k);
					sort(otherWords,0,v.size()-1);//对当前类别标签下的所有文档的所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=otherWords[0];
					count=1;
					for(int k=1;k<otherWords.length;k++){
						if(otherWords[k].equals(previous))
							count++;
						else{
							if(count>=2) {//频度大于等于2
								if (m.containsKey(previous))
									m.put(previous, new WordItem(true));//分散度大于等于2
								else
									m.put(previous, new WordItem());
							}
							previous=otherWords[k];
							count=1;
						}
					}
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Iterator<Entry<String, WordItem>> iter = m.entrySet().iterator();
			String key;
			while(iter.hasNext()) {
			    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
			    // 获取key
			    key = (String)entry.getKey();
			        // 获取value
			    if (entry.getValue().getDfgt2()&&!allWords.containsKey(key)) {
			    	allWords.put(key, new WordItem(true));
			    }
			}
			//System.out.println(allWords.size());
			documentCnt[i]=files.length;//该类别下训练的文本个数
			
		}
		setN(0);
		for (int i=0;i<documentCnt.length;i++) {//N-总训练文档数
			setN(getN()+documentCnt[i]);
		}
		System.out.println("N="+N);
		System.out.println("Computing CHI...");
		for(int i=0;i<labelsName.length;i++){//第二遍遍历把allWords填完
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous;
			for(int j=0;j<files.length;j++){
				try {
					v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对当前类别标签下的所有文档的所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(!tempWords[k].equals(previous)) {							
							if (allWords.containsKey(previous))
								allWords.get(previous).addCf(i);
							previous=tempWords[k];	
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}
		
		allCHI=new CHIWordItem[allWords.size()];
		Iterator<Entry<String, WordItem>> iter = allWords.entrySet().iterator();
		String key;
		int n=0;
		while(iter.hasNext()) {
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		        // 获取value
		    entry.getValue().setSumCf(0);
		    for(int i=0;i<entry.getValue().cf.length;i++) {
		    	entry.getValue().setSumCf(entry.getValue().getSumCf()+entry.getValue().getCf(i));
		    }
		    entry.getValue().computeCHI();
		    //System.out.println(entry.getValue().getCHI()); 
		    allCHI[n]=new CHIWordItem(key,entry.getValue().getCHI());
		    n++;
		}
		System.out.println("Filtering attributes...");
		sort(allCHI,0,allCHI.length-1);//按CHI排序，从大到小
		double temp,Nt;
		Nt=N;
		for(int i=0;i<ConstantInterface.ATTRCOUNT[attrcount];i++) {//计算前2000个2gram的d
			temp=Math.log10(Nt/allWords.get(allCHI[i].word).getSumCf()+0.01);
			allCHI[i].position=i;
			allCHI[i].d=temp;
		    //System.out.println(allCHI[i].position+"   "+String.format("%.5f", allCHI[i].CHI)+"   "+allCHI[i].word+"   "+allCHI[i].d);
		}
		double thresholdCHI=allCHI[ConstantInterface.ATTRCOUNT[attrcount]].CHI;
		iter = allWords.entrySet().iterator();
		while(iter.hasNext()) {//从allWords里删掉CHI排2000后的
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		        // 获取value
		    if (entry.getValue().getCHI()<=thresholdCHI) {
		    	iter.remove();
		    	/*在遍历Map过程中,不能用map.put(key,newVal),map.remove(key)来修改和删除元素， 
		    	     会引发 并发修改异常,可以通过迭代器的remove()： 
		    	    从迭代器指向的 collection 中移除当前迭代元素 
		    	    来达到删除访问中的元素的目的。   */
		    }
		}
		/*iter = allWords.entrySet().iterator();
		while(iter.hasNext()) {
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		    if(entry.getValue().getCHI()>820)
		        System.out.println(key+"   "+entry.getValue().getCHI());
		}*/
		System.out.println(ConstantInterface.ATTRCOUNT[attrcount]);
		System.out.println("Computing feature Vector...");
		double[] tfTemp=new double[ConstantInterface.ATTRCOUNT[attrcount]];//临时存放当前文章中属性单词的tf
		Map<String,Double> tf=new HashMap<String, Double>();//临时存放当前文章的单词词频
		int dCnt=0;
		for(int i=0;i<labelsName.length;i++){//第三遍遍历计算每个训练文档的向量
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous;
			int count;
			double denominator=0;
			documentItem tempDocItem;
			for(int j=0;j<files.length;j++){//读每一篇文章
				count=1;
				try {
					v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					dCnt++;
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(tempWords[k].equals(previous))
							count++;
						else {
							tf.put(previous, (double) count);//读取当前文章的单词词频
							previous=tempWords[k];
							count=1;
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
				for(int k=0;k<tfTemp.length;k++) {//统计属性单词的词频
					if(tf.containsKey(allCHI[k].word))
						tfTemp[k]=tf.get(allCHI[k].word);
					else {
						tfTemp[k]=0;
					}
				}
				for(int k=0;k<tfTemp.length;k++) {//计算TFIDF的分母
					denominator+=Math.pow(tfTemp[k]*allCHI[k].d, 2);
				}
				denominator=Math.pow(denominator, 0.5);
				tempDocItem=new documentItem(attrcount);
				
				ps.println(" ");
				ps.print(i);
				ps.print(" ");
				for(int k=0;k<tfTemp.length;k++) {//计算训练文档向量
					tempDocItem.setW(k,tfTemp[k]*allCHI[k].d/denominator);
					//ps.print(String.format("%.8f", tfTemp[k]*allCHI[k].d/denominator));
					ps.print(tfTemp[k]*allCHI[k].d/denominator);
					ps.print(" ");
					tempDocItem.cl=labelsName[i];
				}	
				ps.println(" ");
				trainDocuments.add(tempDocItem);
				tf.clear();
			}			
		}
		//System.out.println(trainDocuments.size());
		/*trainDocuments.elementAt(1).showW();
		trainDocuments.elementAt(2000).showW();
		trainDocuments.elementAt(5478).showW();*/
		System.out.println(dCnt);
		allWords.clear();
		return trainDocuments;
	}
	public void test(int attrcount){
		System.out.println("Testing...");
		ps=new PrintStream(fos);
		double[] tfTemp=new double[ConstantInterface.ATTRCOUNT[attrcount]];//临时存放当前文章中属性单词的tf
		Map<String,Double> tf=new HashMap<String, Double>();//临时存放当前文章的单词词频
		int dCnt=0;
		for(int m=0;m<labelsName.length;m++) {
			File testsubFolder=new File(testPath+"\\"+labelsName[m]);
			String[] testfiles=testsubFolder.list();
			int rnum=0;			
			double totalNum = testfiles.length;
			double recall;
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous=null;
			int count;
			double denominator=0;
			double mostSum;
			double curSum;
			String outcome = null;
			documentItem tempDocItem;//该测试文档的向量
			for(int j=0;j<testfiles.length;j++){//读每一篇文章
				count=1;
				try {
					v=readFile(testPath+"\\"+labelsName[m]+"\\"+testfiles[j]);
					dCnt++;
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(tempWords[k].equals(previous))
							count++;
						else {
							tf.put(previous, (double) count);//读取当前文章的单词词频
							previous=tempWords[k];
							count=1;
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
				for(int k=0;k<tfTemp.length;k++) {//统计属性单词的词频
					if(tf.containsKey(allCHI[k].word))
						tfTemp[k]=tf.get(allCHI[k].word);
					else {
						tfTemp[k]=0;
					}
				}
				for(int k=0;k<tfTemp.length;k++) {//计算TFIDF的分母
					denominator+=Math.pow(tfTemp[k]*allCHI[k].d, 2);
				}
				denominator=Math.pow(denominator, 0.5);
				tempDocItem=new documentItem(attrcount);
				ps.println(" ");
				ps.print(m);
				ps.print(" ");
				for(int k=0;k<tfTemp.length;k++) {//计算该测试文档向量
					tempDocItem.setW(k,tfTemp[k]*allCHI[k].d/denominator);
					//ps.print(String.format("%.8f", tfTemp[k]*allCHI[k].d/denominator));
					ps.print(tfTemp[k]*allCHI[k].d/denominator);
					ps.print(" ");
					tempDocItem.cl=labelsName[m];//该测试文档的真实类别
				}
				ps.println(" ");
				tf.clear();
				
			}
			
		}
		System.out.println(dCnt);
	}
	public static void main(String[] args) throws IOException {
		VSMSVMN2tfdfClassification vc=new VSMSVMN2tfdfClassification();
		long startTrain,endTrain,startTest,endTest;
		vc.trainPath="C:\\train";
		vc.testPath="C:\\test";
		try {
				
			for(int i=0;i<ConstantInterface.ATTRCOUNT.length;i++) {
				fos=new FileOutputStream("D:\\directory\\SVM_train"+ConstantInterface.ATTRCOUNT[i]+".txt");
				startTrain=System.currentTimeMillis();				
				Vector<documentItem> trainDocuments=vc.train(i);
				endTrain=System.currentTimeMillis();
				System.out.println("Training costs "+(endTrain-startTrain)/1000+"s");	
				fos.close();
			}
			
			for(int i=0;i<ConstantInterface.ATTRCOUNT.length;i++) {
				fos=new FileOutputStream("D:\\directory\\SVM_test"+ConstantInterface.ATTRCOUNT[i]+".txt");	
				startTest=System.currentTimeMillis();		
				vc.test(i);
				endTest=System.currentTimeMillis();
				System.out.println("Test costs "+(endTest-startTest)/1000+"s");	
				fos.close();
			}
			
			//show_f1_result();		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			fos.close();
		}
		
				
	}
}
