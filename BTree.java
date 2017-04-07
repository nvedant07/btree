import java.io.*;
import java.util.*;

//This part has been referred from : https://gist.github.com/mikelikesbikes/4742901

class BPlusTree {

    public static Node tree;
    public static int degree;
    public static boolean debug;
    
    private BPlusTree(int x) {
      	degree = x;
    	tree = new LeafNode(degree);
    	debug = false;
    }
    
    public static void insertIntoTree(DataNode dnode) {
    	tree = tree.insert(dnode);
	}

	public static void searchTree(int x, BufferedWriter output) throws IOException {
		
		if( tree.search(new DataNode(x)) ) {
            output.write("FOUND" + System.getProperty("line.separator"));    
        }
        else {
            output.write("NOT FOUND" + System.getProperty("line.separator"));
        }
	}
	// public static int height;
	// public static int count_nodes;
	
	@SuppressWarnings("unchecked")
    public static void printTree(){
        Vector<Node> nodeList = new Vector();
        
        nodeList.add(tree);

        boolean done = false;
        int height=0,count_nodes=0;
        while(! done) {
        	height++;
        	Vector<Node> nextLevelList = new Vector();
            String toprint = "";
            
            for(int i=0; i < nodeList.size(); i++) {
            	count_nodes++;
            	Node node = (Node)nodeList.elementAt(i);
                
                toprint += node.toString() + " ";
                
                if(node.isLeafNode()) {
                    done = true;
                }
                else
                {
                    for(int j=0; j < node.size()+1 ; j++) {
                        nextLevelList.add( ((TreeNode)node).getPointerAt(j) );
                    }
                }
            }
            
            System.out.println(toprint + System.getProperty("line.separator"));
			nodeList = nextLevelList;
        }
        System.out.println("Height: "+height+" Number of nodes: "+count_nodes);
	}

	public static void readDegree(int x) {
    	new BPlusTree(x);
    }
}

abstract class Node {
	protected Vector<DataNode> data;
	protected Node parent;
	protected int maxsize;

	public boolean isLeafNode() {
		return this.getClass().getName().trim().equals("LeafNode");
	}

	abstract Node insert(DataNode dnode);
	abstract boolean search(DataNode x);

	protected boolean isFull() {
		return data.size() == maxsize-1;
	}
	
	public DataNode getDataAt(int index) {
		return (DataNode) data.elementAt(index);
	}
	
	protected void propagate(DataNode dnode, Node right) {
		
		if(parent == null) {
			
			TreeNode newparent = new TreeNode(maxsize);
			
			newparent.data.add(dnode);
			newparent.pointer.add(this);
			newparent.pointer.add(right);

			this.setParent(newparent);
			right.setParent(newparent);
		}
		else {
			if( ! parent.isFull() ) {
				boolean dnodeinserted = false;
				for(int i = 0; !dnodeinserted && i < parent.data.size(); i++) {
					if( ((DataNode)parent.data.elementAt(i)).inOrder(dnode) ) {
						parent.data.add(i,dnode);
						((TreeNode)parent).pointer.add(i+1, right);
						dnodeinserted = true;
					}
				}
				if(!dnodeinserted) {
					parent.data.add(dnode);
					((TreeNode)parent).pointer.add(right);
				}
				right.setParent(this.parent);
			}
			else {
                ((TreeNode)parent).split(dnode, this, right);
			}
		}
	}
	
	public int size() {
		return data.size();
	}

	@SuppressWarnings("unchecked") Node(int degree) {
		parent = null;
	    
	    data = new Vector();
	    maxsize = degree;
	}
	
	public String toString() {
		String s = "";
		for(int i=0; i < data.size(); i++) {
			s += ((DataNode)data.elementAt(i)).toString() + " ";
		}
		return s + "*";
	}

	protected Node findRoot() {
		Node node = this;
		
		while(node.parent != null) {
			node = node.parent;
		}
		
		return node;
	}

	protected void setParent(Node newparent) {
		this.parent = newparent;
	}
} 

class LeafNode extends Node {
	private LeafNode nextNode;
	
	LeafNode(int degree) {
		super(degree);
		
		nextNode = null;
	}
	
	private void setNextNode(LeafNode next) {
		nextNode = next;
	}
	
