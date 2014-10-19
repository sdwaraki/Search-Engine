package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class LinkAnalysis{

	public static final String linksFile = "C:/Users/Sumanth/workspace/IRProject/IntCitations.txt";
	public static final String citationsFile = "C:/Users/Sumanth/workspace/IRProject/IntCitations.txt";
	public static int numDocs = 25054;

	private int[][] links;
	private int[][] citations;
	
	public LinkAnalysis()throws Exception   
	{
		//try
		//{
			// Read in the links file
			links = new int[numDocs][];
			BufferedReader br = new BufferedReader(new FileReader(linksFile));
			String s = "";
			while ((s = br.readLine())!=null)
			{
				//System.out.println("Reading line"+s); 
				String[] words = s.split("->"); // split the src->dest1,dest2,dest3 string
				int src = Integer.parseInt(words[0]);
				if (words.length > 1 && words[1].length() > 0)
				{
					String[] dest = words[1].split(",");
					links[src] = new int[dest.length];
					for (int i=0; i<dest.length; i++)
					{
						links[src][i] = Integer.parseInt(dest[i]);
					}
				}
				else
				{
					links[src] = new int[0];
				}
			}
			//System.out.println("--------------------------ppppp-----------");
			br.close();
			
			
			// Read in the citations file
			citations = new int[numDocs][];
			br = new BufferedReader(new FileReader(citationsFile));
			s = "";
			while ((s = br.readLine())!=null)
			{
				//System.out.println("Reading citation file lines " +s);
				String[] words = s.split("->"); // split the src->dest1,dest2,dest3 string
				int src = Integer.parseInt(words[0]);
				if (words.length > 1 && words[1].length() > 0)
				{
					String[] dest = words[1].split(",");
					citations[src] = new int[dest.length];
					for (int i=0; i<dest.length; i++)
					{
						citations[src][i] = Integer.parseInt(dest[i]);
					}
				}
				else
				{
					citations[src] = new int[0];
				}

			}
			br.close();
		//}
		//catch(NumberFormatException e)
		//{
		//	System.err.println("links file is corrupt: ");
		//	e.printStackTrace();			
		//}
		//catch(IOException e)
		//{
		//	System.err.println("Failed to open links file: ");
		//	e.printStackTrace();
		//}
	}
	
	public int[] getLinks(int docNumber)
	{
		return links[docNumber];
	}
	
	public int[] getCitations(int docNumber)
	{
		return citations[docNumber];
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> link_main(int [] documentID, int decision) throws Exception
	{
		int []rootset;
		for(int i=0;i<documentID.length;i++)
			System.out.println(documentID[i]);
		rootset=new int[documentID.length];
		//System.out.println("\nIn the Links main function");
		for(int i=0;i<documentID.length;i++)
			rootset[i]=documentID[i];
		
		LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();
		set=addToHashSet(set,rootset);
		for(int i=0;i<rootset.length;i++)
		{
			int links[] = getLinks(rootset[i]);
			set=addToHashSet(set,links);
			int citations[] = getCitations(rootset[i]);
			set=addToHashSet(set,citations);
		}
		
				
		AuthorityHub a = new AuthorityHub();
		ArrayList<Integer> result=new ArrayList<Integer>();
			result=a.ahcalculation(set,decision);
			
				/**for(int pb:cit3)
					{
						System.out.print(pb + ",");
					}**/
		return result;
				
		}
	
	public LinkedHashSet<Integer> addToHashSet(LinkedHashSet<Integer> set,int[]a)
	{
		for(int j=0;j<a.length;j++)
		{
			set.add(a[j]);
		}
		return set;
	}	

	
}
