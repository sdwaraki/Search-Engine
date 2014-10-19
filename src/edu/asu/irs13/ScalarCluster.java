package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ScalarCluster 
{
	
	@SuppressWarnings("null")
	String[] suggestion(String [] terms, HashMap<Integer,HashMap<String,Double>> document, int [] docList)
	{
		ArrayList<String> termList = new ArrayList<String>();
		ArrayList<Integer> documentList = new ArrayList<Integer>(docList.length);
		double[][] dtMatrix;
		double[][] tdMatrix;
		double[][] ttMatrix;
		double [] queryVector=null;
		double [] rowVector=null;
		int row;
		double similarity[];
		//double copysimilarity[];
		double maximumSim;
		int maximumPos;
		double secondmaximumSim;
		int secondmaximumPos = 0;
		String[] suggestTerm = null;
		
		for(int j=0;j<docList.length;j++)
		{
			documentList.add(docList[j]);
		}
		
		termList = computeTermList(document,documentList);
		
		dtMatrix = computeDocumentTermMatrix(documentList,termList,document);
		tdMatrix = computeTransposeMatrix(dtMatrix);
		ttMatrix = matrixMultiplication(tdMatrix,dtMatrix);
		ttMatrix = normalizeTTmatrix(ttMatrix);
		System.out.println("ttmatrix[100][100] is "+ttMatrix[100][100]);
		similarity = new double[termList.size()];
		//copysimilarity = new double[termList.size()];
		queryVector = new double[termList.size()];
		rowVector = new double[termList.size()];
		
		for(int k=0;k<terms.length;k++)
		{
			row = termList.indexOf(terms[k]);
			//queryVector = ttMatrix[row];
			Arrays.fill(queryVector, 0.0);
			Arrays.fill(rowVector, 0.0);
			
			for(int x=0;x<termList.size();x++)
			{
				queryVector[x]=ttMatrix[row][x];
			}
			
			
			for(int p=0;p<termList.size();p++)
			{
				//rowVector = ttMatrix[p];
				for(int x=0;x<ttMatrix.length;x++)
				{
					rowVector[x]=ttMatrix[p][x];
				}
				
				similarity[p] = computeMaxSimilarity(queryVector,rowVector);
			}
			
			similarity = getAbsoluteValue(similarity);
			
					
			maximumSim = similarity[0];
			maximumPos=0;
			for(int p=1;p<similarity.length;p++)
			{
				if(similarity[p]>maximumSim && p!= termList.indexOf(terms[k]))
				{
					secondmaximumSim=maximumSim;
					secondmaximumPos=maximumPos;
					maximumSim=similarity[p];
					maximumPos=p;
				}
			}
			
			suggestTerm[k]=termList.get(maximumPos);
			//System.out.println("Term correlation with " +terms[k]+" is "+firstTerm);
			//System.out.println("Second Term correlation with "+terms[k]+"is"+termList.get(secondmaximumPos));
			//find the maximum of the similarity---get the index----then get the term-----display it 
		}
		return suggestTerm;
	}
	
	ArrayList<String> computeTermList(HashMap<Integer,HashMap<String,Double>> document,List<Integer> docList)
	{
		
		LinkedHashSet <String> termSet = new LinkedHashSet<String>();
		ArrayList<String> termList; 
		Iterator<Integer> it = docList.iterator();
		
		while(it.hasNext())
		{
			HashMap<String,Double> docVector = document.get(it.next());
			for(Map.Entry<String, Double> inEntry : docVector.entrySet())
			{
				if(!termSet.contains(inEntry.getKey()))
					termSet.add(inEntry.getKey());
			}
		}
		
		termList =  new ArrayList<String>(termSet);
		
		return termList;
		
	}
	
	double[][] computeDocumentTermMatrix(List<Integer> docID, List<String> term,HashMap<Integer,HashMap<String,Double>> document)
	{
		//HashMap<String,Double> termIDF = computeIDF(docID,document);
		double dtMatrix[][] = new double[docID.size()][term.size()];
		String temp;
		int row,col,dnum;
		Iterator<Integer> it = docID.iterator();
		while(it.hasNext())
		{
			dnum = it.next();
			HashMap<String,Double> docVector = document.get(dnum);
			for(Map.Entry<String, Double> inEntry : docVector.entrySet())
			{
				temp = inEntry.getKey();
				col = term.indexOf(temp);
				row = docID.indexOf(dnum);
				dtMatrix[row][col] = inEntry.getValue();
			}
		}
		return dtMatrix;
	}
	
	double[][] computeTransposeMatrix(double [][] dtMatrix)
	{
		int dtCol = dtMatrix[0].length;
		int dtRow = dtMatrix.length;
		
		double[][] tdMatrix = new double[dtCol][dtRow];
		
		for(int i=0;i<dtRow;i++)
		{
			for(int j=0;j<dtCol;j++)
			{
				tdMatrix[j][i]=dtMatrix[i][j];
			}
		}
		return tdMatrix;
		
	}
	
	double[][] matrixMultiplication(double [][]tdMatrix, double [][]dtMatrix)
	{
		int rowsize = tdMatrix.length;
		int colsize = dtMatrix[0].length;
		
		double result[][] = new double[rowsize][colsize];
		double sum=0;
		
		for(int i = 0; i<rowsize; i++)
		{
			for(int j=0;j<colsize;j++)
			{
				for(int k=0;k<tdMatrix[0].length;k++)
				{
					sum = sum + (tdMatrix[i][k] * dtMatrix[k][j]);
				}
				result[i][j]=sum;
				sum=0;
			}
		}
		return result;
	}
	
	double computeMaxSimilarity(double[] queryVector, double[] rowVector)
	{
		double similarity = 0;
		double qNorm = 0.0;
		double rNorm = 0.0;
		double numerator=0.0;
		
		for(int i=0;i<queryVector.length;i++)
		{
				numerator += queryVector[i] * rowVector[i]; 
		}
		
		for(int i=0;i<queryVector.length;i++)
		{
				qNorm = qNorm + Math.pow(queryVector[i], 2);
		}
		
		qNorm = Math.sqrt(qNorm);
		
		for(int i=0;i<rowVector.length;i++)
		{
				rNorm = rNorm + Math.pow(rowVector[i], 2);
		}
		
		rNorm = Math.sqrt(rNorm);
		
		similarity = numerator/(qNorm*rNorm);
	
		return similarity;
	}
	
	double [][] normalizeTTmatrix(double[][] ttMatrix)
	{
		double [][] copyttMatrix = new double[ttMatrix.length][ttMatrix[0].length];
		
		for(int i=0;i<ttMatrix.length;i++)
		{
			for(int j=0;j<ttMatrix[0].length;j++)
			{
				copyttMatrix[i][j] = ttMatrix[i][j]/(ttMatrix[i][i] + ttMatrix[j][j]-ttMatrix[i][j]);
			}
		}
		return copyttMatrix;		
	}
	
	double[] getAbsoluteValue(double similarity[])
	{
		for(int i=0;i<similarity.length;i++)
		{
			if(similarity[i]<0)
				similarity[i]=-1*similarity[i];
		}
		
		return similarity;
	}
	
	
	
	   
	
/**	HashMap<String,Double> computeIDF(List<Integer> docID,HashMap<Integer,HashMap<String,Double>> document)
	{
		HashMap<String,Integer> termCounter = new HashMap<String,Integer>();
		HashMap <String,Double> termIDF = new HashMap<String,Double>();
		double weight;
		int sum;
		for(int i=0;i<docID.size();i++)
		{
			HashMap<String,Double> docVector = document.get(docID.get(i));
			
			for(Map.Entry<String, Double> entry : docVector.entrySet())
			{
				if(termCounter.containsKey(entry.getKey()))
				{
					sum = termCounter.get(entry.getKey());
					sum++;
					termCounter.put(entry.getKey(), sum);
				}
				else
				{
					termCounter.put(entry.getKey(), 1);
				}
			}
		}
		
		for(Map.Entry<String,Integer> entry : termCounter.entrySet())
		{
			weight = (double) 50/entry.getValue();
			weight = Math.log(weight);
			termIDF.put(entry.getKey(), weight);
		}
		return termIDF;
	}
**/
	
}