	protected LeafNode getNextNode() {
		return nextNode;
	}

	public boolean search(DataNode x) {
		for(int i=0; i < data.size(); i++) {
			if( ((DataNode)data.elementAt(i)).getData() == x.getData() ) {
				return true;
			}
		}
		return false;
	}

	protected void split(DataNode dnode) {
		boolean dnodeinserted = false;
		for(int i=0; !dnodeinserted && i < data.size(); i++) {
			if( ((DataNode)data.elementAt(i)).inOrder(dnode) ) {
				data.add(i,dnode);
				dnodeinserted = true;
			}
		}
		if(!dnodeinserted) {
			data.add(data.size(), dnode);
		}
		
		int splitlocation;
		if(maxsize%2 == 0) {
			splitlocation = maxsize/2;
		}
		else {
			splitlocation = (maxsize+1)/2;
		}
				
		LeafNode right = new LeafNode(maxsize);
		
		for(int i = data.size()-splitlocation; i > 0; i--) {
			right.data.add(data.remove(splitlocation));
		}
		
		right.setNextNode(this.getNextNode());
		this.setNextNode(right);
		
		// DataNode mid =  (DataNode) data.elementAt(data.size()-1);
		DataNode mid =  (DataNode) right.data.elementAt(0);

		this.propagate(mid, right);
	}

	public Node insert(DataNode dnode) {
		if(data.size() < maxsize-1) {
			boolean dnodeinserted = false;
			int i = 0;
			while(!dnodeinserted && i < data.size()) {
				if( ((DataNode)data.elementAt(i)).inOrder(dnode) ) {
					data.add(i,dnode);
					dnodeinserted = true;
				}
				i++;
			}
			if(!dnodeinserted) {
				data.add(data.size(), dnode);
			}
		}
		else {
			this.split(dnode);
		}
		
		return this.findRoot();
	}
}

class TreeNode extends Node {
	protected Vector<Node> pointer;
	
	@SuppressWarnings("unchecked") TreeNode(int x) {
		super(x);
		pointer = new Vector();
	}

	public Node getPointerTo(DataNode x) {
		int i = 0;
		boolean xptrfound = false;
		while(!xptrfound && i < data.size()) {
			if( ((DataNode)data.elementAt(i)).inOrder(x ) ) {
				xptrfound = true;
			}
			else {
				i++;				
			}

		}
		return (Node) pointer.elementAt(i);
	}

	public Node getPointerAt(int index) {
		return (Node) pointer.elementAt(index);
	}

	boolean search(DataNode dnode) {
		Node next = this.getPointerTo(dnode);
		return next.search(dnode);
	}

	protected void split(DataNode dnode, Node left, Node right) {
		int splitlocation, insertlocation = 0; 
		if(maxsize%2 == 0) {
			splitlocation = maxsize/2;
		}
		else {
			splitlocation = (maxsize+1)/2 -1;
		}
		
		boolean dnodeinserted = false;
		for(int i=0; !dnodeinserted && i < data.size(); i++) {
			if( ((DataNode)data.elementAt(i)).inOrder(dnode) ) {
				data.add(i,dnode);
				((TreeNode)this).pointer.remove(i);
				((TreeNode)this).pointer.add(i, left);
				((TreeNode)this).pointer.add(i+1, right);
				dnodeinserted = true;
                
                insertlocation = i;
			}
		}
		if(!dnodeinserted) {
            insertlocation = data.size();
			data.add(dnode);
			((TreeNode)this).pointer.remove(((TreeNode)this).pointer.size()-1);
			((TreeNode)this).pointer.add(left);
			((TreeNode)this).pointer.add(right);
		}
		
		DataNode mid = (DataNode) data.remove(splitlocation);
		
		TreeNode newright = new TreeNode(maxsize);
		
		for(int i=data.size()-splitlocation; i > 0; i--) {
			newright.data.add(this.data.remove(splitlocation));
			newright.pointer.add(this.pointer.remove(splitlocation+1));
		}
		newright.pointer.add(this.pointer.remove(splitlocation+1));		

        if(insertlocation < splitlocation) {
            left.setParent(this);
            right.setParent(this);
        }
        else if(insertlocation == splitlocation) {
            left.setParent(this);
            right.setParent(newright);
        }
        else {
            left.setParent(newright);
            right.setParent(newright);
        }
		this.propagate(mid, newright);
	}

