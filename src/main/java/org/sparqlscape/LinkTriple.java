package org.sparqlscape;

public class LinkTriple implements Comparable<LinkTriple> {

	public LinkTriple(String subj, String pred, String obj) {
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
	}
	public String subj, pred, obj;

	@Override
	public String toString() {
		return "<" + subj + "> <" + pred + "> <" + obj + ">";		
	}	

	@Override
	public int compareTo(LinkTriple other) {
		int diff1 = subj.compareTo(other.subj);		
		int diff2 = pred.compareTo(other.pred);		
		int diff3 = obj.compareTo(other.obj);				
		if ( diff1 != 0 ) 
			return diff1;
		if ( diff2 != 0 ) 
			return diff2;
		if ( diff3 != 0 ) 
			return diff3;
		return 0;		
	}

	@Override
	public int hashCode() {
		return subj.hashCode() + pred.hashCode() + obj.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof LinkTriple) {
			LinkTriple other = (LinkTriple) o;
			return (subj == other.subj && pred == other.pred && obj == other.obj);
		}
		return false;
	}
}
