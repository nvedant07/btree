import java.io.*;
import java.util.*;

public class external_merge_sort{

	private int buffer_size;
	private String filename;

	public external_merge_sort(String filename,int buffer_size){
		this.buffer_size=buffer_size;
		this.filename=filename;
		int number_of_files=this.create_fragments();
		this.merge_files(number_of_files);
	}

	public void merge_sort(tuple arr[], int l, int r){
		if (l < r){
			int m = l + (r - l) / 2;
			merge_sort(arr, l, m);
			merge_sort(arr, m + 1, r);
			merge(arr, l, m, r);
		}
	}
	public void merge(tuple arr[], int l, int m, int r){
	    int i, j, k;
	    int n1 = m - l + 1;
	    int n2 = r - m;
	 
	    tuple L[] = new tuple[n1];
	    tuple R[] = new tuple[n2];
	 
	    for(i = 0; i < n1; i++)
	        L[i] = arr[l + i];
	    for(j = 0; j < n2; j++)
	        R[j] = arr[m + 1 + j];
	 
	    i = 0;
	    j = 0;
	    k = l;
	    while (i < n1 && j < n2){
	        if (L[i].id <= R[j].id)
	            arr[k++] = L[i++];
	        else
	            arr[k++] = R[j++];
	    }
	 
	    while (i < n1)
	        arr[k++] = L[i++];
	 
	    while(j < n2)
	        arr[k++] = R[j++];
	}

	public int create_fragments(){
		try{
			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
		 
			int fileno=1,count=0;
			while ((strLine = br.readLine()) != null){
				
				tuple [] entries=new tuple[buffer_size];
				while(count<buffer_size && strLine != null){
					String [] parts=strLine.split(" ");
					int id=Integer.parseInt(parts[0]);	
					tuple t=new tuple(strLine,id);
				// System.out.println(strLine+" "+id+" "+count);
					
					entries[count]=t;
					count++;
					if(count<buffer_size)
						strLine = br.readLine();
				}
				// System.out.println(count+" "+fileno);
				merge_sort(entries,0,count-1);
				try{
					FileWriter fw = new FileWriter(Integer.toString(fileno));			
					for (int i = 0; i < count; i++) {
						fw.write(entries[i].record+"\n");
					}
					fw.close();
				}
				catch(IOException ex){
					System.out.println("File not found");
				}
				count=0;
				fileno++;
			}
			br.close();
			return (fileno-1);	
		}
		catch(IOException ex){
			System.out.println("File not found");
		}
		return -1;
	}

	public void merge_files(int n){
		try{
			BufferedReader [] readers = new BufferedReader[n];
			for(int i = 0; i < n; i++){
				// try{
					FileInputStream fstream = new FileInputStream(Integer.toString(i+1));
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
					readers[i]=br;
				// }
				// catch(IOException ex){
				// 	System.out.println("File not found");
				// }
			}

			heap_node [] arr=new heap_node[n];
			String strLine;
		 	int i;
			for(i=0; i<n; i++){
				if((strLine=readers[i].readLine()) == null)
					break;
				String [] parts=strLine.split(" ");
				int id=Integer.parseInt(parts[0]);
				heap_node t=new heap_node(strLine,id,i);
				arr[i]=t;
			}
			min_heap hp=new min_heap(arr,i);
			int count=0;
			FileWriter fw = new FileWriter("input-sorted.txt");
			while(count!=i){
				heap_node root=hp.get_root();
				fw.write(root.record+"\n");
				if((strLine=readers[root.fileno].readLine()) == null){
					root.id=1000000000;
					count++;
				}
				else{
					root=new heap_node(strLine, Integer.parseInt(strLine.split(" ")[0]), root.fileno);	
				}
				hp.replace_root(root);
			}
			fw.close();
			int j = n + 1;
			while(new File(Integer.toString(j)).delete()){
				j++;
			}
		}
		catch(IOException ex){
			System.out.println("LOL");
		}
		
	}

	// public static void main(String [] args){
	// 	external_merge_sort e=new external_merge_sort("input.txt",2);
	// }
}

class tuple{

	int id;
	String record;

	public tuple(String record, int id){
		this.record=record;
		this.id=id;
	}
}

class heap_node{
	int fileno;
	int id;
	String record;

	public heap_node(String record, int id, int fileno){
		this.record=record;
		this.id=id;
		this.fileno=fileno;
	}	
}

class min_heap{
	private int size;
	private heap_node [] arr;

	public min_heap(heap_node arr[], int size){
		this.size=size;
		this.arr=arr;
		int i=(size-1)/2;
		while(i>=0){
			min_heapify(i);
			i--;
		}
	}
	public void replace_root(heap_node x){
		this.arr[0]=x;
		this.min_heapify(0);
	}
	public heap_node get_root(){
		return this.arr[0];
	}
	public int left(int i){
		return 2*i+1;
	}
	public int right(int i){
		return 2*i+2;
	}
	public void min_heapify(int i){
		int l = left(i);
	    int r = right(i);
	    int smallest = i;
	    if (l < size && arr[l].id < arr[i].id)
	        smallest = l;
	    if (r < size && arr[r].id < arr[smallest].id)
	        smallest = r;
	    if (smallest != i)
	    {
	        heap_node temp=arr[i];
	        arr[i]=arr[smallest];
	        arr[smallest]=temp;
	        min_heapify(smallest);
	    }
	}
}