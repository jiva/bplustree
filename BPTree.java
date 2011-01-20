/*****************************************************************************************
 * @file  BPTree.java
 *
 * @author   Farhan Jiva
 */

import static java.lang.System.out;
import java.util.*;

/*****************************************************************************************
 * This class implements a B+ Tree index structure. 
 */
public class BPTree implements Map
{
	/** The root node of the B+ Tree.
	 */
	public BPNode root;
	
	/** Init variable */
	int init;
	
	/*************************************************************************************
     * Construct an empty B+ Tree. Note that the first node is a leaf and an interal node.
     */
	public BPTree()
	{
		init = 1;
		root = new BPNode();
		root.isleaf = true;
	}
	
    /*************************************************************************************
     * Associates the specified value with the specified key in this map
     * @param key  key with which the specified value is to be associated.
     * @param value  value to be associated with the specified key. 
     */
	public void put(KeyType key, Comparable [] value)
	{
		BPEntry entry = new BPEntry(key, value);
		BPEntry nully = new BPEntry();
		BPNodePointer rootPtr = new BPNodePointer(this.root);
		insert(rootPtr, entry, nully);
	}
	
	/*************************************************************************************
     * The procedure for insertion in a B+Tree. Insert entry *newe into subtree with root
	 * page *subtree (newe and subtree are pointers to nodes). The maximum number of
	 * separators in a page is 2, pushup is null initially and upon return unless the node
	 * pointed to by subtree is split. In the latter case it contains a pointer to the
	 * entry that must be pushed up the tree. If the number of levels in the tree increases,
	 * *subtree is the new root page when the outer level of recursion returns.
	 * @param subtree  pointer to the root of a subtree. 
	 * @param newe     to insert into nodepointer. 
	 * @param pushup   is null initially, null upon return unless child is split. 
     */
	private void insert(BPNodePointer subtree, BPEntry newe, BPEntry pushup)
	{
		/* Manually create the initial stages of the tree */
		/* The algorithm only works on a partially implemented B+ tree */
		if(init <= 3)
		{
			if(root.hasroom())
			{
				insertSortedLeafHasRoom(root, newe);
				init++;
			}
			else
			{
				/* Insert */
				insertSortedLeafHasNoRoom(root, newe);
				/* Split root */
				BPNode L2 = new BPNode();
				L2.isleaf = true;
				
				/* Move the splitting entry into L2 */
				L2.e[0] = new BPEntry(root.e[2].k, root.e[2].v);
				
				/* Remove third entry from root */
				root.e[2].k = null;
				root.e[2].v = null;
				root.e[2].setnull();
				
				/* Create new parent node, this will also be the new root */
				BPNode Nroot = new BPNode();
				Nroot.p0 = root;
				Nroot.e[0] = new BPEntry(L2.e[0].k, L2);
				
				/* Set sibling pointers */
				root.rs = L2;
				L2.ls = root;
				
				/* Repoint root */
				root = Nroot;
				
				init++;
			}
			return;
		}
		
		if(!subtree.ptr.isleaf)
		{
			BPNode N = subtree.ptr;
			
			int i = -1;
			
			if(newe.k.compareTo(N.e[0].k) < 0)
			{
				i = 0;
			}
			else if(N.e[1].isnull)
			{
				i = 1;
			}
			else if(N.e[0].k.compareTo(newe.k) <= 0 && newe.k.compareTo(N.e[1].k) < 0)
			{
				i = 1;
			}
			else
			{
				i = 2;
			}
			switch(i)
			{
				case 0: insert(new BPNodePointer(N.p0), newe, pushup); break;
				case 1: insert(new BPNodePointer(N.e[0].p), newe, pushup); break;
				case 2: insert(new BPNodePointer(N.e[1].p), newe, pushup); break;
				default: out.println("ERROR2: Control should not reach this point"); break;
			}
			
			if(pushup.isnull) return;
			else
			{
				if(N.hasroom()) // recall: N = *subtree
				{
					insertSortedNodeHasRoom(N, pushup);
					pushup.setnull();
					return;
				}
				else // N is full; it has 2 entries
				{
					insertSortedNodeHasNoRoom(N, pushup);
					
					/* Split N */
					BPNode N2 = new BPNode();
					/* Move the splitting entry to N2 */
					N2.e[0].k = N.e[2].k;
					N2.e[0].p = N.e[2].p;
					N2.e[0].unsetnull();
					
					/* Remove third entry from N */
					N.e[2].k = null;
					N.e[2].p = null;
					N.e[2].setnull();
					
					/* Modify pushup for the recursion */
					pushup.k = N2.e[0].k;
					pushup.p = N2;
					pushup.unsetnull();
					
					/* Check if N was root, if so, create another root */
					if(N == root)
					{
						BPNode Nroot = new BPNode();
						
						/* Not entirely sure if this is what I'm suppose to do here */
						Nroot.p0 = N;
						Nroot.e[0] = new BPEntry(pushup.k, pushup.p);
						root = Nroot;
						
						/* Point subtree to Nroot */
						subtree.ptr = Nroot;
					}
				}
			}
		}
		
		if(subtree.ptr.isleaf)
		{
			BPNode L = subtree.ptr;
			
			if(L.hasroom())
			{
				insertSortedLeafHasRoom(L, newe);
				pushup.setnull();
				return;
			}
			else // L is full; it has 2 entries 
			{
				insertSortedLeafHasNoRoom(L, newe);
				
				/* Split L */
				BPNode L2 = new BPNode();
				L2.isleaf = true;
				
				/* Move the splitting entry into L2 */
				L2.e[0] = new BPEntry(L.e[2].k, L.e[2].v);
				
				/* Remove third entry from L */
				L.e[2].k = null;
				L.e[2].v = null;
				L.e[2].setnull();
				
				/* Modify pushup for the recursion */
				pushup.k = L2.e[0].k;
				pushup.p = L2;
				pushup.unsetnull();
				
				/* Set sibling pointers */
				L2.rs = L.rs;
				L.rs = L2;
				L2.ls = L;
				if(L2.rs != null) L2.rs.ls = L2;
			}
		}
	}
	
	/*************************************************************************************
     * Insert the entry e (<k,v>) into leaf node L in sorted order.
     * @param L  the node to insert in. 
     * @param e  the entry to insert. 
     */
	private void insertSortedLeafHasRoom(BPNode L, BPEntry e)
	{
		if(L.e[0].isnull && L.e[1].isnull) // trying to do the first insert
		{
			L.e[0] = new BPEntry(e.k, e.v);
		}
		else if(e.k.compareTo(L.e[0].k) > 0)
		{
			L.e[1] = new BPEntry(e.k, e.v);
		}
		else
		{
			L.e[1] = L.e[0];
			L.e[0] = new BPEntry(e.k, e.v);
		}
	}
	
