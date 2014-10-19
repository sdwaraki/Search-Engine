package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class Cluster 
{
	HashMap<Integer,HashMap<String,Double>> clusterDocumentVector = new HashMap<Integer,HashMap<String,Double>>();
	
	Cluster(int docList[]) throws CorruptIndexException, IOException
	{
		ArrayList<Integer> documentList = new ArrayList<Integer>(docList.length);
		
		for(int j=0;j<docList.length;j++)
		{
			documentList.add(docList[j]);
		}
			
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		TermEnum t = r.terms();
		double weight = 0.0;
		while(t.next())
		{
			String a = t.term().text();
			Term tx = new Term("contents",a);
			TermDocs td = r.termDocs(tx);
			while(td.next())
			{
				if(documentList.contains(td.doc()))
				{
					weight = td.freq();
					if(clusterDocumentVector.containsKey(td.doc()))
					{
						HashMap<String,Double> innerMap = clusterDocumentVector.get(td.doc());
						innerMap.put(t.term().text(), weight);
						clusterDocumentVector.put(td.doc(), innerMap);
					}
					else
					{
						HashMap<String,Double> inner = new HashMap<String,Double>();
						inner.put(t.term().text(), weight);
						clusterDocumentVector.put(td.doc(), inner);
					}
				}
			}
		}
	}
	
	HashMap<List<Integer>,String> compute_cluster(int docList[], HashMap<Integer,HashMap<String,Double>> document,String terms[]) throws Exception
	{
		
		int c1 = docList[new Random().nextInt(docList.length)];
		int c2 = docList[new Random().nextInt(docList.length)];
		int c3 = docList[new Random().nextInt(docList.length)];
				
		HashMap<String,Double> centroid1 = new HashMap<String,Double>();
		HashMap<String,Double> centroid2 = new HashMap<String,Double>();
		HashMap<String,Double> centroid3 = new HashMap<String,Double>();
		
		HashSet<Integer> cluster1 = new HashSet<Integer>();
		HashSet<Integer> cluster2 = new HashSet<Integer>(); 
		HashSet<Integer> cluster3 = new HashSet<Integer>();
		
		cluster1.add(c1);
		cluster2.add(c2);
		cluster3.add(c3);
		
		centroid1 = document.get(c1);
		centroid2 = document.get(c2);
		centroid3 = document.get(c3);
		
		HashSet<Integer> previousCluster1 = new HashSet<Integer>();
		HashSet<Integer> previousCluster2 = new HashSet<Integer>();
		HashSet<Integer> previousCluster3 = new HashSet<Integer>();
		
		List<Integer> finalCluster1=new ArrayList<Integer>();
		List<Integer> finalCluster2=new ArrayList<Integer>();
		List<Integer> finalCluster3=new ArrayList<Integer>();
		
		String summary1=null,summary2=null,summary3=null;
		
		HashMap<List<Integer>,String> finalResult = new HashMap<List<Integer>,String>();
		
		int flag = 1;
		int iteration = 0;
		while(flag==1)
		{
			previousCluster1.clear();
			previousCluster1.addAll(cluster1);
			previousCluster2.clear();
			previousCluster2.addAll(cluster2);
			previousCluster3.clear();
			previousCluster3.addAll(cluster3);
						
			System.out.println("Iteration "+(++iteration));
					
			cluster1.clear();
			cluster2.clear();
			cluster3.clear();	
			
			for(int i=0;i<docList.length;i++)
			{
				
				double distance1 = distanceCentroid(docList[i],centroid1,document);
				double distance2 = distanceCentroid(docList[i],centroid2,document);
				double distance3 = distanceCentroid(docList[i],centroid3,document);
				
				double dist_min = maximum_similarity(distance1,distance2,distance3);
				
				if(dist_min == distance1)
					cluster1.add(docList[i]);
				else if(dist_min == distance2)
					cluster2.add(docList[i]);
				else
					cluster3.add(docList[i]);
			}
			
			//print_Cluster(cluster1);
			//print_Cluster(cluster2);
			//print_Cluster(cluster3);
					
			centroid1.clear();
			centroid2.clear();
			centroid3.clear();
			
			centroid1 = computeCentroid(cluster1,document);
			centroid2 = computeCentroid(cluster2,document);
			centroid3 = computeCentroid(cluster3,document);
						
			boolean t1 = compareCluster(previousCluster1,cluster1);
			boolean t2 = compareCluster(previousCluster2,cluster2);
			boolean t3 = compareCluster(previousCluster3,cluster3);
					
			Map<Integer,Double> ranking;
			
			if(t1 && t2 && t3)
			{
				System.out.println("Done!!");
				HashMap<String, Double> idfCluster = computeIDFCluster(cluster1,cluster2,cluster3,clusterDocumentVector);
				if(cluster1.size()>0)
				{
					System.out.println("Cluster 1");
					ranking = computeRanking(cluster1,terms,document);
					finalCluster1=computeClusterList(ranking);
					print_Cluster(ranking);
					ranking.clear();
					summary1 = computeSummary(cluster1,clusterDocumentVector,idfCluster);
				}
				if(cluster2.size()>0)
				{
					System.out.println("Cluster 2");
					ranking = computeRanking(cluster2,terms,document);
					finalCluster2=computeClusterList(ranking);
					print_Cluster(ranking);
					ranking.clear();
					summary2 = computeSummary(cluster2,clusterDocumentVector,idfCluster);
				}
				if(cluster3.size()>0)
				{
					System.out.println("Cluster 3");
					ranking = computeRanking(cluster3,terms,document);
					finalCluster3=computeClusterList(ranking);
					print_Cluster(ranking);
					ranking.clear();
					summary3 = computeSummary(cluster3,clusterDocumentVector,idfCluster);
				}
				
				finalResult.put(finalCluster1, summary1);
				finalResult.put(finalCluster2, summary2);
				finalResult.put(finalCluster3, summary3);
				flag=0;
				
			}
		}
		return finalResult;
	}
	
	
	double distanceCentroid(int docID, HashMap<String,Double> centroid, HashMap<Integer,HashMap<String,Double>> document) throws Exception
	{
		// Compute Distance between document and centroid vector
		//HashMap<String,Double> docVector= computeDocumentVector(docID);
		HashMap<String,Double> docVector = document.get(docID);
		double dotProduct=0.0;
		
		for(Map.Entry<String, Double> entry : docVector.entrySet())
		{
			String term = entry.getKey();
			if(centroid.containsKey(term))
			{
				dotProduct+=entry.getValue()*centroid.get(term);
			}
		}
		
		
		double docNorm = 0.0;
		double centroidNorm=0.0;
		
		for(Map.Entry<String, Double> entry : docVector.entrySet())
		{
			docNorm += Math.pow(entry.getValue(),2);
		}
		
		for(Map.Entry<String, Double> entry : centroid.entrySet())
		{
			centroidNorm += Math.pow(entry.getValue(),2);
		}
		
		docNorm = Math.sqrt(docNorm);
		centroidNorm = Math.sqrt(centroidNorm);
		return (double) dotProduct/(docNorm*centroidNorm);
	}
	
	HashMap<String,Double> computeCentroid(HashSet<Integer> Cluster,HashMap<Integer,HashMap<String,Double>> document) throws Exception
	{
					//Compute Centroid of Cluster
		int clusterSize = Cluster.size();
		HashMap<String,Double> count = new HashMap<String,Double>();
		Iterator<Integer> it = Cluster.iterator();
		int docID;
		
		Object[] array = Cluster.toArray();
		for(int i=0;i<array.length;i++)
		{
			docID= (Integer) array[i];
							
			//HashMap<String,Double> docVector = computeDocumentVector(docID);
			HashMap<String,Double> docVector = document.get(docID);
			
			for(Map.Entry<String, Double> entry : docVector.entrySet())
			{
				double sum=0.0;
				String a = entry.getKey();
				if(count.containsKey(a))
				{
					sum=count.get(a)+entry.getValue();
					count.put(a, sum);
				}
				else
				{
					count.put(a, entry.getValue());
				}
			}
		}
		
		for(Map.Entry<String, Double> entry : count.entrySet())
		{
			double totalTermCount = entry.getValue()/clusterSize;
			count.put(entry.getKey(), totalTermCount);
		}
		return count;
	}
	
	double maximum_similarity(double d1, double d2, double d3)
	{
		return d1>d2?(d1>d3?d1:d3):(d2>d3?d2:d3);
	}
	
	
	boolean compareCluster(HashSet<Integer> a, HashSet<Integer> b)
	{
		if (a.equals(b)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	void print_Cluster(Map<Integer,Double> c)
	{
		int count = 0;
		Iterator<Integer> it = c.keySet().iterator();
		while(it.hasNext())
		{
			if(count < 3)
			{
				System.out.println(it.next());
				count++;
			}
			else
				return;
		}
	}
	
	HashMap<String,Double> computeIDFCluster(HashSet<Integer> a, HashSet<Integer> b, HashSet<Integer> c,HashMap<Integer,HashMap<String,Double>> document)
	{
		ArrayList<HashSet<Integer>> clusterArray = new ArrayList<HashSet<Integer>> ();
		clusterArray.add(a);
		clusterArray.add(b);
		clusterArray.add(c);
	
		Iterator <HashSet<Integer>> clusterIterator = clusterArray.iterator();
		HashMap <String,Integer> termCounter = new HashMap<String,Integer>(); 
		HashMap <String,Double> termIDF = new HashMap<String,Double>();
		
		while(clusterIterator.hasNext())
		{
			HashSet<Integer> presentCluster = clusterIterator.next();
			Object[] array = presentCluster.toArray();
			for(int i=0;i<array.length;i++)
			{
				HashMap<String,Double> docVector = document.get(array[i]);
				
				for(Map.Entry<String, Double> entry : docVector.entrySet())
				{
					if(termCounter.containsKey(entry.getKey()))
					{
						int sum = termCounter.get(entry.getKey());
						sum++;
						termCounter.put(entry.getKey(), sum);
					}
					else
					{
						termCounter.put(entry.getKey(), 1);
					}
				}
			}
		}
		
		for(Map.Entry<String,Integer> entry : termCounter.entrySet())
		{
			double weight = 50/entry.getValue();
			termIDF.put(entry.getKey(), weight);
		}
				
		return termIDF;
	}
	
	String computeSummary(HashSet<Integer> c, HashMap<Integer,HashMap<String,Double>> document, HashMap<String, Double> idfCluster)
	{
		
		Object [] array = c.toArray();
		double idf;
		double tfidf;
		double summaryWeight;
		HashMap<String,Double> summaryMap = new HashMap<String,Double>(); 
		String summary;
		
		for(int i=0;i<array.length;i++)
		{
			HashMap<String,Double> docVector = document.get(array[i]);
			for(Map.Entry<String, Double> entry : docVector.entrySet())
			{
				idf = idfCluster.get(entry.getKey());
				tfidf = entry.getValue() * idf;  
				if(summaryMap.containsKey(entry.getKey()))
				{
					summaryWeight = entry.getValue();
					summaryWeight +=tfidf;
					summaryMap.put(entry.getKey(), summaryWeight);
				}
				else
				{
					summaryMap.put(entry.getKey(), tfidf);
				}
			}
		}
		
		
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(summaryMap.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
	    {
	        public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
	        {
	            return (o2.getValue()).compareTo(o1.getValue());
	        }
	    });

	    Map<String, Double> result = new LinkedHashMap<String, Double>();
	    for (Map.Entry<String, Double> entry : list)
	    {
	    	result.put( entry.getKey(), entry.getValue() );
	    }
	    
	    Iterator<String> it  = result.keySet().iterator();
	    String x = (String) it.next();
	    //System.out.println(x);
	    return x;
	}
	
	
	
	
	Map<Integer,Double> computeRanking(HashSet<Integer> cluster, String terms[],HashMap<Integer,HashMap<String,Double>> document)
	{
		HashMap<String,Integer> querymap=new HashMap<String,Integer>();  //querymap will contain the query vector
		HashMap<Integer,Double> dotProduct = new HashMap<Integer,Double>();
		HashMap<Integer,Double> docNorm = new HashMap<Integer,Double>();
		HashMap<Integer,Double> result = new HashMap<Integer,Double>();
		
		double dProduct = 0.0;
		double weight = 0.0;
		double queryNorm = 0;
		double numerator = 0.0;
		double denominator = 0.0;
		double ratio = 0.0;
		
		
		for(String word : terms)
		{
			if(!word.isEmpty())
			{
				Integer freq=querymap.get(word);
				if(freq==null)
				{
					freq=0;
				}
				++freq;
				querymap.put(word, freq);
			}
		}
			
		for(Map.Entry<String, Integer> entry: querymap.entrySet()) //adding the frequencies of the terms to get |q|
		{
			int fr=(int) Math.pow(entry.getValue(), 2);
			queryNorm+=fr;
		}
		
		queryNorm=Math.sqrt(queryNorm);
		
		Object [] array = cluster.toArray();
		
		for(String word : terms)
		{
			for(int i=0;i<array.length;i++)
			{
				HashMap<String,Double> docVector = document.get(array[i]);
				dProduct=0.0;
				if(docVector.containsKey(word))
				{
					dProduct = querymap.get(word)*docVector.get(word);
				}
				
				if(!dotProduct.containsKey(array[i]))
				{
					dotProduct.put((Integer) array[i], dProduct);
				}
				else
				{
					double temp = dotProduct.get((Integer)array[i]);
					dotProduct.put((Integer)array[i], temp+dProduct);
				}
			}
		}
		
		for(int i=0;i<array.length;i++)
		{
			weight =0.0;
			HashMap<String,Double> docVector = document.get(array[i]);
			for(Map.Entry<String, Double> entry : docVector.entrySet())
			{
				weight = weight + Math.pow(entry.getValue(), 2);
			}
			weight = Math.sqrt(weight);
			docNorm.put((Integer)array[i], weight);
		}
		
		for(int i=0;i<array.length;i++)
		{
			numerator = dotProduct.get(array[i]);
			denominator = queryNorm * docNorm.get(array[i]);
			ratio = numerator/denominator;
			result.put((Integer)array[i],ratio);
		}
		
		List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(result.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<Integer, Double>>()
	    {
	        public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 )
	        {
	            return (o2.getValue()).compareTo(o1.getValue());
	        }
	    });

	    Map<Integer, Double> sortedResult = new LinkedHashMap<Integer, Double>();
	    for (Map.Entry<Integer, Double> entry : list)
	    {
	    	sortedResult.put( entry.getKey(), entry.getValue() );
	    }

		
		return sortedResult;
	}
	
	
	List<Integer> computeClusterList(Map<Integer,Double> ranking)
	{
		List<Integer> resultant = new ArrayList<Integer>();
		int count =0;
		for(Map.Entry<Integer, Double> entry : ranking.entrySet())
		{
			if(count< 3)
				resultant.add(entry.getKey());
			
		}
		return resultant;
	}
	
	
	
	
	
}
