import java.io.*;
import java.util.*;

abstract class DataUnit{

}
class NodeUnit extends DataUnit{
	public static int max = 4; 
	private Node[] elem = new Node[max-1];
	private DataUnit[] ptrs = new DataUnit[max];
	private NodeUnit prnt;
	private int eInd = 0;
	private int count = 0;
	private int pInd = 0;
	public boolean isFull(){
		if (eInd == max-1)
			return true;
		else
			return false;
	}
	public void putElem(Node n){
		elem[eInd++] = n;
		count = eInd;
	}
	public void putPtr(DataUnit a){
		ptrs[pInd++] = a;
	}
	public NodeUnit getPrnt(){
		return prnt;
	}
	public Node getTop(){
		return elem[0];
	}
	public void setPrnt(NodeUnit _prnt){
		prnt = _prnt;
	}
	public void printElem(){
		for (int i=0; i<count; i++){
			System.out.print(elem[i].getData() + " ");
		}		
	}
	public DataUnit getPtr(int ind){
		return ptrs[ind];
	}
	public Node getElem(int ind){
		return elem[ind];
	}
	public void decElem(){
		eInd--;
	}
	public int getCount(){
		return count;
	}
	public DataUnit checkBelong(int n){
		for (int i=0; i<count; i++){
			if (elem[i].getData() > n)
				return ptrs[i];
		}
		if (ptrs[count] == null)
			return ptrs[count-1];
		return ptrs[count];
	}
	public boolean isLeaf(){
		if (ptrs[0] instanceof FileUnit){
			return true;
		}
		else{
			return false;
		}
	}

}
class FileUnit extends DataUnit{
	private int index;
	public FileUnit(int _index){
		index = _index;
	}
	public int getIndex(){
		return index;
	}
}
class Node{
	private int data;
	public Node(int _data){
		data = _data;
	}
	public int getData(){
		return data;
	}
}
abstract class Tree{
	protected NodeUnit root;
	public NodeUnit getRoot(){
		return root;
	}
	public void printRec(DataUnit d){
		if (d == null){
			return;
		}
		if (d instanceof FileUnit){
			FileUnit d2 = (FileUnit)d;
			System.out.println("File Index: "+d2.getIndex());
			return;
		}
		NodeUnit d2 = (NodeUnit)d;
		int till = NodeUnit.max;
		if (d2.isLeaf()){
			till--;
		}
		d2.printElem();
		System.out.println();
		for (int i=0; i<till; i++){
			printRec(d2.getPtr(i));
		}
	}
	public void printTree(){
		printRec(root);
	}
	public int findNodes(){
		return getNodes(root);
	}
	public int findHeight(){
		return getHeight(root);
	}
	public int getNodes(DataUnit d){
		if (d == null || d instanceof FileUnit){
			return 0;
		}
		NodeUnit d2 = (NodeUnit)d;
		int till = NodeUnit.max;
		if (d2.isLeaf()){
			till--;
		}
		int count = 1;
		for (int i=0; i<till; i++){
			count+=getNodes(d2.getPtr(i));
		}
		return count;
	}
	public int getHeight(DataUnit d){
		if (d == null || d instanceof FileUnit){
			return 0;
		}
		NodeUnit d2 = (NodeUnit)d;
		int till = NodeUnit.max;
		if (d2.isLeaf()){
			till--;
		}
		int count = 1;
		int maxval = 0;
		for (int i=0; i<till; i++){
			int var = getHeight(d2.getPtr(i));
			if (var > maxval)
				maxval = var;
		}
		count+=maxval;
		return count;
	}
	public void retRecord(int n){
		NodeUnit s = root;
		DataUnit c;
		while (!((c = s.checkBelong(n)) instanceof FileUnit)){
			s = (NodeUnit)c;
		}
		for (int i=0; i<s.getCount(); i++){
			if (s.getElem(i).getData() == n){
				c = s.getPtr(i);
				System.out.println("Index of search: "+((FileUnit)c).getIndex());
				return;
			}
		}
		System.out.println("Record Not Found!");
	}
	abstract public void makeTree(String file);
}
class TopDown extends Tree{
	public void makeTree(String file){

	}
}
class BottomUp extends Tree{
	public void createPrnt(NodeUnit a, NodeUnit b){
		NodeUnit n = new NodeUnit();
		root = n;
		n.putPtr(a);
		n.putPtr(b);
		n.putElem(b.getTop());
		//here
		if (!b.isLeaf()){
			Node temp = new Node(b.getTop().getData()+1);
			b.decElem();
			b.putElem(temp);
			b.decElem();
		}
		//end
		a.setPrnt(n);
		b.setPrnt(n);
	}
	public void prntAdd(NodeUnit prnt, NodeUnit d){
		if (prnt.isFull()){
			NodeUnit u = newContainer(d, d.getTop(), prnt);
			d.setPrnt(u);
		}
		else{
			prnt.putPtr(d);
			prnt.putElem(d.getTop());
			d.setPrnt(prnt);
			//here
			if (!d.isLeaf()){
				Node temp = new Node(d.getTop().getData()+1);
				d.decElem();
				d.putElem(temp);
				d.decElem();
			}
			//end
		}
	}
	public NodeUnit newContainer(DataUnit d, Node n, NodeUnit old){
		NodeUnit u = new NodeUnit();
		u.putPtr(d);
		u.putElem(n);
		NodeUnit prnt = old.getPrnt();
		if (prnt != null){
			prntAdd(prnt, u);
		}
		else{
			createPrnt(old, u);
		}
		return u;
	}
	public void makeTree(String file){
		try{
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			// int[] list = {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
			int i = 0;
			NodeUnit u = new NodeUnit();
			root = u;
			String c;
			while ((c = br.readLine()) != null){
				c = c.split(" ")[0];
				int a = Integer.parseInt(c);
				Node n = new Node(a);
				FileUnit f = new FileUnit(i);
				i++;
				if (u.isFull()){
					NodeUnit temp = newContainer(f,n,u);
					u.putPtr(temp);
					u = temp;
				}
				else{
					u.putPtr(f);
					u.putElem(n);
				}
			}
			br.close();
			fstream.close();
		}
		catch (IOException excep){
			System.out.println("Sorted File not Found!");
		}
	}
}
public class BTree{
	public static void main(String[] args) {
		// external_merge_sort sort = new external_merge_sort("input.txt", 4);
		Tree bup = new BottomUp();
		bup.makeTree("input-sorted.txt");
		// bup.printTree();
		System.out.println(bup.findNodes()+" "+bup.findHeight());
		bup.retRecord(1000);
	}
}