	/*************************************************************************************
     * Insert the entry e (<k,v>) into leaf node L in sorted order. Note that the max 
	 * entries for a node is 2. This insertion will overflow the node (making it 3). This
	 * assumes that this node will be split shortly after this call.
     * @param L  the node to insert in. 
     * @param e  the entry to insert. 
     */
	private void insertSortedLeafHasNoRoom(BPNode L, BPEntry e)
	{
		BPEntry tmp1 = new BPEntry(L.e[0].k, L.e[0].v);
		BPEntry tmp2 = new BPEntry(L.e[1].k, L.e[1].v);
		BPEntry tmp3 = new BPEntry(e.k, e.v);
		
		BPEntry min = null, mid = null, max = null;
		
		if(tmp1.k.compareTo(tmp2.k) < 0 && tmp1.k.compareTo(tmp3.k) < 0) //tmp1 is smallest
		{
			min = tmp1;
			if(tmp2.k.compareTo(tmp3.k) < 0) //tmp2.k < tmp3.k
			{
				mid = tmp2;
				max = tmp3;
			}
			else //tmp3.k < tmp2.k
			{
				mid = tmp3;
				max = tmp2;
			}
		}
		else if(tmp2.k.compareTo(tmp1.k) < 0 && tmp2.k.compareTo(tmp3.k) < 0) //tmp2 is smallest
		{
			min = tmp2;
			if(tmp1.k.compareTo(tmp3.k) < 0) //tmp1.k < tmp3.k
			{
				mid = tmp1;
				max = tmp3;
			}
			else //tmp3.k < tmp1.k
			{
				mid = tmp3;
				max = tmp1;
			}
		}
		else if(tmp3.k.compareTo(tmp1.k) < 0 && tmp3.k.compareTo(tmp2.k) < 0) //tmp3 is smallest
		{
			min = tmp3;
			if(tmp1.k.compareTo(tmp2.k) < 0) //tmp1.k < tmp2.k
			{
				mid = tmp1;
				max = tmp2;
			}
			else //tmp2.k < tmp1.k
			{
				mid = tmp2;
				max = tmp1;
			}
		}
		else
		{
			//Some of the keys might have been equal, this should not have happened
			out.println("ERROR4: Control should not reach this point");
		}
		
		/* Insert back into L, in sorted order */
		L.e[0] = min; //This entry will stay with L after the split
		L.e[1] = mid; //This entry will stay with L after the split
		L.e[2] = max; //This entry will split away from L
	}
	
	/*************************************************************************************
     * Insert the entry e (<k,p>) into internal node N in sorted order.
     * @param N  the node to insert in. 
     * @param e  the entry to insert. 
     */
	private void insertSortedNodeHasRoom(BPNode N, BPEntry e)
	{
		if(e.k.compareTo(N.e[0].k) > 0)
		{
			N.e[1] = new BPEntry(e.k, e.p);
		}
		else
		{
			N.e[1] = N.e[0]; //might cause issues, probably not
			N.e[0] = new BPEntry(e.k, e.p);
		}
	}
	
	/*************************************************************************************
     * Insert the entry e (<k,p>) into internal node N in sorted order. Note that the max 
	 * entries for a node is 2. This insertion will overflow the node (making it 3). This
	 * assumes that this node will be split shortly after this call.
     * @param N  the node to insert in. 
     * @param e  the entry to insert. 
     */
	private void insertSortedNodeHasNoRoom(BPNode N, BPEntry e)
	{
		BPEntry tmp1 = new BPEntry(N.e[0].k, N.e[0].p);
		BPEntry tmp2 = new BPEntry(N.e[1].k, N.e[1].p);
		BPEntry tmp3 = new BPEntry(e.k, e.p);
		
		BPEntry min = null, mid = null, max = null;
		
		if(tmp1.k.compareTo(tmp2.k) < 0 && tmp1.k.compareTo(tmp3.k) < 0) //tmp1 is smallest
		{
			min = tmp1;
			if(tmp2.k.compareTo(tmp3.k) < 0) //tmp2.k < tmp3.k
			{
				mid = tmp2;
				max = tmp3;
			}
			else //tmp3.k < tmp2.k
			{
				mid = tmp3;
				max = tmp2;
			}
		}
		else if(tmp2.k.compareTo(tmp1.k) < 0 && tmp2.k.compareTo(tmp3.k) < 0) //tmp2 is smallest
		{
			min = tmp2;
			if(tmp1.k.compareTo(tmp3.k) < 0) //tmp1.k < tmp3.k
			{
				mid = tmp1;
				max = tmp3;
			}
			else //tmp3.k < tmp1.k
			{
				mid = tmp3;
				max = tmp1;
			}
		}
		else if(tmp3.k.compareTo(tmp1.k) < 0 && tmp3.k.compareTo(tmp2.k) < 0) //tmp3 is smallest
		{
			min = tmp3;
			if(tmp1.k.compareTo(tmp2.k) < 0) //tmp1.k < tmp2.k
			{
				mid = tmp1;
				max = tmp2;
			}
			else //tmp2.k < tmp1.k
			{
				mid = tmp2;
				max = tmp1;
			}
		}
		else
		{
			//Some of the keys might have been equal, this should not have happened
			out.println("ERROR3: Control should not reach this point");
		}
		
		/* Insert back into N, in sorted order */
		N.e[0] = min; //This entry will stay with N after the split
		N.e[1] = mid; //This entry will stay with N after the split
		N.e[2] = max; //This entry will split away from N
	}
	
    /*************************************************************************************
     * Returns the value to which this map maps the specified key.
	 * Returns null if the map contains no mapping for this key.
     * @param key  key whose associated value is to be returned. 
	 * @return  the value to which this map maps the specified key, or null if the map 
	 *          contains no mapping for this key.
     */
	public Comparable [] get(KeyType key)
	{
		int c = 0;
		
		/* Start from root, iter down to leaves */
		BPNode iter = root;
		
		while(true)
		{
			if(iter.isleaf) // should be this this node
			{
				if(!iter.e[0].isnull)
				{
					if(iter.e[0].k.equals(key)) return iter.e[0].v;
				}
				if(!iter.e[1].isnull)
				{
					if(iter.e[1].k.equals(key)) return iter.e[1].v;
				}
				
				/* Not found, return null */
				return null;
			}
			
			if(key.compareTo(iter.e[0].k) < 0)
			{
				iter = iter.p0;
			}
			else if(iter.e[1].isnull)
			{
				iter = iter.e[0].p;
			}
			else if(key.compareTo(iter.e[1].k) < 0)
			{
				iter = iter.e[0].p;
			}
			else
			{
				iter = iter.e[1].p;
			}
		}
	}
	
