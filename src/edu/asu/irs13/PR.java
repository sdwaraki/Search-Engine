package edu.asu.irs13;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;


public class PR 
{
	int maxdocs=25054;
	double [] resultant = new double[maxdocs];
	double [] r = new double[maxdocs];
	double qw=1.0/maxdocs;
	//List<Double> tempRow = new ArrayList<Double>(maxdocs);
	double [] tempRow = new double[maxdocs];
	double [] arr = new double[maxdocs];
	
	PR()
	{
		
		
		
		//-----Make Initial Probability Vector as 1/maxdocs------
		
		for(int i=0;i<maxdocs;i++)
		{
			r[i]=qw;
		}
		
	}
	
	public Map<Integer,String> precompute(Map<Integer, Double> sortedcosine,double w) throws Exception
	{
		
		IndexReader qwerty = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		
		double threshold=0.0001;
		double residual=1.0;
		int iterationCount=0;
		int k;
		LinkAnalysis l=new LinkAnalysis();
		
		while(residual>threshold)
		{
			iterationCount++; //counts number of iterations
			//System.out.println("Iteration "+iterationCount);
			
			if(iterationCount!=1)
			{
				for(int i=0;i<maxdocs;i++)
				{
					r[i] = resultant[i];
					resultant[i]=0.0;
				}
			}
			
			
			for(int i=0;i<maxdocs;i++)
			{
				int [] temp= l.getCitations(i);  //Get the citations to create stochastic matrix
				
				for(int j=0;j<maxdocs;j++)
					tempRow[j]=0.0;

				for(int j=0;j<temp.length;j++)
				{
					int len=l.getLinks(temp[j]).length;
					tempRow[temp[j]]=1.0/len;
				}
				
				
				int x=0;
				while(tempRow[x]==0 && l.getLinks(x).length==0)
				{
					tempRow[x]=qw;
					x++;
				}
				
				for(int h=0;h<maxdocs;h++)					//creating M*
					tempRow[h]=tempRow[h]*0.8+0.2*qw;
					
										
				//---------------------Matrix multiplication---------------------------------------------------------------------------------
				
				double sum=0.0;
				for(int p=0;p<maxdocs;p++)
				{
					
					sum=sum + tempRow[p]*r[p];
					
				}
				
				resultant[i]=sum;
				
			}
			
			
			//-----------------------------------Compare Both the Probability vector----------------------------------------------------------
			
			double max=resultant[0]-r[0];
			
			for(int z=1;z<maxdocs;z++)
			{
				double tempo=resultant[z]-r[z];
				if(max<tempo)					//finding the maximum difference
					max=tempo;
			}
				
			residual=max;
			
		}
	
		//-----------------------Normalize page Rank---------------------------------------
		
		double prsum=0.0;
		for(int kh=0;kh<maxdocs;kh++)
		{
			prsum=prsum+resultant[kh];
		}
		
		for(int kh=0;kh<maxdocs;kh++)
		{
			resultant[kh]=resultant[kh]/prsum;
		}
		
		//------------------Combine PageRank and Vector space similarity----------------------------------
		
		int ycount=0;
		HashMap <Integer,Double> combine=new HashMap<Integer,Double>();
		for(Map.Entry<Integer, Double> entry : sortedcosine.entrySet())
	    {
	    	if(ycount++<10)
	    	{
		    	int documentid=entry.getKey();
		    	double combined_result=w*resultant[documentid]+(1-w)*entry.getValue();   //adding w
		    	combine.put(documentid, combined_result);
	       	}
	    }
	    
		for(Map.Entry<Integer, Double> entry : combine.entrySet())
	    {
	    	System.out.println("Doc ID " + entry.getKey() +"------Similarity "+entry.getValue());
	    }
	    
		int zcount=0;
		Map<Integer,String> resultdocs=new LinkedHashMap<Integer,String>();
		for(Map.Entry<Integer,Double> entry:combine.entrySet())
		{
			if(zcount++<10)
			{
				int did=entry.getKey();
				Document di=qwerty.document(did);
				String urldi = di.getFieldable("path").stringValue(); 
				urldi=urldi.replace("%%", "/");
				resultdocs.put(did, urldi);
			}
		}
		
		
		return resultdocs;
		
	}
}

	
	
	
	
	
	
	
	
	
	
	


