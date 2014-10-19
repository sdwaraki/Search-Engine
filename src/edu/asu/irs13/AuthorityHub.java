package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class AuthorityHub {

	int [] index;
	double m[][];
	double mt[][];
	double [] a;
	double [] h;
	double [] x;
	double [] y;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<Integer> ahcalculation(LinkedHashSet<Integer> set, int decision) throws Exception
	{
		//System.out.println("In the ahcalculation method");
		int count=0;
		int aflag=0,hflag=0;
		int k=0;
				HashMap <Integer,Double> indexmappingauth=new HashMap<Integer, Double>();
		HashMap <Integer,Double> indexmappinghub=new HashMap<Integer, Double>();
		//---------------------------------------Calculate the numberof keys in Base------------------------
		count=set.size();
		index=new int[count];
		a=new double[count];
		h=new double[count];
		x=new double[count];
		y=new double[count];
		m=new double[count][count];
		mt=new double[count] [count];
		
		//-------------------------------------Converting LinkedHashSet into ArrayList-----------------------
				// Doing this since I need the index of the elements in the LinkedHashSet to populate the adjacency matrix
				
				ArrayList<Integer> index = new ArrayList<Integer>();
				Iterator <Integer> it = set.iterator();
				while(it.hasNext())
				{
					index.add(it.next());			
				}
				
				//------------------------------------Creating the Adjacency Matrix given the index-------------------
				
				//initalize matrix to zero
				for(int i=0;i<count;i++)
					for(int j=0;j<count;j++)
						m[i][j]=0.0;
				
				//Populate Adjacency matrix
				LinkAnalysis li = new LinkAnalysis();
				Iterator<Integer> itr = index.iterator();
				while(itr.hasNext())
				{
					int x=itr.next();
					int links[] = li.getLinks(x);
					for(int i=0;i<links.length;i++)
					{
						if(set.contains(links[i]))
						{
							m[index.indexOf(x)][index.indexOf(links[i])]=1.0;
						}
					}
				}
				
				
				//--------------------------Calculating Authority and Hubs----------------------------------------------
				
				for(int q=0;q<count;q++)
				{
					a[q]=1;
					h[q]=1;
				}
				
				//--------------------------Find out transpose of the adjacency matrix----------------------------------
					for(int i=0;i<count;i++)
						for(int j=0;j<count;j++)
							mt[j][i]=m[i][j];
					
				
				while(aflag!=1 || hflag!=1)
				{
					
					//-------------------------Authority Calculation----------------------------
					
					double rsum=0.0;
					x=a.clone();
					for(int i=0;i<count;i++)
					{
						rsum=0.0;
						for(int j=0;j<count;j++)
						{
							rsum = rsum + mt[i][j] * h[j];
						}
						
						a[i]=rsum;
					}
					
					
					//------------For Hub Calculation-----------------------------------------
					
					rsum=0.0;
					y=h.clone();
					for(int i=0;i<count;i++)
					{
						rsum=0.0;
						for(int j=0;j<count;j++)
						{
							rsum=rsum+m[i][j]*a[j];
						}
						h[i]=rsum;
					}
				
				
					//--------------------------Normalize Authority Hub Values-----------------------------
					double asum = 0;
					double hsum = 0;
					for(int i=0;i<count;i++)
					{
						asum=asum + Math.pow(a[i],2);
						hsum=hsum + Math.pow(h[i],2);
					}
					
					for(int i=0;i<count;i++)
					{
						a[i]=(double)a[i]/Math.sqrt(asum);
						h[i]=(double)h[i]/Math.sqrt(hsum);
					}
					
					//-------------------------Compare for Convergence--------------------------------------
					
					boolean r1= converge(x,a);
					boolean r2= converge(y,h);
					
					if(r1&&r2)
					{
						aflag=1;
						hflag=1;
					}
				
					
					
					
				}
				
		
		//-------------------------Print Top 10 Authority with their Document Numbers-----------------
		
		//-------------------------Make a Hashmap of index,authority and sort them by value-----------
		
		for(int i=0;i<count;i++)
		{
			indexmappingauth.put(index.get(i), a[i]);
			indexmappinghub.put(index.get(i), h[i]);
		}
		
		//--------------------Sorting index, Authority Hashmap-----------------------------------------
		
		List<Map.Entry<Integer,Double>> l=new LinkedList<Map.Entry<Integer,Double>>(indexmappingauth.entrySet());
		Collections.sort(l, new Comparator(){
			public int compare(Object o1, Object o2)
			{
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		
		Map<Integer,Double> sortedauthMap = new LinkedHashMap<Integer,Double>();
		for (Iterator itx = l.iterator(); itx.hasNext();) {
			Map.Entry<Integer,Double> entry = (Map.Entry<Integer,Double>) itx.next();
			sortedauthMap.put(entry.getKey(), entry.getValue());
		}
		
		//-----------------------Sorting index, Hub HashMap--------------------------------------------------
		
		List<Map.Entry<Integer,Double>> m=new LinkedList<Map.Entry<Integer,Double>>(indexmappinghub.entrySet());
		Collections.sort(m, new Comparator(){
			public int compare(Object o1, Object o2)
			{
				return ((Comparable) ((Map.Entry<Integer,Double>) (o2)).getValue()).compareTo(((Map.Entry<Integer,Double>) (o1)).getValue());
			}
		});
		
		Map<Integer,Double> sortedhubMap = new LinkedHashMap<Integer,Double>();
		for (Iterator iter = m.iterator(); iter.hasNext();) {
			Map.Entry<Integer,Double> entry = (Map.Entry<Integer,Double>) iter.next();
			sortedhubMap.put(entry.getKey(), entry.getValue());
		}
		
		//return value sent through ahSorted.		
		int ahcount=0;
		ArrayList<Integer> ahSorted = new ArrayList<Integer>();
		if(decision==1)
		{
			
			
				for(Map.Entry<Integer,Double> entry : sortedauthMap.entrySet())
				{
					if(ahcount++<10)
					ahSorted.add(entry.getKey());
				}
			
		}
		else
		{
			
			
				for(Map.Entry<Integer,Double> entry : sortedhubMap.entrySet())
				{
					if(ahcount++<10)
					ahSorted.add(entry.getKey());
				}
			
			
		}
		System.out.println("ahsorted returning");
		return ahSorted;
	}
	
	
	//Converge function to check for convergence given a threshold.
	public boolean converge(double[] p, double [] n)
	{
		double threshold = 0.0002;
		boolean converged = false;
		for(int i=0;i<p.length;i++)
		{
			if(Math.abs(p[i]-n[i]) <= threshold)
			{
				converged=true;
			}
			else
				return false;
		}
		
		return converged;
		
	}
	
}