	Node insert(DataNode dnode) {
		Node next = this.getPointerTo(dnode);
		
		return next.insert(dnode);
	}
}

class DataNode {
    private Integer data;
    
    DataNode() {
        data = null;
    }   
    public String toString() {
		return data.toString();
	}
	public DataNode(int x) {
        data = x;
    }
    public int getData() {
        return data.intValue();
    }   
    public boolean inOrder(DataNode dnode) {
        return (dnode.getData() <= this.data.intValue());
    }
}


//till here

abstract class DataUnit{

}
class NodeUnit extends DataUnit{
	public static int max = 4; 
	private NodeBU[] elem = new NodeBU[max-1];
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
	public void putElem(NodeBU n){
		elem[eInd++] = n;
		count = eInd;
	}
	public void putElemAt(NodeBU n,int i){
		elem[i] = n;
	}
	public void putPtr(DataUnit a){
		ptrs[pInd++] = a;
	}
	public void putPtrAt(DataUnit a,int i){
		ptrs[i] = a;
	}
	public NodeUnit getPrnt(){
		return prnt;
	}
	public NodeBU getTop(){
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
	public NodeBU getElem(int ind){
		return elem[ind];
	}
	public void decElem(){
		eInd--;
	}
	public NodeBU deleteElem(){
		eInd--;
		count=eInd;
		return elem[eInd+1];
	}
	public void decPtr(){
		pInd--;
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
class NodeBU{
	private int data;
	public NodeBU(int _data){
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
	NodeUnit u;
	int i;

	public void makeTree(String file){
		System.out.println("function called");
		try{
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			// int[] list = {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
			i = 0;
			u = new NodeUnit();
			root = u;
			
			String c;
			while ((c = br.readLine()) != null){
				c = c.split(" ")[0];
				int a = Integer.parseInt(c);
				System.out.println(a);
				this.insert(a);
			}
			// if(root.getCount()==0)
			System.out.println(root.getCount()+" "+root.getElem(0).getData()+" "+root.getElem(1).getData()+" "+root.getElem(2).getData());
			
			br.close();
			fstream.close();
		}
		catch (IOException excep){
			System.out.println("Sorted File not Found!");
		}
	}
	public void insert(int a){
		NodeUnit temp=root;
		if(root.getCount()==0){
			FileUnit f=new FileUnit(i);
			i++;
			root.putPtr(f);
			root.putElem(new NodeBU(a));
			// System.out.println("count:"+root.getCount());
		}
		else{
			while(! temp.isLeaf() ){
				int j;
				for(j=0; j<temp.getCount(); j++){
					if(temp.getElem(j).getData() > a)
						break;
				}
				temp=(NodeUnit)temp.getPtr(j);
			}
			if(temp.getCount() < NodeUnit.max - 1 ){
				int j;
				for( j=0; j<temp.getCount(); j++){
					if(temp.getElem(j).getData() > a){ 
						break;
					}
				}
				if(j!=temp.getCount()){
					int z = temp.getCount()-1;
					NodeBU newnode=new NodeBU(a);
					FileUnit newfile = new FileUnit(i);
					i++;
					while(z != j){
						NodeBU t = temp.deleteElem();
						temp.putElemAt(t,z+1);
						z--;
					}
					temp.putElemAt(newnode, z);
					// NodeBU newnode=new NodeBU(a);
					// FileUnit newfile = new FileUnit(i);
					// i++;
					// NodeBU oldnode=temp.getElem(j);
					// FileUnit oldptr=(FileUnit) temp.getPtr(j);
					// temp.decElem();
					// temp.decPtr();
					// temp.putElem(newnode);
					// temp.putPtr(newfile);
					// temp.putElem(oldnode);
					// temp.putPtr(oldptr);
				}
				else{
					NodeBU newnode=new NodeBU(a);
					FileUnit newfile = new FileUnit(i);
					i++;
					temp.putElem(newnode);
					temp.putPtr(newfile);	
				}
			}
			else{
				split(temp,a,new FileUnit(i++));
				// NodeUnit newnode=new NodeUnit();

				// if(temp==root)
			}
		}
	}
	public void split(NodeUnit temp, int a, DataUnit ptr){
		int pos;
		for( pos=0; pos<temp.getCount(); pos++){
			if(temp.getElem(pos).getData() > a){ 
				break;
			}
		}
		NodeUnit newnode=new NodeUnit();
		NodeUnit oldnode=new NodeUnit();
		for(int j = 0; j < temp.getCount() + 1; j++){
			if(j<temp.getCount()/2){
				if(j==pos)
					oldnode.putElem(new NodeBU(a));
				else
					oldnode.putElem(temp.getElem(j));
			}
			else{
				if(j==pos)
					newnode.putElem(new NodeBU(a));
				else
					newnode.putElem(temp.getElem(j));
			}
			newnode.putElem(temp.getElem(j));
			newnode.putPtr(temp.getPtr(j));
			temp.decElem();
			temp.decPtr();
		}
		NodeBU node_to_be_inserted=new NodeBU(a);
		newnode.putElem(node_to_be_inserted);
		newnode.putPtr(ptr);
		NodeUnit parent=temp.getPrnt();
		if(parent==null){
			parent=new NodeUnit();
			parent.putElem(node_to_be_inserted);
			parent.putPtr(temp);
			parent.putPtr(newnode);
			root=parent;	
		}
		else if(!parent.isFull()){
			parent.putElem(node_to_be_inserted);
			parent.putPtr(newnode);
		}
		else{
			split(parent,a,newnode);
		}
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
			NodeBU temp = new NodeBU(b.getTop().getData()+1);
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
			//here
			if (!d.isLeaf()){
				NodeBU temp = new NodeBU(d.getTop().getData()+1);
				d.decElem();
				d.putElem(temp);
				d.decElem();
			}
			//end
		}
		else{
			prnt.putPtr(d);
			prnt.putElem(d.getTop());
			d.setPrnt(prnt);
			//here
			if (!d.isLeaf()){
				NodeBU temp = new NodeBU(d.getTop().getData()+1);
				d.decElem();
				d.putElem(temp);
				d.decElem();
			}
			//end
		}
	}
	public NodeUnit newContainer(DataUnit d, NodeBU n, NodeUnit old){
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
				NodeBU n = new NodeBU(a);
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
		external_merge_sort sort = new external_merge_sort("input.txt", 4);
		
		System.out.println("Bottom Up:");
		
		Tree bup = new BottomUp();
		long starttime=System.currentTimeMillis();
		
		bup.makeTree("input-sorted.txt");
		
		long end=System.currentTimeMillis();
		
		bup.printTree();
		
		// System.out.println(end-starttime);
		System.out.println("Height: "+bup.findHeight()+" Number of Nodes: "+bup.findNodes()+"\n");
		// bup.retRecord(1000);

		System.out.println("\nTop Down:");

		BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("input-sorted.txt")));
            BPlusTree.readDegree(NodeUnit.max);
            String strLine;
			
			 starttime=System.currentTimeMillis();
			
			while ((strLine = in.readLine()) != null){
				BPlusTree.insertIntoTree(new DataNode(Integer.parseInt(strLine.split(" ")[0])));	
			}
			 end=System.currentTimeMillis();

			in.close();
			System.out.println();
			BPlusTree.printTree();
		// System.out.println(end-starttime);


        } catch (IOException e) {
            System.err.println("Error: specified file not found");
        }

        System.out.println("\nInserting one by one:");

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
            BPlusTree.readDegree(NodeUnit.max);
            String strLine;
			
			 starttime=System.currentTimeMillis();

			while ((strLine = in.readLine()) != null){
				BPlusTree.insertIntoTree(new DataNode(Integer.parseInt(strLine.split(" ")[0])));	
			}
			 end=System.currentTimeMillis();
			
			in.close();
			System.out.println();
			BPlusTree.printTree();
		// System.out.println(end-starttime);


        } catch (IOException e) {
            System.err.println("Error: specified file not found");
        }
		// BPlusTree.readDegree(NodeUnit.max);
		// Tree tpd = new TopDown();
		// System.out.println("making tree");
		// tpd.makeTree("input.txt");
		// System.out.println("making tree");
		
	}
}