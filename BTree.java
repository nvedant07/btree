abstract class DataUnit{

}
class NodeUnit extends DataUnit{
	private Node[] elem = new Node[3];
	private DataUnit[] ptrs = new DataUnit[4];
	private NodeUnit prnt;
	private int eInd = 0;
	private int count = 0;
	private int pInd = 0;
	public boolean isFull(){
		if (eInd == 3)
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
	public void decElem(){
		eInd--;
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
public class BTree{
	private NodeUnit root;
	public void createPrnt(NodeUnit a, NodeUnit b){
		NodeUnit n = new NodeUnit();
		root = n;
		n.putPtr(a);
		n.putPtr(b);
		n.putElem(b.getTop());
		//here
		if (!b.isLeaf()){
			Node temp = new Node(b.getTop().getData()+1);
			// n.putElem(temp);
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
	public void makeTree(){
		//read file
		int[] list = {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
		int i = 0;
		NodeUnit u = new NodeUnit();
		root = u;
		for (int a : list){
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
	}
	public void printRec(DataUnit d){
		if (d == null || d instanceof FileUnit){
			return;
		}
		NodeUnit d2 = (NodeUnit)d;
		if (d2.isLeaf()){
			return;
		}
		d2.printElem();
		System.out.println();
		for (int i=0; i<4; i++){
			printRec(d2.getPtr(i));
		}

	}
	public void printTree(){
		printRec(root);

	}
	public static void main(String[] args) {
		BTree btree = new BTree();
		btree.makeTree();
		btree.printTree();
	}
}