    /*************************************************************************************
     * Returns the (leaf) BPNode which contains the key.
	 * Returns null if the map contains no mapping for this key.
     * @param key  key whose associated BPNode is to be returned. 
	 * @return  the BPNode to which this map maps the specified key, or null if the map 
	 *          contains no mapping for this key.
     */
	public BPNode getnode(KeyType key)
	{
		int c = 0;
		
		/* Start from root, iter down to leaves */
		BPNode iter = root;
		
		while(true)
		{
			if(iter.isleaf) // should be this this node
			{
				if(!iter.e[0].isnull)
				{
					if(iter.e[0].k.equals(key)) return iter;
				}
				if(!iter.e[1].isnull)
				{
					if(iter.e[1].k.equals(key)) return iter;
				}
				
				/* Not found, return null */
				return null;
			}
			
			if(key.compareTo(iter.e[0].k) < 0)
			{
				iter = iter.p0;
			}
			else if(iter.e[1].isnull)
			{
				iter = iter.e[0].p;
			}
			else if(key.compareTo(iter.e[1].k) < 0)
			{
				iter = iter.e[0].p;
			}
			else
			{
				iter = iter.e[1].p;
			}
		}
	}
	
	/*****************************************************************************************
	 * This class implements a node object for the B+ Tree. An internal node has the form 
	 * (p0, <k1,p1>, <k2,p2>), where <k,p> is a BPEntry object. A leaf node has the form
	 * (<k1,v1>, <k2,v2>) where <k,v> is a BPEntry object.
	 */
	class BPNode
	{
		/** True if this node is a leaf, false otherwise. */
		public boolean isleaf;
		
		/** The leftmost child pointer */
		public BPNode p0;
		
		/** An array of BPEntry objects */
		public BPEntry [] e;
		
		/** The siblings of the node. */
		public BPNode ls, rs;
		
		/*************************************************************************************
		 * Construct an empty node and initialize variables.
		 */
		public BPNode()
		{
			isleaf = false;
			p0 = null;
			e = new BPEntry[3];
			e[0] = new BPEntry(); //Associated with k1
			e[1] = new BPEntry(); //Associated with k2
			e[2] = new BPEntry(); //Used during a split, not normally part of this node
			ls = null;
			rs = null;
		}
		
		/*************************************************************************************
		 * Checks if this node has room for a key
		 * @return whether or not this node has room for a key.
		 */
		public boolean hasroom()
		{
			if(e[0].isnull || e[1].isnull) return true;
			return false;
		}
		
		/*************************************************************************************
		 * Returns the value associated with this node's key (either at position 1 or 2).
		 * This method can only be applied to leaf nodes.
		 * @param keypos  the position of the key. 
		 * @return the Comparable [] tuple pointed to by the key, or null.
		 */
		public Comparable [] getdirectvalue(int keypos)
		{
			if(!isleaf)
			{
				return null;
			}
			
			if(keypos == 1 || keypos == 2)
			{
				if(e[keypos-1].isnull)
				{
					return null;
				}
				else
				{
					return e[keypos-1].v;
				}
			}
			
			return null; //keypos was invalid
		}
		
		/*************************************************************************************
		 * Returns the right sibling of the current node.
		 * This method can only be applied to leaf nodes.
		 * @return the BPNode pointed to by rs.
		 */
		public BPNode next()
		{
			return rs;
		}
		
		/*************************************************************************************
		 * Returns the left sibling of the current node.
		 * This method can only be applied to leaf nodes.
		 * @return the BPNode pointed to by ls.
		 */
		public BPNode prev()
		{
			return ls;
		}
	}
	
	/*****************************************************************************************
	 * Because Java is not pass-by-referece (does not allow the address of a method
	 * variable to be modified), I have to perform this little hack to imitate passing-by
	 * -reference.
	 */
	class BPNodePointer
	{
		/** The BPNode object being pointed to */
		public BPNode ptr;
		
		/*************************************************************************************
		 * Construct a BPNodePointer object.
		 * @param _ptr  the BPNode you want a pointer for. 
		 */
		public BPNodePointer(BPNode _ptr)
		{
			this.ptr = _ptr;
		}
	}
	
	/*************************************************************************************
     * This class implements a BPEntry object. An entry in the index has the form <k,p> or
	 * <k,v>, where k is a search-key value, p is a pointer, and v is a value.
     */
	class BPEntry
	{
		/** Used because Java is pass-by-value and not reference */
		public boolean isnull;
		
		/** The key being carried */
		public KeyType k;
		
		/** The value being carried */
		public Comparable [] v;
		
		/** The BPNode pointer being carried */
		public BPNode p;
		
		/*************************************************************************************
		 * Construct an empty BPEntry object.
		 * @param _key  the key to carry. 
		 * @param _value  the value to carry. 
		 */
		public BPEntry(KeyType _key, Comparable [] _value)
		{
			isnull = false;
			k = _key;
			v = _value;
			p = null;
		}
		
		/*************************************************************************************
		 * Construct an empty BPEntry object.
		 * @param _key  the key to carry. 
		 * @param _pointer  the pointer to carry. 
		 */
		public BPEntry(KeyType _key, BPNode _pointer)
		{
			isnull = false;
			k = _key;
			v = null;
			p = _pointer;
		}
		
		/*************************************************************************************
		 * Construct an empty BPEntry object.
		 */
		public BPEntry()
		{
			isnull = true;
			k = null;
			v = null;
			p = null;
		}
		
		/** Set the null variable to true */
		public void setnull()
		{
			isnull = true;
		}
		
		/** Set the null variable to false */
		public void unsetnull()
		{
			isnull = false;
		}
	}
	
	/** Extended from Map, not implemented */
	public void clear()
	{
		//
	}
	
	/** Extended from Map, not implemented */
	public boolean containsKey(Object key)
	{
		return false;
	}
	
	/** Extended from Map, not implemented */
	public boolean containsValue(Object value)
	{
		return false;
	}
	
	/** Extended from Map, not implemented */
	public boolean equals(Object o)
	{
		return false;
	}
	
	/** Extended from Map, not implemented */
	public int hashCode()
	{
		return 0;
	}
	
	/** Extended from Map, not implemented */
	public boolean isEmpty()
	{
		return false;
	}
	
	/** Extended from Map, not implemented */
	public Set keySet()
	{
		return null;
	}
	
	/** Extended from Map, not implemented */
	public void putAll(Map t)
	{
		//
	}
	
	/** Extended from Map, not implemented */
	public Object remove(Object key)
	{
		return null;
	}
	
	/** Extended from Map, not implemented */
	public int size()
	{
		return 0;
	}
	
	/** Extended from Map, not implemented */
	public Collection values()
	{
		return null;
	}
	
	/** Extended from Map, not implemented */
	public Set entrySet()
	{
		return null;
	}
	
	/** Extended from Map, not implemented */
	public Object put(Object key, Object value)
	{
		return null;
	}
	
	/** Extended from Map, not implemented */
	public Object get(Object key)
	{
		return null;
	}
	
    /*************************************************************************************
     * The main method is used for testing purposes only.
     * @param args  the command-line arguments
     */
	public static void main(String [] args)
	{
		/* Create tuples to use with keytype, using only primary key values for the content of the tuple */
		// Comparable [] t1 = { "A", 1980};
		// Comparable [] t2 = { "B", 1981};
		// Comparable [] t3 = { "C", 1982 };
		// Comparable [] t4 = { "D", 1983 };
		// Comparable [] t5 = { "E", 1984 };
		
		Comparable [] tuple1 = { "A", 1980, 124, "T", "Fox", 12345 };
		Comparable [] tuple2 = { "B", 1981, 200, "T", "Universal", 12125 };
		Comparable [] tuple3 = { "C", 1982, 200, "T", "Universal", 12125 };
		Comparable [] tuple4 = { "D", 1983, 200, "T", "Universal", 12125 };
		Comparable [] tuple5 = { "E", 1984, 200, "T", "Universal", 12125 };
		Comparable [] tuple6 = { "F", 1985, 200, "T", "Universal", 12125 };
		Comparable [] tuple7 = { "G", 1986, 200, "T", "Universal", 12125 };
		Comparable [] tuple8 = { "H", 1987, 200, "T", "Universal", 12125 };
		Comparable [] tuple9 = { "I", 1988, 200, "T", "Universal", 12125 };
		Comparable [] tuple10 = { "J", 1989, 200, "T", "Universal", 12125 };
		Comparable [] tuple11 = { "K", 1990, 200, "T", "Universal", 12125 };
		Comparable [] tuple12 = { "L", 1991, 200, "T", "Universal", 12125 };
		Comparable [] tuple13 = { "M", 1992, 200, "T", "Universal", 12125 };
		Comparable [] tuple14 = { "N", 1993, 200, "T", "Universal", 12125 };
		Comparable [] tuple15 = { "O", 1994, 200, "T", "Universal", 12125 };
		Comparable [] tuple16 = { "P", 1995, 200, "T", "Universal", 12125 };
		Comparable [] tuple17 = { "Q", 1996, 200, "T", "Universal", 12125 };
		Comparable [] tuple18 = { "R", 1997, 200, "T", "Universal", 12125 };
		Comparable [] tuple19 = { "S", 1998, 200, "T", "Universal", 12125 };
		Comparable [] tuple20 = { "T", 1999, 200, "T", "Universal", 12125 };
		Comparable [] tuple21 = { "U", 2000, 200, "T", "Universal", 12125 };
		Comparable [] tuple22 = { "V", 2001, 200, "T", "Universal", 12125 };
		Comparable [] tuple23 = { "W", 2002, 200, "T", "Universal", 12125 };
		Comparable [] tuple24 = { "X", 2003, 200, "T", "Universal", 12125 };
		Comparable [] tuple25 = { "Y", 2004, 200, "T", "Universal", 12125 };
		Comparable [] tuple26 = { "Z", 2005, 200, "T", "Universal", 12125 };
		
		Comparable [] tuple27 = { "AA", 1980, 124, "T", "Fox", 12345 };
		Comparable [] tuple28 = { "BB", 1981, 200, "T", "Universal", 12125 };
		Comparable [] tuple29 = { "CC", 1982, 200, "T", "Universal", 12125 };
		Comparable [] tuple30 = { "DD", 1983, 200, "T", "Universal", 12125 };
		Comparable [] tuple31 = { "EE", 1984, 200, "T", "Universal", 12125 };
		Comparable [] tuple32 = { "FF", 1985, 200, "T", "Universal", 12125 };
		Comparable [] tuple33 = { "GG", 1986, 200, "T", "Universal", 12125 };
		Comparable [] tuple34 = { "HH", 1987, 200, "T", "Universal", 12125 };
		Comparable [] tuple35 = { "II", 1988, 200, "T", "Universal", 12125 };
		Comparable [] tuple36 = { "JJ", 1989, 200, "T", "Universal", 12125 };
		Comparable [] tuple37 = { "KK", 1990, 200, "T", "Universal", 12125 };
		Comparable [] tuple38 = { "LL", 1991, 200, "T", "Universal", 12125 };
		Comparable [] tuple39 = { "MM", 1992, 200, "T", "Universal", 12125 };
		Comparable [] tuple40 = { "NN", 1993, 200, "T", "Universal", 12125 };
		Comparable [] tuple41 = { "OO", 1994, 200, "T", "Universal", 12125 };
		Comparable [] tuple42 = { "PP", 1995, 200, "T", "Universal", 12125 };
		Comparable [] tuple43 = { "QQ", 1996, 200, "T", "Universal", 12125 };
		Comparable [] tuple44 = { "RR", 1997, 200, "T", "Universal", 12125 };
		Comparable [] tuple45 = { "SS", 1998, 200, "T", "Universal", 12125 };
		Comparable [] tuple46 = { "TT", 1999, 200, "T", "Universal", 12125 };
		Comparable [] tuple47 = { "UU", 2000, 200, "T", "Universal", 12125 };
		Comparable [] tuple48 = { "VV", 2001, 200, "T", "Universal", 12125 };
		Comparable [] tuple49 = { "WW", 2002, 200, "T", "Universal", 12125 };
		Comparable [] tuple50 = { "XX", 2003, 200, "T", "Universal", 12125 };
		Comparable [] tuple51 = { "YY", 2004, 200, "T", "Universal", 12125 };
		Comparable [] tuple52 = { "ZZ", 2005, 200, "T", "Universal", 12125 };
		
		Comparable [] tuple53 = { "AAA", 1980, 124, "T", "Fox", 12345 };
		Comparable [] tuple54 = { "BBB", 1981, 200, "T", "Universal", 12125 };
		Comparable [] tuple55 = { "CCC", 1982, 200, "T", "Universal", 12125 };
		Comparable [] tuple56 = { "DDD", 1983, 200, "T", "Universal", 12125 };
		Comparable [] tuple57 = { "EEE", 1984, 200, "T", "Universal", 12125 };
		Comparable [] tuple58 = { "FFF", 1985, 200, "T", "Universal", 12125 };
		Comparable [] tuple59 = { "GGG", 1986, 200, "T", "Universal", 12125 };
		Comparable [] tuple60 = { "HHH", 1987, 200, "T", "Universal", 12125 };
		Comparable [] tuple61 = { "III", 1988, 200, "T", "Universal", 12125 };
		Comparable [] tuple62 = { "JJJ", 1989, 200, "T", "Universal", 12125 };
		Comparable [] tuple63 = { "KKK", 1990, 200, "T", "Universal", 12125 };
		Comparable [] tuple64 = { "LLL", 1991, 200, "T", "Universal", 12125 };
		Comparable [] tuple65 = { "MMM", 1992, 200, "T", "Universal", 12125 };
		Comparable [] tuple66 = { "NNN", 1993, 200, "T", "Universal", 12125 };
		Comparable [] tuple67 = { "OOO", 1994, 200, "T", "Universal", 12125 };
		Comparable [] tuple68 = { "PPP", 1995, 200, "T", "Universal", 12125 };
		Comparable [] tuple69 = { "QQQ", 1996, 200, "T", "Universal", 12125 };
		Comparable [] tuple70 = { "RRR", 1997, 200, "T", "Universal", 12125 };
		Comparable [] tuple71 = { "SSS", 1998, 200, "T", "Universal", 12125 };
		Comparable [] tuple72 = { "TTT", 1999, 200, "T", "Universal", 12125 };
		Comparable [] tuple73 = { "UUU", 2000, 200, "T", "Universal", 12125 };
		Comparable [] tuple74 = { "VVV", 2001, 200, "T", "Universal", 12125 };
		Comparable [] tuple75 = { "WWW", 2002, 200, "T", "Universal", 12125 };
		Comparable [] tuple76 = { "XXX", 2003, 200, "T", "Universal", 12125 };
		Comparable [] tuple77 = { "YYY", 2004, 200, "T", "Universal", 12125 };
		Comparable [] tuple78 = { "ZZZ", 2005, 200, "T", "Universal", 12125 };
		
		Comparable [] tuple79 = { "AAAA", 1980, 124, "T", "Fox", 12345 };
		Comparable [] tuple80 = { "BBBB", 1981, 200, "T", "Universal", 12125 };
		Comparable [] tuple81 = { "CCCC", 1982, 200, "T", "Universal", 12125 };
		Comparable [] tuple82 = { "DDDD", 1983, 200, "T", "Universal", 12125 };
		Comparable [] tuple83 = { "EEEE", 1984, 200, "T", "Universal", 12125 };
		Comparable [] tuple84 = { "FFFF", 1985, 200, "T", "Universal", 12125 };
		Comparable [] tuple85 = { "GGGG", 1986, 200, "T", "Universal", 12125 };
		Comparable [] tuple86 = { "HHHH", 1987, 200, "T", "Universal", 12125 };
		Comparable [] tuple87 = { "IIII", 1988, 200, "T", "Universal", 12125 };
		Comparable [] tuple88 = { "JJJJ", 1989, 200, "T", "Universal", 12125 };
		Comparable [] tuple89 = { "KKKK", 1990, 200, "T", "Universal", 12125 };
		Comparable [] tuple90 = { "LLLL", 1991, 200, "T", "Universal", 12125 };
		Comparable [] tuple91 = { "MMMM", 1992, 200, "T", "Universal", 12125 };
		Comparable [] tuple92 = { "NNNN", 1993, 200, "T", "Universal", 12125 };
		Comparable [] tuple93 = { "OOOO", 1994, 200, "T", "Universal", 12125 };
		Comparable [] tuple94 = { "PPPP", 1995, 200, "T", "Universal", 12125 };
		Comparable [] tuple95 = { "QQQQ", 1996, 200, "T", "Universal", 12125 };
		Comparable [] tuple96 = { "RRRR", 1997, 200, "T", "Universal", 12125 };
		Comparable [] tuple97 = { "SSSS", 1998, 200, "T", "Universal", 12125 };
		Comparable [] tuple98 = { "TTTT", 1999, 200, "T", "Universal", 12125 };
		Comparable [] tuple99 = { "UUUU", 2000, 200, "T", "Universal", 12125 };
		Comparable [] tuple100 = { "VVVV", 2001, 200, "T", "Universal", 12125 };
		Comparable [] tuple101 = { "WWWW", 2002, 200, "T", "Universal", 12125 };
		Comparable [] tuple102 = { "XXXX", 2003, 200, "T", "Universal", 12125 };
		Comparable [] tuple103 = { "YYYY", 2004, 200, "T", "Universal", 12125 };
		Comparable [] tuple104 = { "ZZZZ", 2005, 200, "T", "Universal", 12125 };
		
		/* Create KeyType objects with our special key-only tuples */
		// KeyType key1 = new KeyType (t1, 2);
		// KeyType key2 = new KeyType (t2, 2);
		// KeyType key3 = new KeyType (t3, 2);
		// KeyType key4 = new KeyType (t4, 2);
		// KeyType key5 = new KeyType (t5, 2);
		
		KeyType key1 = new KeyType (tuple1, 2);
		KeyType key2 = new KeyType (tuple2, 2);
		KeyType key3 = new KeyType (tuple3, 2);
		KeyType key4 = new KeyType (tuple4, 2);
		KeyType key5 = new KeyType (tuple5, 2);
		KeyType key6 = new KeyType (tuple6, 2);
		KeyType key7 = new KeyType (tuple7, 2);
		KeyType key8 = new KeyType (tuple8, 2);
		KeyType key9 = new KeyType (tuple9, 2);
		KeyType key10 = new KeyType (tuple10, 2);
		KeyType key11 = new KeyType (tuple11, 2);
		KeyType key12 = new KeyType (tuple12, 2);
		KeyType key13 = new KeyType (tuple13, 2);
		KeyType key14 = new KeyType (tuple14, 2);
		KeyType key15 = new KeyType (tuple15, 2);
		KeyType key16 = new KeyType (tuple16, 2);
		KeyType key17 = new KeyType (tuple17, 2);
		KeyType key18 = new KeyType (tuple18, 2);
		KeyType key19 = new KeyType (tuple19, 2);
		KeyType key20 = new KeyType (tuple20, 2);
		KeyType key21 = new KeyType (tuple21, 2);
		KeyType key22 = new KeyType (tuple22, 2);
		KeyType key23 = new KeyType (tuple23, 2);
		KeyType key24 = new KeyType (tuple24, 2);
		KeyType key25 = new KeyType (tuple25, 2);
		KeyType key26 = new KeyType (tuple26, 2);
		
		KeyType key27 = new KeyType (tuple27, 2);
		KeyType key28 = new KeyType (tuple28, 2);
		KeyType key29 = new KeyType (tuple29, 2);
		KeyType key30 = new KeyType (tuple30, 2);
		KeyType key31 = new KeyType (tuple31, 2);
		KeyType key32 = new KeyType (tuple32, 2);
		KeyType key33 = new KeyType (tuple33, 2);
		KeyType key34 = new KeyType (tuple34, 2);
		KeyType key35 = new KeyType (tuple35, 2);
		KeyType key36 = new KeyType (tuple36, 2);
		KeyType key37 = new KeyType (tuple37, 2);
		KeyType key38 = new KeyType (tuple38, 2);
		KeyType key39 = new KeyType (tuple39, 2);
		KeyType key40 = new KeyType (tuple40, 2);
		KeyType key41 = new KeyType (tuple41, 2);
		KeyType key42 = new KeyType (tuple42, 2);
		KeyType key43 = new KeyType (tuple43, 2);
		KeyType key44 = new KeyType (tuple44, 2);
		KeyType key45 = new KeyType (tuple45, 2);
		KeyType key46 = new KeyType (tuple46, 2);
		KeyType key47 = new KeyType (tuple47, 2);
		KeyType key48 = new KeyType (tuple48, 2);
		KeyType key49 = new KeyType (tuple49, 2);
		KeyType key50 = new KeyType (tuple50, 2);
		KeyType key51 = new KeyType (tuple51, 2);
		KeyType key52 = new KeyType (tuple52, 2);
		
		KeyType key53 = new KeyType (tuple53, 2);
		KeyType key54 = new KeyType (tuple54, 2);
		KeyType key55 = new KeyType (tuple55, 2);
		KeyType key56 = new KeyType (tuple56, 2);
		KeyType key57 = new KeyType (tuple57, 2);
		KeyType key58 = new KeyType (tuple58, 2);
		KeyType key59 = new KeyType (tuple59, 2);
		KeyType key60 = new KeyType (tuple60, 2);
		KeyType key61 = new KeyType (tuple61, 2);
		KeyType key62 = new KeyType (tuple62, 2);
		KeyType key63 = new KeyType (tuple63, 2);
		KeyType key64 = new KeyType (tuple64, 2);
		KeyType key65 = new KeyType (tuple65, 2);
		KeyType key66 = new KeyType (tuple66, 2);
		KeyType key67 = new KeyType (tuple67, 2);
		KeyType key68 = new KeyType (tuple68, 2);
		KeyType key69 = new KeyType (tuple69, 2);
		KeyType key70 = new KeyType (tuple70, 2);
		KeyType key71 = new KeyType (tuple71, 2);
		KeyType key72 = new KeyType (tuple72, 2);
		KeyType key73 = new KeyType (tuple73, 2);
		KeyType key74 = new KeyType (tuple74, 2);
		KeyType key75 = new KeyType (tuple75, 2);
		KeyType key76 = new KeyType (tuple76, 2);
		KeyType key77 = new KeyType (tuple77, 2);
		KeyType key78 = new KeyType (tuple78, 2);
		
		KeyType key79 = new KeyType (tuple79, 2);
		KeyType key80 = new KeyType (tuple80, 2);
		KeyType key81 = new KeyType (tuple81, 2);
		KeyType key82 = new KeyType (tuple82, 2);
		KeyType key83 = new KeyType (tuple83, 2);
		KeyType key84 = new KeyType (tuple84, 2);
		KeyType key85 = new KeyType (tuple85, 2);
		KeyType key86 = new KeyType (tuple86, 2);
		KeyType key87 = new KeyType (tuple87, 2);
		KeyType key88 = new KeyType (tuple88, 2);
		KeyType key89 = new KeyType (tuple89, 2);
		KeyType key90 = new KeyType (tuple90, 2);
		KeyType key91 = new KeyType (tuple91, 2);
		KeyType key92 = new KeyType (tuple92, 2);
		KeyType key93 = new KeyType (tuple93, 2);
		KeyType key94 = new KeyType (tuple94, 2);
		KeyType key95 = new KeyType (tuple95, 2);
		KeyType key96 = new KeyType (tuple96, 2);
		KeyType key97 = new KeyType (tuple97, 2);
		KeyType key98 = new KeyType (tuple98, 2);
		KeyType key99 = new KeyType (tuple99, 2);
		KeyType key100 = new KeyType (tuple100, 2);
		KeyType key101 = new KeyType (tuple101, 2);
		KeyType key102 = new KeyType (tuple102, 2);
		KeyType key103 = new KeyType (tuple103, 2);
		KeyType key104 = new KeyType (tuple104, 2);
		
		/* Insert keys/tuples randomly */
		
		Random r = new Random();
		
		BPTree tree = new BPTree();
		
		int [] pos = new int[104];
		for(int i = 0; i < 104; i++)
		{
			pos[i] = i;
		}
		
		int count = 0;
		while(count < 104)
		{
			int j = r.nextInt(104);
			
			if(pos[j] != -1)
			{
				pos[j] = -1;
				count++;
				KeyType tmp = null;
				// if(j==0){ tree.put(key1, t1); tmp = key1;}
				// if(j==1){ tree.put(key2, t2); tmp = key2;}
				// if(j==2){ tree.put(key3, t3); tmp = key3;}
				// if(j==3){ tree.put(key4, t4); tmp = key4;}
				// if(j==4){ tree.put(key5, t5); tmp = key5;}
				
				if(j==0){ tree.put(key1, tuple1); tmp = key1;}
				if(j==1){ tree.put(key2, tuple2); tmp = key2;}
				if(j==2){ tree.put(key3, tuple3); tmp = key3;}
				if(j==3){ tree.put(key4, tuple4); tmp = key4;}
				if(j==4){ tree.put(key5, tuple5); tmp = key5;}
				if(j==5){ tree.put(key6, tuple6); tmp = key6;}
				if(j==6){ tree.put(key7, tuple7); tmp = key7;}
				if(j==7){ tree.put(key8, tuple8); tmp = key8;}
				if(j==8){ tree.put(key9, tuple9); tmp = key9;}
				if(j==9){ tree.put(key10, tuple10); tmp = key10;}
				if(j==10){ tree.put(key11, tuple11); tmp = key11;}
				if(j==11){ tree.put(key12, tuple12); tmp = key12;}
				if(j==12){ tree.put(key13, tuple13); tmp = key13;}
				if(j==13){ tree.put(key14, tuple14); tmp = key14;}
				if(j==14){ tree.put(key15, tuple15); tmp = key15;}
				if(j==15){ tree.put(key16, tuple16); tmp = key16;}
				if(j==16){ tree.put(key17, tuple17); tmp = key17;}
				if(j==17){ tree.put(key18, tuple18); tmp = key18;}
				if(j==18){ tree.put(key19, tuple19); tmp = key19;}
				if(j==19){ tree.put(key20, tuple20); tmp = key20;}
				if(j==20){ tree.put(key21, tuple21); tmp = key21;}
				if(j==21){ tree.put(key22, tuple22); tmp = key22;}
				if(j==22){ tree.put(key23, tuple23); tmp = key23;}
				if(j==23){ tree.put(key24, tuple24); tmp = key24;}
				if(j==24){ tree.put(key25, tuple25); tmp = key25;}
				if(j==25){ tree.put(key26, tuple26); tmp = key26;}
				if(j==26){ tree.put(key27, tuple27); tmp = key27;}
				
				if(j==27){ tree.put(key28, tuple28); tmp = key28;}
				if(j==28){ tree.put(key29, tuple29); tmp = key29;}
				if(j==29){ tree.put(key30, tuple30); tmp = key30;}
				if(j==30){ tree.put(key31, tuple31); tmp = key31;}
				if(j==31){ tree.put(key32, tuple32); tmp = key32;}
				if(j==32){ tree.put(key33, tuple33); tmp = key33;}
				if(j==33){ tree.put(key34, tuple34); tmp = key34;}
				if(j==34){ tree.put(key35, tuple35); tmp = key35;}
				if(j==35){ tree.put(key36, tuple36); tmp = key36;}
				if(j==36){ tree.put(key37, tuple37); tmp = key37;}
				if(j==37){ tree.put(key38, tuple38); tmp = key38;}
				if(j==38){ tree.put(key39, tuple39); tmp = key39;}
				if(j==39){ tree.put(key40, tuple40); tmp = key40;}
				if(j==40){ tree.put(key41, tuple41); tmp = key41;}
				if(j==41){ tree.put(key42, tuple42); tmp = key42;}
				if(j==42){ tree.put(key43, tuple43); tmp = key43;}
				if(j==43){ tree.put(key44, tuple44); tmp = key44;}
				if(j==44){ tree.put(key45, tuple45); tmp = key45;}
				if(j==45){ tree.put(key46, tuple46); tmp = key46;}
				if(j==46){ tree.put(key47, tuple47); tmp = key47;}
				if(j==47){ tree.put(key48, tuple48); tmp = key48;}
				if(j==48){ tree.put(key49, tuple49); tmp = key49;}
				if(j==49){ tree.put(key50, tuple50); tmp = key50;}
				if(j==50){ tree.put(key51, tuple51); tmp = key51;}
				if(j==51){ tree.put(key52, tuple52); tmp = key52;}
				if(j==52){ tree.put(key53, tuple53); tmp = key53;}
				if(j==53){ tree.put(key54, tuple54); tmp = key54;}
				if(j==54){ tree.put(key55, tuple55); tmp = key55;}
				if(j==55){ tree.put(key56, tuple56); tmp = key56;}
				if(j==56){ tree.put(key57, tuple57); tmp = key57;}
				if(j==57){ tree.put(key58, tuple58); tmp = key58;}
				if(j==58){ tree.put(key59, tuple59); tmp = key59;}
				if(j==59){ tree.put(key60, tuple60); tmp = key60;}
				if(j==60){ tree.put(key61, tuple61); tmp = key61;}
				if(j==61){ tree.put(key62, tuple62); tmp = key62;}
				if(j==62){ tree.put(key63, tuple63); tmp = key63;}
				if(j==63){ tree.put(key64, tuple64); tmp = key64;}
				if(j==64){ tree.put(key65, tuple65); tmp = key65;}
				if(j==65){ tree.put(key66, tuple66); tmp = key66;}
				if(j==66){ tree.put(key67, tuple67); tmp = key67;}
				if(j==67){ tree.put(key68, tuple68); tmp = key68;}
				if(j==68){ tree.put(key69, tuple69); tmp = key69;}
				if(j==69){ tree.put(key70, tuple70); tmp = key70;}
				if(j==70){ tree.put(key71, tuple71); tmp = key71;}
				if(j==71){ tree.put(key72, tuple72); tmp = key72;}
				if(j==72){ tree.put(key73, tuple73); tmp = key73;}
				if(j==73){ tree.put(key74, tuple74); tmp = key74;}
				if(j==74){ tree.put(key75, tuple75); tmp = key75;}
				if(j==75){ tree.put(key76, tuple76); tmp = key76;}
				if(j==76){ tree.put(key77, tuple77); tmp = key77;}
				if(j==77){ tree.put(key78, tuple78); tmp = key78;}
				if(j==78){ tree.put(key79, tuple79); tmp = key79;}
				if(j==79){ tree.put(key80, tuple80); tmp = key80;}
				if(j==80){ tree.put(key81, tuple81); tmp = key81;}
				if(j==81){ tree.put(key82, tuple82); tmp = key82;}
				if(j==82){ tree.put(key83, tuple83); tmp = key83;}
				if(j==83){ tree.put(key84, tuple84); tmp = key84;}
				if(j==84){ tree.put(key85, tuple85); tmp = key85;}
				if(j==85){ tree.put(key86, tuple86); tmp = key86;}
				if(j==86){ tree.put(key87, tuple87); tmp = key87;}
				if(j==87){ tree.put(key88, tuple88); tmp = key88;}
				if(j==88){ tree.put(key89, tuple89); tmp = key89;}
				if(j==89){ tree.put(key90, tuple90); tmp = key90;}
				if(j==90){ tree.put(key91, tuple91); tmp = key91;}
				if(j==91){ tree.put(key92, tuple92); tmp = key92;}
				if(j==92){ tree.put(key93, tuple93); tmp = key93;}
				if(j==93){ tree.put(key94, tuple94); tmp = key94;}
				if(j==94){ tree.put(key95, tuple95); tmp = key95;}
				if(j==95){ tree.put(key96, tuple96); tmp = key96;}
				if(j==96){ tree.put(key97, tuple97); tmp = key97;}
				if(j==97){ tree.put(key98, tuple98); tmp = key98;}
				if(j==98){ tree.put(key99, tuple99); tmp = key99;}
				if(j==99){ tree.put(key100, tuple100); tmp = key100;}
				if(j==100){ tree.put(key101, tuple101); tmp = key101;}
				if(j==101){ tree.put(key102, tuple102); tmp = key102;}
				if(j==102){ tree.put(key103, tuple103); tmp = key103;}
				if(j==103){ tree.put(key104, tuple104); tmp = key104;}
				
				if(tmp != null) out.println("Just inserted: " + tmp);
			}
		}
		
		/* Find the records */
		// out.println(tree.get(key1)[0]);
		// out.println(tree.get(key2)[0]);
		// out.println(tree.get(key3)[0]);
		// out.println(tree.get(key4)[0]);
		// out.println(tree.get(key5)[0]);
		// out.println(tree.get(key6)[0]);
		// out.println(tree.get(key7)[0]);
		// out.println(tree.get(key8)[0]);
		// out.println(tree.get(key9)[0]);
		// out.println(tree.get(key10)[0]);
		// out.println(tree.get(key11)[0]);
		// out.println(tree.get(key12)[0]);
		// out.println(tree.get(key13)[0]);
		// out.println(tree.get(key14)[0]);
		// out.println(tree.get(key15)[0]);
		// out.println(tree.get(key16)[0]);
		// out.println(tree.get(key17)[0]);
		// out.println(tree.get(key18)[0]);
		// out.println(tree.get(key19)[0]);
		// out.println(tree.get(key20)[0]);
		// out.println(tree.get(key21)[0]);
		// out.println(tree.get(key22)[0]);
		// out.println(tree.get(key23)[0]);
		// out.println(tree.get(key24)[0]);
		// out.println(tree.get(key25)[0]);
		// out.println(tree.get(key26)[0]);
		// out.println(tree.get(key27)[0]);
		// out.println(tree.get(key28)[0]);
		// out.println(tree.get(key29)[0]);
		// out.println(tree.get(key30)[0]);
		// out.println(tree.get(key31)[0]);
		// out.println(tree.get(key32)[0]);
		// out.println(tree.get(key33)[0]);
		// out.println(tree.get(key34)[0]);
		// out.println(tree.get(key35)[0]);
		// out.println(tree.get(key36)[0]);
		// out.println(tree.get(key37)[0]);
		// out.println(tree.get(key38)[0]);
		// out.println(tree.get(key39)[0]);
		// out.println(tree.get(key40)[0]);
		// out.println(tree.get(key41)[0]);
		// out.println(tree.get(key42)[0]);
		// out.println(tree.get(key43)[0]);
		// out.println(tree.get(key44)[0]);
		// out.println(tree.get(key45)[0]);
		// out.println(tree.get(key46)[0]);
		// out.println(tree.get(key47)[0]);
		// out.println(tree.get(key48)[0]);
		// out.println(tree.get(key49)[0]);
		// out.println(tree.get(key50)[0]);
		// out.println(tree.get(key51)[0]);
		// out.println(tree.get(key52)[0]);
		// out.println(tree.get(key53)[0]);
		// out.println(tree.get(key54)[0]);
		// out.println(tree.get(key55)[0]);
		// out.println(tree.get(key56)[0]);
		// out.println(tree.get(key57)[0]);
		// out.println(tree.get(key58)[0]);
		// out.println(tree.get(key59)[0]);
		// out.println(tree.get(key60)[0]);
		// out.println(tree.get(key61)[0]);
		// out.println(tree.get(key62)[0]);
		// out.println(tree.get(key63)[0]);
		// out.println(tree.get(key64)[0]);
		// out.println(tree.get(key65)[0]);
		// out.println(tree.get(key66)[0]);
		// out.println(tree.get(key67)[0]);
		// out.println(tree.get(key68)[0]);
		// out.println(tree.get(key69)[0]);
		// out.println(tree.get(key70)[0]);
		// out.println(tree.get(key71)[0]);
		// out.println(tree.get(key72)[0]);
		// out.println(tree.get(key73)[0]);
		// out.println(tree.get(key74)[0]);
		// out.println(tree.get(key75)[0]);
		// out.println(tree.get(key76)[0]);
		// out.println(tree.get(key77)[0]);
		// out.println(tree.get(key78)[0]);
		// out.println(tree.get(key79)[0]);
		// out.println(tree.get(key80)[0]);
		// out.println(tree.get(key81)[0]);
		// out.println(tree.get(key82)[0]);
		// out.println(tree.get(key83)[0]);
		// out.println(tree.get(key84)[0]);
		// out.println(tree.get(key85)[0]);
		// out.println(tree.get(key86)[0]);
		// out.println(tree.get(key87)[0]);
		// out.println(tree.get(key88)[0]);
		// out.println(tree.get(key89)[0]);
		// out.println(tree.get(key90)[0]);
		// out.println(tree.get(key91)[0]);
		// out.println(tree.get(key92)[0]);
		// out.println(tree.get(key93)[0]);
		// out.println(tree.get(key94)[0]);
		// out.println(tree.get(key95)[0]);
		// out.println(tree.get(key96)[0]);
		// out.println(tree.get(key97)[0]);
		// out.println(tree.get(key98)[0]);
		// out.println(tree.get(key99)[0]);
		// out.println(tree.get(key100)[0]);
		// out.println(tree.get(key101)[0]);
		// out.println(tree.get(key102)[0]);
		// out.println(tree.get(key103)[0]);
		// out.println(tree.get(key104)[0]);
		
		/* Find the records, in a range-search manner */
		// BPNode start = tree.getnode(key1); // Should return "A"
		// BPNode end = tree.getnode(key104); // Should return "ZZZZ"
		// out.println(start.getdirectvalue(1)[0]);
		// out.println(end.getdirectvalue(1)[0]);
		// out.println(end.getdirectvalue(2)[0]);
		
		out.println("Forward range search");
		/* Start from the first key, get the rest using a range search */
		for(BPNode iter = tree.getnode(key1); iter != null; iter = iter.next())
		{
			if(iter.getdirectvalue(1) != null) out.println(iter.getdirectvalue(1)[0]);
			if(iter.getdirectvalue(2) != null) out.println(iter.getdirectvalue(2)[0]);
		}
		
		out.println("Reverse range search");
		/* Start from the last key, get the rest (in reverse) using a range search */
		for(BPNode iter = tree.getnode(key104); iter != null; iter = iter.prev())
		{
			if(iter.getdirectvalue(2) != null) out.println(iter.getdirectvalue(2)[0]);
			if(iter.getdirectvalue(1) != null) out.println(iter.getdirectvalue(1)[0]);
		}
	}
}
